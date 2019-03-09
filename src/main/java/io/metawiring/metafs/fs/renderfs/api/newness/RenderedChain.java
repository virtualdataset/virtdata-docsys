package io.metawiring.metafs.fs.renderfs.api.newness;

import java.nio.ByteBuffer;
import java.security.InvalidParameterException;
import java.util.function.Supplier;

public class RenderedChain implements Rendered {
    private RenderedEntry[] entries;

    public RenderedChain(Supplier<ByteBuffer> initialBufferSource, TemplateCompiler... compilers) {
        this.entries = new RenderedEntry[compilers.length];
        if (compilers.length==0) {
            throw new InvalidParameterException("There must be at least 1 compiler for a rendered chain.");
        }
        entries[0]=new RenderedEntry(initialBufferSource, compilers[0]);
        if (entries.length>1) {
            for (int i = 1; i < compilers.length; i++) {
                entries[i] = new RenderedEntry(entries[i-1],compilers[i]);
            }
        }
    }

    @Override
    public long getVersion() {
        return entries[entries.length - 1].getVersion();
    }

    @Override
    public ByteBuffer apply(TargetPathView targetPathView) {
        return entries[entries.length -1].apply(targetPathView);
    }
}
