package io.metawiring.metafs.fs.renderfs.renderers;

import io.metawiring.metafs.fs.renderfs.api.newness.TargetPathView;
import io.metawiring.metafs.fs.renderfs.api.RendererIO;
import org.mvel2.templates.CompiledTemplate;
import org.mvel2.templates.TemplateCompiler;
import org.mvel2.templates.TemplateRuntime;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

@SuppressWarnings("ALL")
public class MvelRenderer implements PathRendererTemplate {

    private final CompiledTemplate compiledTemplate;

    public MvelRenderer(Path path) {
        String template = RendererIO.readString(path);
        compiledTemplate = TemplateCompiler.compileTemplate(template);
    }

    public MvelRenderer(ByteBuffer buf) {
        String template = new String(buf.array(),StandardCharsets.UTF_8);
        compiledTemplate = TemplateCompiler.compileTemplate(template);
    }

    @Override
    public ByteBuffer apply(TargetPathView targetPathView) {
        String output = (String) TemplateRuntime.execute(compiledTemplate, targetPathView);
        return ByteBuffer.wrap(output.getBytes());
    }
}
