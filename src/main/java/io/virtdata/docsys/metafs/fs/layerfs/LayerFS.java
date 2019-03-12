package io.virtdata.docsys.metafs.fs.layerfs;

import io.virtdata.docsys.metafs.core.MetaFS;
import io.virtdata.docsys.metafs.core.MetaPath;
import io.virtdata.docsys.metafs.fs.virtual.VirtFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * This filesystem is a filesystem aggregator, allowing users to
 * provide layered views of existing filesystems. The URI is merely
 * symbolic in this filesystem, since the user must endRenderers any wrapped
 * filesystems separately from the call to
 * {@link java.nio.file.spi.FileSystemProvider#newFileSystem(URI, Map)}.
 *
 * <H2>Meta Details</H2>
 *
 * <P>In general, operations are attempted on each filesystem, each
 * with a path scoped to that filesystem instance, until the operation
 * succeeds without throwing an error.
 * </P>
 *
 * <P>For operations which specify read-specific or write-specific options,
 * any read-only filesystems are skipped when write mode is requested.
 * By default, filesystems are registered as readonly, providing some
 * default safety.</P>
 */
public class LayerFS extends MetaFS {

    private final static Logger logger = LoggerFactory.getLogger(LayerFS.class);
    private static LayerFSProvider provider = LayerFSProvider.get();
    private List<FileSystem> wrappedFilesystems = new ArrayList<>();


    public LayerFS() {
    }

    public LayerFS setWritable(boolean writable) {
        this.isReadOnly=!writable;
        return this;
    }

    public LayerFS addLayer(Path outerPath) {
        VirtFS metafs = new VirtFS(outerPath);
        this.wrappedFilesystems.add(metafs);
        return this;
    }

    public LayerFS addLayer(FileSystem fileSystem) {
        this.wrappedFilesystems.add(fileSystem);
        return this;
    }

    @Override
    public Iterable<Path> getRootDirectories() {
        Set<String> pathNames = new HashSet<>();
        Set<Path> paths = new HashSet<>();
        for (FileSystem fileSystem : wrappedFilesystems) {
            for (Path rootDirectory : fileSystem.getRootDirectories()) {
                String dirName = rootDirectory.toString();
                if (!pathNames.contains(dirName)) {
                    pathNames.add(dirName);
                    paths.add(rootDirectory);
                }
            }
        }
        return paths;
    }

    @Override
    public LayerFSProvider provider() {
        return provider;
    }

    @Override
    public String getSeparator() {
        if (wrappedFilesystems.size() > 0) {
            return wrappedFilesystems.get(0).getSeparator();
        }
        return FileSystems.getDefault().getSeparator();
    }

    @Override
    public Iterable<FileStore> getFileStores() {
        return () -> wrappedFilesystems.stream().map(FileSystem::getFileStores)
                .map(f -> StreamSupport.stream(f.spliterator(), false))
                .flatMap(i -> StreamSupport.stream(i.spliterator(), false)).iterator();
    }

    @Override
    public Set<String> supportedFileAttributeViews() {
        if (wrappedFilesystems.size()==0) {
            return new HashSet<>();
        }
        Set<String> sfav = null;
        for (FileSystem fileSystem : wrappedFilesystems) {
            if (sfav == null) {
                sfav = fileSystem.supportedFileAttributeViews();
            } else {
                sfav.retainAll(fileSystem.supportedFileAttributeViews());
            }
        }
        return sfav;
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


    public List<FileSystem> getWrappedFilesystems() {
        return this.wrappedFilesystems;
    }

    @Override
    public String toString() {
        return "LayerFS:" + wrappedFilesystems.stream().map(String::valueOf).collect(Collectors.joining(",", "[[", "]]"));
    }

//    @Override
//    public Path getSysPath(MetaPath path) {
//        for (FileSystem wfs : wrappedFilesystems) {
//
//        }
//        return null;
//    }

//    @Override
//    public Path getRoot() {
//        return null;
//    }
}
