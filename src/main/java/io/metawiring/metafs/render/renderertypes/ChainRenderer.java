package io.metawiring.metafs.render.renderertypes;

import java.nio.ByteBuffer;
import java.security.InvalidParameterException;

public class ChainRenderer extends FileRendererType {
    private final FileRendererType first;
    private final FileRendererType second;

    public ChainRenderer(FileRendererType first, FileRendererType second) {
        super(first.getSourceExtension(), second.getTargetExtension(),
                first.isCaseSensitive() && second.isCaseSensitive());

        if (!first.getTargetExtension().equals(second.getSourceExtension())) {
            throw new InvalidParameterException("The target extension of the first transformer '" +
                    first.getTargetExtension() + "' must match the source extension '" +
                    second.getSourceExtension() + "' of the second transformer.");
        }

        this.first = first;
        this.second = second;

    }

    @Override
    public ByteBuffer transform(ByteBuffer buffer) {
        ByteBuffer afterFirst = first.transform(buffer);
        ByteBuffer afterSecond = second.transform(afterFirst);
        return afterSecond;
    }
}
