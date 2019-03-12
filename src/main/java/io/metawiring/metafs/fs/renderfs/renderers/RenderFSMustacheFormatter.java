package io.metawiring.metafs.fs.renderfs.renderers;

import com.samskivert.mustache.Mustache;
import io.metawiring.metafs.fs.renderfs.api.MarkdownStringer;

public class RenderFSMustacheFormatter implements Mustache.Formatter {

    @Override
    public String format(Object value) {
        if (value instanceof MarkdownStringer) {
            return ((MarkdownStringer) value).asMarkdown();
        } else {
            return value.toString();
        }
    }
}
