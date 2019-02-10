package io.metawiring.metafs.layer;

import io.metawiring.metafs.MetaFSProvider;
import io.metawiring.metafs.MetaPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.security.InvalidParameterException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@SuppressWarnings("ALL")
public class LayerFSProvider extends MetaFSProvider {

    private final Logger logger = LoggerFactory.getLogger(LayerFSProvider.class);

    private static LayerFSProvider instance;
    public synchronized static LayerFSProvider get() {
        if (instance==null) {
            instance=new LayerFSProvider();
        }
        return instance;
    }
    private LayerFSProvider() {
    }

    @Override
    public synchronized FileSystem newFileSystem(URI uri, Map<String, ?> env) throws IOException {
        boolean readonly = true;
        LayerFS fs = new LayerFS();
        fs.setWritable(env != null && env.get("writable").toString().equals("true"));
        return fs;
    }

    @Override
    public SeekableByteChannel newByteChannel(Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs) throws IOException {
        MetaPath metapath = assertMetaPath(path);
        LayerFS layerFS = assertLayerFS(metapath);
        if (options.contains(StandardOpenOption.READ) || options.isEmpty()) {
            Path firstReadablePath = findFirstReadablePath(metapath, layerFS.getWrappedFilesystems());
            return firstReadablePath.getFileSystem().provider().newByteChannel(firstReadablePath,options,attrs);
        } else {
            Path firstWritablePath = findFirstWritablePath(metapath, layerFS.getWrappedFilesystems());
            return firstWritablePath.getFileSystem().provider().newByteChannel(firstWritablePath,options,attrs);
        }
    }

    @Override
    public DirectoryStream<Path> newDirectoryStream(Path dir, DirectoryStream.Filter<? super Path> filter) throws IOException {
        MetaPath metapath = assertMetaPath(dir);
        LayerFS layerFS = assertLayerFS(metapath);

        Set<String> names = new HashSet<>();
        Set<Path> paths = new HashSet<>();
        IOException possibleException = null;
        int foundDirectoryCount = 0;
        for (FileSystem fs : layerFS.getWrappedFilesystems()) {
            Path fsSpecificPath = fs.getPath(metapath.toString());
            try {
                DirectoryStream<Path> dsp = super.newDirectoryStream(fsSpecificPath, filter);
                foundDirectoryCount++;
                for (Path path : dsp) {
                    if (!names.contains(path.toString())) {
                        names.add(path.toString());
                        paths.add(path);
                    }
                }
            } catch (IOException ioe) {
                possibleException = ioe;
            }
        }
        if (foundDirectoryCount == 0) {
            throw new IOException("Unable to find even one directory entry in addLayers for path " + dir);
        }

        return new DirectoryStream<Path>() {
            @Override
            public Iterator<Path> iterator() {
                return paths.iterator();
            }

            @Override
            public void close() throws IOException {
            }
        };
    }

    @Override
    public <V extends FileAttributeView> V getFileAttributeView(Path path, Class<V> type, LinkOption... options) {

        return acceptFirstSuccess(path, p -> p.getFileSystem().provider().getFileAttributeView(p, type, options));
    }


    @Override
    public <A extends BasicFileAttributes> A readAttributes(Path path, Class<A> type, LinkOption... options) throws IOException {
        return acceptFirstSuccess(path, p -> {
            try {
                return p.getFileSystem().provider().readAttributes(p, type, options);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public Map<String, Object> readAttributes(Path path, String attributes, LinkOption... options) throws IOException {
        return acceptFirstSuccess(path, p -> {
            try {
                return p.getFileSystem().provider().readAttributes(p, attributes, options);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private <T> T acceptFirstSuccess(Path path, Function<Path, T> process) {
        MetaPath metapath = assertMetaPath(path);
        LayerFS layerFS = assertLayerFS(metapath);
        List<Exception> exceptions = new ArrayList<>();
        for (FileSystem wrappedFilesystem : layerFS.getWrappedFilesystems()) {
            try {
                Path localizedpath = wrappedFilesystem.getPath(metapath.toString());
                return process.apply(localizedpath);
            } catch (Exception e) {
                exceptions.add(e);
            }
        }
        throw new RuntimeException(exceptions.size() + " exceptions occurred:" + exceptions.stream().map(Exception::getMessage).collect(Collectors.joining(",")));
    }

    @Override
    public void checkAccess(Path path, AccessMode... modes) throws IOException {
        MetaPath metapath = assertMetaPath(path);
        LayerFS layerFS = assertLayerFS(metapath);
        IOException possibleException = null;
        for (FileSystem wrappedFilesystem : layerFS.getWrappedFilesystems()) {
            try {
                Path wrappedPath = wrappedFilesystem.getPath(metapath.toString());
                wrappedPath.getFileSystem().provider().checkAccess(wrappedPath, modes);
                return;
            } catch (IOException ioe) {
                possibleException = ioe;
            }
        }
        if (possibleException != null) {
            throw possibleException;
        }
        throw new RuntimeException("Invalid condition.");
    }


    private Path findFirstWritablePath(Path toWrite, List<FileSystem> fileSystems) {
        for (FileSystem fs : fileSystems) {
            if (!fs.isReadOnly()) {
                return fs.getPath(toWrite.toString());
            }
        }
        throw new RuntimeException("Unable to find a writable filesystem in addLayers.");

    }

    private Path findFirstReadablePath(Path toRead, List<FileSystem> fileSystems) {
        for (FileSystem fileSystem : fileSystems) {
            try {
                Path fsSpecificPath = fileSystem.getPath(toRead.toString());
                fsSpecificPath.getFileSystem().provider().checkAccess(fsSpecificPath,AccessMode.READ);
                return fsSpecificPath;
            } catch (IOException e) {
                logger.warn("Did not find readable file " + toRead + " in fs " + fileSystem);
            }
        }
        throw new RuntimeException("Unable to find a readable " + toRead + " in any addLayer");
    }

    private LayerFS assertLayerFS(MetaPath path) {
        if (!(path.getFileSystem() instanceof LayerFS)) {
            throw new RuntimeException("Unable to do LayerFS operations on Path from filesystem of type " + path.getFileSystem().getClass().getCanonicalName());
        }
        return (LayerFS) path.getFileSystem();
    }

    private MetaPath assertMetaPath(Path path) {
        if (!(path instanceof MetaPath)) {
            throw new InvalidParameterException("Unable to do MetaPath operations on Path of type " + path.getClass().getCanonicalName());
        }
        return (MetaPath) path;
    }
}
