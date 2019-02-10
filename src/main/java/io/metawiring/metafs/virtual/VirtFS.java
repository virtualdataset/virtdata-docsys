package io.metawiring.metafs.virtual;

import io.metawiring.metafs.MetaFS;
import io.metawiring.metafs.MetaPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.WatchService;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.nio.file.spi.FileSystemProvider;
import java.security.InvalidParameterException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * This MetaFS filesystem type simply virtualizes a root directory
 * behind a filesystem interface.
 */
public class VirtFS extends MetaFS {

    protected final static Logger logger = LoggerFactory.getLogger(VirtFS.class);

    protected VirtFSProvider provider;

    private final Path outerMount;
    private final Path innerRoot = new MetaPath(this,"/");
//    protected final URI uri;
    private Path sysDefaultPath;

//    protected VirtFS(VirtFSProvider provider, URI uri) {
//        this.provider = provider;
//
//        outerMount=new URI("VIRTUAL",null,uri.getPath(),null, null);
//        this.uri = uri;
//        this.innerRoot = new MetaPath(this,"/");
//    }

    public VirtFS(Path outerPath, Path sysDefaultPath) {
        this.outerMount = outerPath;
//        this(VirtFSProvider.get(),outerPath.toUri());
        setSysDefaultPath(sysDefaultPath);
    }

    public VirtFS(Path outerPath) {
        this.outerMount=outerPath;
//        this(VirtFSProvider.get(), outerPath.toUri());
        setSysDefaultPath(outerPath);
    }


    public VirtFS setSysDefaultPath(Path sysDefaultPath) {
        if (sysDefaultPath.getFileSystem() != outerMount.getFileSystem()) {
            throw new InvalidParameterException("The default path must be part of the enclosing filesystem.");
        }
        this.sysDefaultPath = sysDefaultPath.toAbsolutePath();
        return this;
    }

    @Override
    public FileSystemProvider provider() {
        return provider;
    }


    @Override
    public String getSeparator() {
        return outerMount.getFileSystem().getSeparator();
    }

    @Override
    public Iterable<Path> getRootDirectories() {
        Iterable<Path> outerRoots = outerMount.getFileSystem().getRootDirectories();
        List<Path> localizedPaths = StreamSupport
                .stream(outerRoots.spliterator(), false)
                .map(p -> this.getPath(p.getName(0).toString()))
                .collect(Collectors.toList());
        return localizedPaths;
    }

    @Override
    public Iterable<FileStore> getFileStores() {
        return outerMount.getFileSystem().getFileStores();
    }

    @Override
    public Set<String> supportedFileAttributeViews() {
        return outerMount.getFileSystem().supportedFileAttributeViews();
    }

    @Override
    public Path getPath(String first, String... more) {
        return new MetaPath(this, first, more);
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

    public Path getContainerFilesystemPath(Path metaPath) {
        assertThisFileSystemOwnership(metaPath);
        Path sysRelativePath = null;
        if (metaPath.isAbsolute()) {
            sysRelativePath = this.outerMount.getFileSystem().getPath(metaPath.toString());
        } else if (sysDefaultPath!=null) {
            sysRelativePath = this.sysDefaultPath.resolve(this.outerMount.getFileSystem().getPath(metaPath.toString()));
        } else {
            throw new InvalidParameterException("The system default path was not set, and the logical filesystem path provided was not absolute.");
        }
        Path resolved = this.outerMount.resolve(sysRelativePath);
        return resolved;
    }

    private void assertThisFileSystemOwnership(Path relPath) {
        if (relPath.getFileSystem()!=this) {
            throw new InvalidParameterException("This path is not owned by the current filesystem.");
        }
    }

    public Path getRoot() {
        return innerRoot;
    }

    @Override
    public String toString() {
        return "MetaFS::" + this.getClass().getSimpleName();
    }

//    public Path getSysPath() {
//        return filesystem.getContainerFilesystemPath(this);
//    }

}
