package io.metawiring.metafs.fs.renderfs.api.newness;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class TargetPathView implements Versioned {
    private Path path;
    private long version;

    public TargetPathView(Path path, long version) {
        this.path = path;
        this.version = version;
    }

    public List<String> getFiles() {
        List<String> files = new ArrayList<>();
        path.iterator().forEachRemaining(p -> files.add(p.getFileName().toString()));
        return files;
    }

    @Override
    public long getVersion() {
        return version;
    }

}
