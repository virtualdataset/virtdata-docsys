package io.metawiring.metafs.render.renderertypes;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Document;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class MarkdownRenderer extends FileRendererType {

    public MarkdownRenderer() {
        super("md", "html", true);
    }

    @Override
    public ByteBuffer transform(ByteBuffer input) {

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

    @Override
    public String toString() {
        return this.getSourceExtension() + "->" + getTargetExtension();
    }
}
