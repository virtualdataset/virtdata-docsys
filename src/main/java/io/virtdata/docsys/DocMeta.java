package io.virtdata.docsys;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class DocMeta {

    private String name;
    private Path path;
    private Map<String,Object> meta = new HashMap<>();
    private String content;

    public DocMeta(Path path, String content, Map<String,Object> meta) {
        this.name = path.getFileName().toString();
        this.path = path;
        this.content = content;
        this.meta = meta;
    }

    public String asMarkdown() {
        return content;
    }

    public String asHTML() {
        return content;
    }

}