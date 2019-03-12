package io.virtdata.docsys;

import org.testng.annotations.Test;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

@Test
public class DocParserTest {

    public void loadBasicDoc() {
        try {
            ClassLoader cl = this.getClass().getClassLoader();
            URL r1 = cl.getResource("testsite1/basics/section1/topic1.md");
            Path p1 = Paths.get(r1.toURI());
            DocParser parser = new DocParser();
            parser.parse(p1.toString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}