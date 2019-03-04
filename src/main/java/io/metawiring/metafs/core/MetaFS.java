package io.metawiring.metafs.core;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.WatchService;
import java.nio.file.attribute.UserPrincipalLookupService;

public abstract class MetaFS extends FileSystem {

    protected boolean isReadOnly=true;

    @Override
    public void close() throws IOException {
    }

    @Override
    public boolean isOpen() {
        return true;
    }

    @Override
    public boolean isReadOnly() {
        return isReadOnly;
    }

    @Override
    public PathMatcher getPathMatcher(String syntaxAndPattern) {
        return null;
    }

    @Override
    public UserPrincipalLookupService getUserPrincipalLookupService() {
        return null;
    }

    @Override
    public WatchService newWatchService() throws IOException {
        return null;
    }

    public Path getRootPath() {
        return new MetaPath(this, "/");
    }

    @Override
    public Path getPath(String first, String... more) {
        return new MetaPath(this, first, more);
    }

//    protected abstract BasicFileAttributes readAttributes(Path path, Class type, LinkOption[] options) throws IOException;
//
//    protected abstract Map<String, Object> readAttributes(Path path, String attributes, LinkOption[] options) throws IOException;
//
//    protected abstract FileAttributeView getFileAttributeView(Path path, Class type, LinkOption... options);
}
