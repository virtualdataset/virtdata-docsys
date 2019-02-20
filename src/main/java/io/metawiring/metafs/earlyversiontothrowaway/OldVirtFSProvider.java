package io.metawiring.metafs.earlyversiontothrowaway;

public class OldVirtFSProvider  {
//    private final static Logger logger = LoggerFactory.getLogger(VirtFSProvider.class);
//    FileSystem sysfs = FileSystems.getDefault();
//    private Map<URI, VirtFS> filesystems = new ConcurrentHashMap<>();
//    private RenderFSMethods vtransform = new RenderFSMethods();
//
//    private static Path getContainerPath(Path path) {
//        if (path instanceof VirtFSPath) {
//            VirtFSPath metaFSPath = (VirtFSPath) path;
//            Path sysPath = metaFSPath.getSysPath();
//            return sysPath;
//        } else {
//            throw new InvalidParameterException("Non-meta path was given to meta fs to convert to a system path.");
//        }
//    }
//
//    @Override
//    public String getScheme() {
//        return "meta";
//    }
//
//    @Override
//    public synchronized FileSystem newFileSystem(URI uri, Map<String, ?> env) throws IOException {
//        VirtFS metaFS = new VirtFS(this, uri, env);
//        logger.debug("started new meta fs on " + metaFS.getSysPath());
//        filesystems.put(uri, metaFS);
//        return metaFS;
//    }
//
//
//    @Override
//    public FileChannel newFileChannel(Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs) throws IOException {
//        FileChannel channel = null;
//        try {
//            channel = super.newFileChannel(path, options, attrs);
//            return channel;
//        } catch (Exception ioe) {
//            throw new RuntimeException(ioe);
//        }
//    }
//
//    @Override
//    public FileSystem getFileSystem(URI uri) {
//        return filesystems.get(uri);
//    }
//
//    @Override
//    public Path getPath(URI uri) {
//        VirtFS extant = null;
//        int matching = 0;
//
//        if (uri.getScheme().equals(this.getScheme())) {
//            throw new IllegalArgumentException("The scheme for " + uri + " does not match the scheme for " + this);
//        }
//
//        for (URI rooturi : filesystems.keySet()) {
//            URI relUri = rooturi.relativize(uri);
//            if (relUri.equals(uri)) {
//                matching++;
//                extant = filesystems.get(uri);
//            }
//            if (matching > 1) {
//                throw new RuntimeException("Of the known filesystem instances, more than one matched " + uri);
//            }
//            if (matching == 0) {
//                throw new FileSystemNotFoundException("Unable to match uri against any filesystem instances: " + uri);
//            }
//        }
//
//        return extant.getPath(uri.getPath());
//    }
//
//    @Override
//    public SeekableByteChannel newByteChannel(Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs) throws IOException {
//        Path getContainerPath = getContainerPath(path);
//        return FileSystems.getDefault().provider().newByteChannel(getContainerPath, options, attrs);
//    }
//
//    @Override
//    public DirectoryStream<Path> newDirectoryStream(Path dir, DirectoryStream.Filter<? super Path> filter) throws IOException {
//        Path getContainerPath = getContainerPath(dir);
//        DirectoryStream<Path> sysDirectoryStream = FileSystems.getDefault().provider().newDirectoryStream(getContainerPath, filter);
//        return vtransform.transformDirectoryStream(sysDirectoryStream);
//    }
//
//    @Override
//    public void createDirectory(Path dir, FileAttribute<?>... attrs) throws IOException {
//        Path getContainerPath = getContainerPath(dir);
//        FileSystems.getDefault().provider().createDirectory(getContainerPath, attrs);
//    }
//
//    @Override
//    public void delete(Path path) throws IOException {
//        Path getContainerPath = getContainerPath(path);
//        FileSystems.getDefault().provider().delete(getContainerPath);
//    }
//
//    @Override
//    public void copy(Path source, Path target, CopyOption... options) throws IOException {
//        Path syspathSource = getContainerPath(source);
//        Path syspathTarget = getContainerPath(target);
//        FileSystems.getDefault().provider().copy(syspathSource, syspathTarget, options);
//    }
//
//    @Override
//    public void move(Path source, Path target, CopyOption... options) throws IOException {
//        Path syspathSource = getContainerPath(source);
//        Path syspathTarget = getContainerPath(target);
//        FileSystems.getDefault().provider().move(syspathSource, syspathTarget, options);
//    }
//
//    @Override
//    public boolean isSameFile(Path path, Path path2) throws IOException {
//        Path syspath1 = getContainerPath(path);
//        Path syspath2 = getContainerPath(path2);
//        return FileSystems.getDefault().provider().isSameFile(syspath1, syspath2);
//    }
//
//    @Override
//    public boolean isHidden(Path path) throws IOException {
//        Path getContainerPath = getContainerPath(path);
//        return FileSystems.getDefault().provider().isHidden(getContainerPath);
//    }
//
//    @Override
//    public FileStore getFileStore(Path path) throws IOException {
//        return FileSystems.getDefault().provider().getFileStore(getContainerPath(path));
//    }
//
//    @Override
//    public void checkAccess(Path path, AccessMode... modes) throws IOException {
//        try {
//            FileSystems.getDefault().provider().checkAccess(getContainerPath(path), modes);
//        } catch (Exception e) {
//            try {
//                vtransform.maybeCheckAccess(getContainerPath(path));
//            } catch (Exception e2) {
//                throw e;
//            }
//        }
//    }
//
//    @Override
//    public <V extends FileAttributeView> V getFileAttributeView(Path path, Class<V> type, LinkOption... options) {
//        return FileSystems.getDefault().provider().getFileAttributeView(getContainerPath(path), type, options);
//    }
//
//    @Override
//    public <A extends BasicFileAttributes> A readAttributes(Path path, Class<A> type, LinkOption... options) throws IOException {
//        Path getContainerPath = getContainerPath(path);
//        FileSystemProvider provider = FileSystems.getDefault().provider();
//        try {
//            A attributes = provider.readAttributes(getContainerPath, type, options);
//            return attributes;
//        } catch (Exception e) {
//            try {
//                A attributes = vtransform.maybeReadSourceAttributes(getContainerPath, type, options);
//                return attributes;
//            } catch (Exception e2) {
//                throw new IOException("Unable to read attributes for " + path);
//            }
//        }
//    }
//
//    @Override
//    public Map<String, Object> readAttributes(Path path, String attributes, LinkOption... options) throws IOException {
//        Path getContainerPath = getContainerPath(path);
//        FileSystemProvider provider = FileSystems.getDefault().provider();
//        try {
//            Map<String, Object> map = provider.readAttributes(getContainerPath, attributes, options);
//            return map;
//        } catch (Exception e) {
//            try {
//                Map<String, Object> map = vtransform.maybeReadSourceAttributes(getContainerPath, attributes, options);
//                return map;
//            } catch (Exception e2) {
//                throw new IOException("Unable to read attribute map for " + path + " or " + getContainerPath);
//            }
//        }
//    }
//
//    @Override
//    public void setAttribute(Path path, String attribute, Object value, LinkOption... options) throws IOException {
//        FileSystems.getDefault().provider().setAttribute(getContainerPath(path), attribute, value, options);
//    }
//
//    public void unregister(VirtFS metaFS) {
//        filesystems.remove(metaFS.getUri());
//    }
}