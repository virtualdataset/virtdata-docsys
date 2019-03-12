package io.virtdata.docsys;

import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public class DocParser {

    public HelpTopic parse(String filepath) {
        try {
            Path path = Paths.get(filepath);
            byte[] bytes = Files.readAllBytes(path);
            String data = new String(bytes, StandardCharsets.UTF_8);
            return parse(path, data);
        } catch (IOException e) {
            throw new RuntimeException("error reading " + filepath + ": " + e);
        }
    }

    public HelpTopic parse(Path path, String filedata) {
        if (filedata.startsWith("---\n")) {
            String[] sections = filedata.split("---\n", 3);
            Yaml yaml = new Yaml();
            StringReader dataReader = new StringReader(filedata);
            Map<String,Object> meta = yaml.load(sections[1]);
            return new HelpTopic(path,sections[2],meta);

        } else {
            throw new RuntimeException("implement me!");
        }
    }
}
