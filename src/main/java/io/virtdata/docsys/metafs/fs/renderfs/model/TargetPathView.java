package io.virtdata.docsys.metafs.fs.renderfs.model;

import io.virtdata.docsys.metafs.fs.renderfs.api.Versioned;

import java.io.IOException;
import java.nio.file.DirectoryStream;
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

    public PathView getPath() {
        return new PathView(path);
    }

    public ListView getFiles() {
        List<String> files = new ArrayList<>();
        Path dirPath = path.getParent();
        try {
            DirectoryStream<Path> paths =
                    dirPath.getFileSystem().provider()
                            .newDirectoryStream(dirPath, AcceptAllFiles);
            paths.forEach(p -> files.add(p.getFileName().toString()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new ListView(files);
    }

    public TreeView getFileTree() {
        return new TreeView(this);
    }

    private final static DirectoryStream.Filter<Path> AcceptAllFiles = new DirectoryStream.Filter<Path>() {
        @Override
        public boolean accept(Path entry) throws IOException {
            return true;
        }
    };

    @Override
    public long getVersion() {
        return version;
    }

}
