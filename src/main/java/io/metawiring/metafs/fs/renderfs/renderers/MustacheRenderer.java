package io.metawiring.metafs.fs.renderfs.renderers;

import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;
import io.metawiring.metafs.fs.renderfs.api.newness.TargetPathView;
import io.metawiring.metafs.fs.renderfs.api.RendererIO;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public class MustacheRenderer implements PathRendererTemplate {

    private final Template compiledTemplate;
    private ByteBuffer renderCache;

    public MustacheRenderer(Path path) {
        String template = RendererIO.readString(path);
        this.compiledTemplate = Mustache.compiler().compile(template);
    }

    public MustacheRenderer(ByteBuffer buf) {
        String template = new String(buf.array(), StandardCharsets.UTF_8);
        this.compiledTemplate = Mustache.compiler().compile(template);
    }

    public MustacheRenderer(String template) {
        this.compiledTemplate = Mustache.compiler().compile(template);
    }


    @Override
    public ByteBuffer apply(TargetPathView targetPathView) {
        return ByteBuffer.wrap(compiledTemplate.execute(targetPathView).getBytes(StandardCharsets.UTF_8));
    }

}


