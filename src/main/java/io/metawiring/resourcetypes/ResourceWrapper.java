package io.metawiring.resourcetypes;

import org.eclipse.jetty.util.resource.Resource;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

public class ResourceWrapper extends Resource {

    private final Resource resource;
    private final ResourceTransformer renderer;
    private ByteBuffer rendered;

    public ResourceWrapper(Resource resource) {
        this.resource=resource;
        this.renderer=null;
    }

    public ResourceWrapper(Resource resource, ResourceTransformer renderer) {
        this.resource = resource;
        this.renderer = renderer;
    }

    private synchronized ByteBuffer getRendered() {
        if (rendered==null && isTransformable()) {
            try {
                byte[] bytes = resource.getInputStream().readAllBytes();
                ByteBuffer buf = ByteBuffer.wrap(bytes);
                rendered = renderer.getContentTransformer().apply(buf);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            throw new RuntimeException("This method should not be called when the resource it not transformable.");
        }
        return rendered;
    }

    @Override
    public boolean isContainedIn(Resource r) throws MalformedURLException {
        return resource.isContainedIn(r);
    }

    @Override
    public void close() {
        resource.close();
    }

    @Override
    public boolean exists() {
        return resource.exists();
    }

    @Override
    public boolean isDirectory() {
        return resource.isDirectory();
    }

    @Override
    public long lastModified() {
        return resource.lastModified();
    }

    @Override
    public long length() {
        if (isTransformable()) {
            return getRendered().remaining();
        } else {
            return resource.length();
        }
    }

    @Override
    public URL getURL() {
        return resource.getURL();
    }

    @Override
    public File getFile() throws IOException {
        if (isTransformable()) {
            return null;
        }
        return resource.getFile();
    }

    @Override
    public String getName() {
        if (isTransformable()) {
            return this.renderer.getNameTransformer().apply(resource.getName());
        }
        return resource.getName();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        if (isTransformable()) {
            ByteBuffer rendered = getRendered();
            byte[] array = rendered.array();
            return new ByteArrayInputStream(array);
        }
        return resource.getInputStream();
    }

    @Override
    public ReadableByteChannel getReadableByteChannel() throws IOException {
        if (isTransformable()) {
            return null;
        }
        return resource.getReadableByteChannel();
    }

    @Override
    public boolean delete() throws SecurityException {
        if (isTransformable()) {
            return false;
        }
        return resource.delete();
    }

    @Override
    public boolean renameTo(Resource dest) throws SecurityException {
        if (isTransformable()) {
            return false;
        }
        return resource.renameTo(dest);
    }

    @Override
    public String[] list() {
        if (isTransformable()) {
            return new String[0];
        }
        return resource.list();
    }

    @Override
    public Resource addPath(String path) throws IOException, MalformedURLException {
        Resource foundResource = this.resource.addPath(path);
        if (foundResource==null) {
            throw new IOException("Unable to find child resource " + path.toString() + " for " + this.toString());
        }
        ResourceTransformer resourceTransformer = ResourceTypeMapper.get().forNamePattern(foundResource.getName());
        return new ResourceWrapper(foundResource,resourceTransformer);
    }

    protected boolean isTransformable() {
        return (renderer!=null);
    }
}
