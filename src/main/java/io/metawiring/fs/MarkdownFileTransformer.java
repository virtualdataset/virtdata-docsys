package io.metawiring.fs;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Document;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

public class MarkdownFileTransformer implements TransformedPath {

    private final Path sourcePath;
    private final Path virtualPath;
    private ByteBuffer rendered;
    private final static MarkdownRenderer renderer = new MarkdownRenderer();

    public MarkdownFileTransformer(Path virtualPath) {
        this.virtualPath = virtualPath;
        this.sourcePath = virtualPath.getFileSystem().getPath(virtualPath.toString().replaceFirst("\\.html", ".md"));
    }

    @Override
    public Path getSourcePath() {
        return sourcePath;
    }

    @Override
    public Path getVirtualPath() {
        return virtualPath;
    }

    @Override
    public synchronized ByteBuffer getRendered() {
        if (rendered == null) {
            try {
                byte[] bytes = Files.readAllBytes(sourcePath);
                rendered = getContentTransformer().apply(ByteBuffer.wrap(bytes));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return rendered;
    }

    @Override
    public Function<ByteBuffer, ByteBuffer> getContentTransformer() {
        return renderer;
    }


    private static class MarkdownRenderer implements Function<ByteBuffer, ByteBuffer> {
        @SuppressWarnings("Duplicates")
        @Override
        public ByteBuffer apply(ByteBuffer input) {

            byte[] buf = new byte[input.remaining()];
            input.get(buf);
            String rawMarkdown = new String(buf, StandardCharsets.UTF_8);

            Parser parser = Parser.builder().build();
            HtmlRenderer renderer = HtmlRenderer.builder().build();

            Document document = parser.parse(rawMarkdown);
            String html = renderer.render(document);
            ByteBuffer htmlBytes = ByteBuffer.wrap(html.getBytes(StandardCharsets.UTF_8));
            return htmlBytes;
        }
    }

    public static class MarkdownTransformerEntry implements TransformerEntry {

        @Override
        public Pattern getSourcePattern() {
            return Pattern.compile(".*\\.[mM][dD]");
        }

        @Override
        public Pattern getTargetPattern() {
            return Pattern.compile(".*\\.(html|HTML)");
        }

        @Override
        public Function<Path, TransformedPath> getFileTransformer() {
            return MarkdownFileTransformer::new;
        }

        @Override
        public UnaryOperator<Path> getSourceTargetMapper() {
            return (p) -> p.getFileSystem().getPath(p.toString().replaceFirst("\\.md",".html"));
        }

        @Override
        public UnaryOperator<Path> getTargetSourceMapper() {
            return (p) -> p.getFileSystem().getPath(p.toString().replaceFirst("\\.html", ".md"));
        }
    }

}
