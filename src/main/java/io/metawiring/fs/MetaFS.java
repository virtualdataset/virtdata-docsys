package io.metawiring.fs;

import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.nio.file.spi.FileSystemProvider;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MetaFS extends FileSystem {

    private final MetaFSProvider provider;
    private final Map<String, ?> env;
    private final URI uri;
    private Path sysRoot;
    private Path innerRoot;

    public MetaFS(MetaFSProvider provider, URI uri, Map<String, ?> env) {
        this.provider = provider;
        this.uri = uri;
        sysRoot = FileSystems.getDefault().getPath(uri.getRawPath());
        this.innerRoot = new MetaPath(this,"/");
        this.env = env;
    }

    @Override
    public FileSystemProvider provider() {
        return provider;
    }

    @Override
    public void close() throws IOException {
        provider.unregister(this);

    }

    @Override
    public boolean isOpen() {
        return provider.getFileSystem(this.getUri())!=null;
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    @Override
    public String getSeparator() {
        return FileSystems.getDefault().getSeparator();
    }

    @Override
    public Iterable<Path> getRootDirectories() {
        List<Path> paths = new ArrayList<>();
        try {
            Files.list(getSysPath()).forEach(p -> paths.add(new MetaPath(this,p.getFileName().toString())));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return paths;
    }

    @Override
    public Iterable<FileStore> getFileStores() {
        return sysRoot.getFileSystem().getFileStores();
    }

    @Override
    public Set<String> supportedFileAttributeViews() {
        return sysRoot.getFileSystem().supportedFileAttributeViews();
    }

    @Override
    public Path getPath(String first, String... more) {
        return new MetaPath(this,first,more);
    }

    @Override
    public PathMatcher getPathMatcher(String syntaxAndPattern) {
        return sysRoot.getFileSystem().getPathMatcher(syntaxAndPattern);
    }

    @Override
    public UserPrincipalLookupService getUserPrincipalLookupService() {
        return sysRoot.getFileSystem().getUserPrincipalLookupService();
    }

    @Override
    public WatchService newWatchService() throws IOException {
        return sysRoot.getFileSystem().newWatchService();
    }

    public URI getUri() {
        return uri;
    }

    public Path getRootPath() {
        return innerRoot;
    }

    public Path getSysPath() {
        return sysRoot;
    }

    public Path getSysPath(Path relPath) {
        if (relPath.getFileSystem()!=this) {
            throw new InvalidParameterException("System path was provided for a meta path.");
        }
        if (!relPath.isAbsolute()) {
            throw new InvalidParameterException("relative paths are not expected here");
        }

        String relpathString = relPath.toString().substring(1);
        Path sysRelpath = FileSystems.getDefault().getPath(relpathString);
        Path resolved = sysRoot.resolve(sysRelpath);
        return resolved;
    }
}
