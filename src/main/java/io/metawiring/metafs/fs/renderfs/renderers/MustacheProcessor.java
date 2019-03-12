package io.metawiring.metafs.fs.renderfs.renderers;

import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;
import io.metawiring.metafs.fs.renderfs.api.Renderer;
import io.metawiring.metafs.fs.renderfs.api.TemplateCompiler;
import io.metawiring.metafs.fs.renderfs.model.TargetPathView;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class MustacheProcessor implements TemplateCompiler {

    public final static Mustache.Compiler compiler = Mustache.compiler().withFormatter(
            new RenderFSMustacheFormatter()
    );

    @Override
    public Renderer apply(ByteBuffer byteBuffer) {
        return new MustacheRenderer(byteBuffer);
    }

    public static class MustacheRenderer implements Renderer {

        private final Template compiledTemplate;

        public MustacheRenderer(ByteBuffer templateBuffer) {
            String rawTemplate = new String(templateBuffer.array(),StandardCharsets.UTF_8);
            this.compiledTemplate = compiler.compile(rawTemplate);
        }

        @Override
        public ByteBuffer apply(TargetPathView targetPathView) {
            String renderedText = compiledTemplate.execute(targetPathView);
            return ByteBuffer.wrap(renderedText.getBytes(StandardCharsets.UTF_8));
        }
    }
}


