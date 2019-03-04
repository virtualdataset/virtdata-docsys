package io.metawiring.metafs;

import io.metawiring.metafs.core.MetaFS;
import io.metawiring.metafs.core.MetaFSProvider;

import java.nio.file.FileStore;
import java.nio.file.Path;
import java.nio.file.spi.FileSystemProvider;
import java.util.Arrays;
import java.util.Set;

public class TestableMetaFS extends MetaFS {
    private final Path[] roots;
    private final FileStore[] filestores;
    private Set<String> supportedFileAttributeViews;

    public TestableMetaFS(Path[] roots, FileStore[] filestores, Set<String> supportedFileAttributeViews) {
        this.roots = roots;
        this.filestores = filestores;
        this.supportedFileAttributeViews = supportedFileAttributeViews;
    }

    @Override
    public FileSystemProvider provider() {
        return new MetaFSProvider();
    }

    @Override
    public String getSeparator() {
        return "/";
    }

    @Override
    public Iterable<Path> getRootDirectories() {
        return Arrays.asList(roots);
    }

    @Override
    public Iterable<FileStore> getFileStores() {
        return Arrays.asList(filestores);
    }

    @Override
    public Set<String> supportedFileAttributeViews() {
        return supportedFileAttributeViews;
    }
}
