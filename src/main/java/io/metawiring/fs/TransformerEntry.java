package io.metawiring.fs;

import java.nio.file.Path;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

public interface TransformerEntry {
    Pattern getSourcePattern();
    Pattern getTargetPattern();
    UnaryOperator<Path> getSourceTargetMapper();
    UnaryOperator<Path> getTargetSourceMapper();

    Function<Path,TransformedPath> getFileTransformer();
    default boolean matchesSource(Path p) {
        return getSourcePattern().matcher(p.toString()).matches();
    }
    default boolean matchesTarget(Path p) {
        return getTargetPattern().matcher(p.toString()).matches();
    }

    default Path getTargetNameForSource(Path path) {
        return getTargetSourceMapper().apply(path);
    }
    default Path getSourceNameForTarget(Path path) {
        return getSourceTargetMapper().apply(path);
    };
}
