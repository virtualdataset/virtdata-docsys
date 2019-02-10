package io.metawiring.metafs;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * A MetaPath represent a logical path for one of any MetaFS derivatives.
 * A MetaPath is meant to be filesystem provider agnostic in terms of the
 * path syntax. Internally, the MetaPath syntax sill defer to the
 * default FileSystem provider's syntax.
 */
public class MetaPath implements Path {
    private final MetaFS filesystem;
    private final String[] path;
    private final boolean isAbsolute;

    public MetaPath(MetaFS metaFS, String initial, String... remaining) {
        this.filesystem = metaFS;
        // Rely on system implementation to canonicalize path, etc
        Path sysformattedPath = Path.of(initial, remaining);
        this.path = sysformattedPath.toString().split(FileSystems.getDefault().getSeparator());
        isAbsolute=initial.startsWith(FileSystems.getDefault().getSeparator());
    }

    public MetaPath(MetaFS metaFS, String[] components, boolean absolute) {
        this.filesystem = metaFS;
        this.path = components;
        this.isAbsolute = absolute;
    }

    @Override
    public MetaFS getFileSystem() {
        return filesystem;
    }

    @Override
    public boolean isAbsolute() {
        return isAbsolute;
    }

    @Override
    public Path getRoot() {
        if (this.isAbsolute) {
            return new MetaPath(filesystem,FileSystems.getDefault().getSeparator());
        }
        return null;
    }

    @Override
    public Path getFileName() {
        return new MetaPath(filesystem, path[path.length-1]);
    }

    @Override
    public Path getParent() {
        String[] parentArray = new String[path.length-1];
        System.arraycopy(path,0,parentArray,0,parentArray.length);
        return new MetaPath(filesystem,parentArray,isAbsolute);
    }

    @Override
    public int getNameCount() {
        return path.length;
    }

    @Override
    public Path getName(int index) {
        return new MetaPath(filesystem, path[index]);
    }

    @Override
    public Path subpath(int beginIndex, int endIndex) {
        if (beginIndex<0 || endIndex > path.length-1) {
            throw new InvalidParameterException("Index range must be within available path name count: " +
                    "0 < begin(" + beginIndex + ") <= end(" + endIndex +") <= " + (path.length-1) + " ?");
        }
        int len = (endIndex-beginIndex)+1;
        String[] components = new String[len];
        System.arraycopy(path, beginIndex, components, 0, len);
        return new MetaPath(filesystem,components,false);
    }

    @Override
    public boolean startsWith(Path other) {
        if (path.length < other.getNameCount()) {
            return false;
        }
        if (getFileSystem()!=other.getFileSystem()) {
            return false;
        }
        if (isAbsolute!=other.isAbsolute()) {
            return false;
        }
        for (int i = 0; i < other.getNameCount(); i++) {
            if (! path[i].equals(other.getName(i).toString())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean endsWith(Path other) {
        if (other.isAbsolute()) {
            return false;
        }
        if (getNameCount()< other.getNameCount()) {
            return false;
        }
        for (int i = 0; i < other.getNameCount(); i++) {
            if (! path[path.length-i].equals(other.getName(other.getNameCount()-(i+1)))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Path normalize() {
        String normalized = tmpSysPath().normalize().toString();
        if (!normalized.equals(this)) {
            return new MetaPath(filesystem,normalized);
        }
        return this;
    }

    @Override
    public Path resolve(Path other) {
        assertFilesystemOwnership(other);

        tmpSysPath().resolve(other);
        Path sysResolved = tmpSysPath().resolve(tmpSysPath((MetaPath)other));
        return new MetaPath(filesystem, sysResolved.toString());
    }

    @Override
    public Path relativize(Path other) {
        assertFilesystemOwnership(other);
        Path sysRelativized = tmpSysPath().relativize(tmpSysPath((MetaPath)other));
        return new MetaPath(filesystem, sysRelativized.toString());
    }


    @Override
    public Path toAbsolutePath() {
        return getRoot().resolve(this);
    }

    @Override
    public Path toRealPath(LinkOption... options) throws IOException {
        return toAbsolutePath();
    }

    @Override
    public WatchKey register(WatchService watcher, WatchEvent.Kind<?>[] events, WatchEvent.Modifier... modifiers) throws IOException {
        return null;
    }

    @Override
    public int compareTo(Path other) {
        int thisSize = getNameCount();
        int thatSize = other.getNameCount();

        int commonIdx = Math.min(thisSize,thatSize);
        for (int i = 0; i < commonIdx; i++) {
            int diff = getName(i).compareTo(other.getName(i));
            if (diff!=0) {
                return diff;
            }
        }
        return Integer.compare(thisSize,thatSize);
    }

    private Path tmpSysPath() {
        return FileSystems.getDefault().getPath(
                (isAbsolute() ? FileSystems.getDefault().getSeparator() : "") + path[0],
                Arrays.copyOfRange(path,1,path.length-1)
        );
    }
    private Path tmpSysPath(MetaPath other) {
        return FileSystems.getDefault().getPath(
                (other.isAbsolute() ? FileSystems.getDefault().getSeparator() : "") + other.path[0],
                Arrays.copyOfRange(other.path,1,other.path.length-1)
        );
    }

    private void assertFilesystemOwnership(Path other) {
        if (other.getFileSystem() != filesystem) {
            throw new InvalidParameterException("This path is from a different filesystem.");
        }
        if (!(other instanceof MetaPath)) {
            throw new InvalidParameterException("This path is not of type MetaPath");
        }
    }

    private String joinedPath() {
        StringBuilder sb = new StringBuilder();
        if (isAbsolute()) {
            sb.append(FileSystems.getDefault().getSeparator());
        }
        for (String s : path) {
            sb.append(s);
            sb.append(FileSystems.getDefault().getSeparator());
        }
        try {
            sb.setLength(sb.length()-1);
        } catch (Exception e) {
            System.out.println(e);
        }
        return sb.toString();
    }



    @Override
    public String toString() {
        return (isAbsolute() ? FileSystems.getDefault().getSeparator() : "") + Arrays.stream(path).collect(Collectors.joining(getFileSystem().getSeparator()));
    }

    @Override
    public URI toUri() {
        try {
            return new URI(filesystem.provider().getScheme(),null,joinedPath(),null);
        } catch (URISyntaxException e) {
            throw new InvalidParameterException("Unable to create URI from " + this +": " + e.getInput());
        }
    }

//    public Path getSysPath() {
//        return filesystem.getSysPath(this);
//    }
}
