package io.metawiring.handlers;

import com.openhtmltopdf.resource.AbstractResource;

import java.io.Reader;

public class MarkdownResource extends AbstractResource {

    public MarkdownResource(Reader reader) {
        super(reader);
    }

}
