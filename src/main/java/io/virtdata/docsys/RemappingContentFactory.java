package io.virtdata.docsys;

import org.eclipse.jetty.http.CompressedContentFormat;
import org.eclipse.jetty.http.HttpContent;
import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.jetty.server.ResourceContentFactory;
import org.eclipse.jetty.util.resource.ResourceFactory;

import java.io.IOException;
import java.nio.file.InvalidPathException;

public class RemappingContentFactory extends ResourceContentFactory {

    private final static String[][] remaps = new String[][]{
            new String[]{"html", "md"}
    };

    public RemappingContentFactory(ResourceFactory factory, MimeTypes mimeTypes, CompressedContentFormat[] precompressedFormats) {
        super(factory, mimeTypes, precompressedFormats);
    }

    @Override
    public HttpContent getContent(String pathInContext, int maxBufferSize) throws IOException {
        HttpContent content = null;
        try {
            content = super.getContent(pathInContext, maxBufferSize);
            return content;
        } catch (InvalidPathException ipe) {
            for (String[] remap : remaps) {
                String from= remap[0];
                String to = remap[1];
                if (pathInContext.endsWith(from)) {
                    String candidate = pathInContext.substring(0,pathInContext.length()-from.length()) + to;
                    try {
                        content = super.getContent(candidate,maxBufferSize);
                        return content;
                    } catch (InvalidPathException ignored) {
                    }
                }
            }
        }
        return content;

    }
}
