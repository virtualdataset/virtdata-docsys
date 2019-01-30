package io.metawiring.fs;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;

public class MetaPath implements Path {
    MetaFS fileSystem;
    private String path;

    public MetaPath(MetaFS fileSystem, String first, String... more) {
        this.fileSystem = fileSystem;
        StringBuilder sb = new StringBuilder(first);
        for (String component : more) {
            if (sb.lastIndexOf("/")!=sb.length()-1) {
                sb.append(FileSystems.getDefault().getSeparator());
            }
            sb.append(component.startsWith(FileSystems.getDefault().getSeparator()) ? component.substring(1) : component);
        }
        path = sb.toString();
    }

    @Override
    public MetaFS getFileSystem() {
        return fileSystem;
    }

    @Override
    public boolean isAbsolute() {
        return path != null && path.length()>0 && path.startsWith("/");
    }

    @Override
    public Path getRoot() {
        return fileSystem.getRootPath();
    }

    @Override
    public Path getFileName() {
        int lastSeparator = path.lastIndexOf(FileSystems.getDefault().getSeparator());
        if (lastSeparator<0) {
            return null;
        }
        return new MetaPath(fileSystem,path.substring(lastSeparator+1));
    }

    @Override
    public Path getParent() {
        int lastSeparator = path.lastIndexOf(FileSystems.getDefault().getSeparator());
        if (lastSeparator<0) {
            return null;
        }
        return new MetaPath(fileSystem,path.substring(0,lastSeparator-1));
    }

    @Override
    public int getNameCount() {
        if (path.equals("/")) {
            return 0;
        }
        int length = path.split(FileSystems.getDefault().getSeparator()).length;
        int nameCount = path.startsWith("/") ? length - 1 : length;
        return nameCount;
    }

    @Override
    public Path getName(int index) {
        return subpath(0,index);
    }

    @Override
    public Path subpath(int beginIndex, int endIndex) {
        String tosplit = path.startsWith("/") ? path.substring(1) : path;
        String[] components = tosplit.split(FileSystems.getDefault().getSeparator());
        if (beginIndex < 0 || beginIndex > components.length-1) {
            throw new IllegalArgumentException();
        }
        if (endIndex < 0 || endIndex > components.length-1) {
            throw new IllegalArgumentException();
        }
        if (endIndex<beginIndex) {
            throw new IllegalArgumentException();
        }

        StringBuilder sb = new StringBuilder();
        for (int i = beginIndex; i < endIndex+1; i++) {
            sb.append(components[i]);
            sb.append(FileSystems.getDefault().getSeparator());
        }
        sb.setLength(sb.length()-1);
        return new MetaPath(fileSystem,sb.toString());
    }

    @Override
    public boolean startsWith(Path other) {
        return path.startsWith(other.toString());
    }

    @Override
    public boolean endsWith(Path other) {
        return path.endsWith(other.toString());
    }

    @Override
    public Path normalize() {
        Path syspath = FileSystems.getDefault().getPath(this.path);
        String normalized = syspath.normalize().toString();
        if (!normalized.equals(path)) {
            return new MetaPath(fileSystem,normalized);
        }
        return this;
    }

    @Override
    public Path resolve(Path other) {
        Path syspath = FileSystems.getDefault().getPath(path);
        Path sysOther = FileSystems.getDefault().getPath(other.toString());
        Path resolved = syspath.resolve(sysOther);
        return new MetaPath(fileSystem, resolved.toString());
    }

    @Override
    public Path relativize(Path other) {
        Path syspath = FileSystems.getDefault().getPath(path);
        Path relativized = syspath.relativize(other);
        return new MetaPath(fileSystem,relativized.toString());
    }

    @Override
    public URI toUri() {
        try {
            return new URI(fileSystem.provider().getScheme(),null, path,null);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Path toAbsolutePath() {
        return fileSystem.getRootPath().resolve(this);
    }

    @Override
    public Path toRealPath(LinkOption... options) throws IOException {
        return toAbsolutePath();
    }

    public WatchKey register(
            WatchService watcher,
            WatchEvent.Kind<?>[] events,
            WatchEvent.Modifier... modifiers) {
        try {
            return getSysPath().register(watcher, events, modifiers);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int compareTo(Path other) {
        return path.compareTo(other.toString());
    }

    public String toString() {
        return path;
    }

    @Override
    public File toFile() {
        return null;
//        return new File(path);
    }

    public Path getSysPath() {
        return getFileSystem().getSysPath(this);
    }
}
