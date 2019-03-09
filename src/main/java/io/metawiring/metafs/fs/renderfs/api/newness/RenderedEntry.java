package io.metawiring.metafs.fs.renderfs.api.newness;

import java.nio.ByteBuffer;
import java.util.function.Supplier;

public class RenderedEntry implements Rendered {

    private long renderedVersion;
    private final Supplier<ByteBuffer> bufferSource;

    private ByteBuffer input;
    private TemplateCompiler compiler;
    private Renderer renderer;
    private ByteBuffer output;

    private final RenderedEntry upstream;

    public RenderedEntry(Supplier<ByteBuffer> bufferSource, TemplateCompiler compiler) {
        this.compiler = compiler;
        this.bufferSource = bufferSource;
        this.upstream = null;
    }
    public RenderedEntry(RenderedEntry upstream, TemplateCompiler compiler) {
        this.upstream = upstream;
        this.compiler = compiler;
        this.bufferSource=null;
    }

    @Override
    public long getVersion() {
        return renderedVersion;
    }

    @Override
    public ByteBuffer apply(TargetPathView targetPathView) {

        if (output!=null && isValidFor(targetPathView)) {
            return output;
        }
        // Get the raw template image
        input = (bufferSource!=null) ? bufferSource.get() : upstream.apply(targetPathView);

        // Create a compiled template of some type
        renderer= compiler.apply(input);

        // Apply the view context to the compiled template
        output = renderer.apply(targetPathView);

        // Update the cache info
        renderedVersion = targetPathView.getVersion();
        return output;
    }
}
