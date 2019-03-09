package io.metawiring.metafs.fs.renderfs.renderers;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Document;
import io.metawiring.metafs.fs.renderfs.api.newness.TargetPathView;
import io.metawiring.metafs.fs.renderfs.api.RendererIO;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public class MarkdownRenderer implements PathRendererTemplate {

    protected final static Parser parser = Parser.builder().build();
    protected final static HtmlRenderer renderer = HtmlRenderer.builder().build();
    private final Document document;

    public MarkdownRenderer(Path path) {
        String template = RendererIO.readString(path);
        document = parser.parse(template);
    }

    public MarkdownRenderer(ByteBuffer buf) {
        document = parser.parse(new String(buf.array(),StandardCharsets.UTF_8));
    }

    @Override
    public ByteBuffer apply(TargetPathView targetPathView) {
        String rendered = renderer.render(document);
        return ByteBuffer.wrap(rendered.getBytes());
    }


}
