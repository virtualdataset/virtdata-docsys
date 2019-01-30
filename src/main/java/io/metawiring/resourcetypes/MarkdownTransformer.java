package io.metawiring.resourcetypes;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Document;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;
import java.util.regex.Pattern;

public class MarkdownTransformer implements ResourceTransformer {

    private final static MarkdownRenderer contentRenderer = new MarkdownRenderer();

    @Override
    public Function<ByteBuffer, ByteBuffer> getContentTransformer() {
        return contentRenderer;
    }

    @Override
    public Function<String, String> mimeTypeTransformer() {
        return (s) -> "text/html";
    }

    private static class NameRenderer implements Function<String,String> {
        private static Pattern extension = Pattern.compile("\\.(md|MD)$");
        @Override
        public String apply(String s) {
            return s.replaceFirst(extension.pattern(),".html");
        }
    }

    private static class MarkdownRenderer implements Function<ByteBuffer, ByteBuffer> {
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

}
