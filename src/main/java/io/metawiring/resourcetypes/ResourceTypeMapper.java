package io.metawiring.resourcetypes;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class ResourceTypeMapper {

    private final static Map<Pattern,ResourceTransformer> transformers = new LinkedHashMap<>();
    static {
        transformers.put(Pattern.compile(".*\\.(md|MD)$"),new MarkdownTransformer());
    }

    private ResourceTypeMapper() { }

    public static ResourceTypeMapper get() {
        return instance;
    }

    public ResourceTransformer forNamePattern(String name) {
        for (Map.Entry<Pattern, ResourceTransformer> transformer : transformers.entrySet()) {
            if (transformer.getKey().matcher(name).matches()) {
                return transformer.getValue();
            }
        }
        return null;
    }

    private static ResourceTypeMapper instance = new ResourceTypeMapper();


}
