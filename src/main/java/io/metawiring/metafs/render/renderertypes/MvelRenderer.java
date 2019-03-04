package io.metawiring.metafs.render.renderertypes;

import org.mvel2.templates.CompiledTemplate;
import org.mvel2.templates.TemplateCompiler;
import org.mvel2.templates.TemplateRuntime;

@SuppressWarnings("ALL")
public class MvelRenderer extends FileRendererType {

    public MvelRenderer(String sourceExtension, String targetExtension) {
        super(sourceExtension, targetExtension,true);
    }

    @Override
    public String transform(String input) {
        CompiledTemplate compiledTemplate = TemplateCompiler.compileTemplate(input);
        String output = (String) TemplateRuntime.execute(compiledTemplate);
        return output;
    }

    @Override
    public String toString() {
        return getSourceExtension() + "->" + getTargetExtension();
    }
}
