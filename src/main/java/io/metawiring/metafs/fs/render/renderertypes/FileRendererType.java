package io.metawiring.metafs.fs.render.renderertypes;

import io.metawiring.metafs.fs.render.FileContentRenderer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("Duplicates")
public abstract class FileRendererType implements FileContentRenderer {

    private final String sourceExtension;
    private final String targetExtension;
    private final Pattern sourceNamePattern;
    private final Pattern targetNamePattern;
    private final boolean isCaseSensitive;

    public FileRendererType(String sourceExtension, String targetExtension, boolean isCaseSensitive) {
        this.isCaseSensitive = isCaseSensitive;
        this.sourceExtension = sourceExtension;
        this.sourceNamePattern = toNamePattern(sourceExtension);
        this.targetExtension = targetExtension;
        this.targetNamePattern = toNamePattern(targetExtension);
    }

    private Pattern toNamePattern(String fileExtension) {
        Pattern.compile(fileExtension);
        if (fileExtension.matches("[a-zA-Z0-9]+")) {
            StringBuilder sb = new StringBuilder("(?<basepath>.+\\.)(?<extension>");
            if (isCaseSensitive) {
                sb.append(fileExtension);
            } else {
                sb.append("(");
                for (int i = 0; i < fileExtension.length() - 1; i++) {
                    String charString = fileExtension.substring(i, i + 1);
                    sb.append(charString.toUpperCase());
                    sb.append("|");
                    sb.append(charString.toLowerCase());
                }
                sb.append(")");
            }
            sb.append(")");
            return Pattern.compile(sb.toString());
        } else {
            throw new RuntimeException("Invalid extension pattern '" + fileExtension + "'. This must be all letters or numbers.");
        }
    }

    @Override
    public Pattern getSourcePattern() {
        return sourceNamePattern;
    }

    @Override
    public Pattern getTargetPattern() {
        return targetNamePattern;
    }

    public String getSourceExtension() {
        return sourceExtension;
    }

    public String getTargetExtension() {
        return targetExtension;
    }

    @Override
    public Path getSourcePath(Path targetName) {
        Matcher matcher = targetNamePattern.matcher(targetName.toString());
        if (matcher.matches()) {
            String basepath = matcher.group("basepath");
            String extension = matcher.group("extension");
            if (basepath == null || extension == null) {
                throw new RuntimeException(
                        "Unable to extract named fields 'basepath' or 'extension' from target " +
                                "name '" + targetName + "' with pattern '" + targetNamePattern + "'");
            }
            return targetName.getFileSystem().getPath(basepath + sourceExtension);

        }
        return null;

    }

    @Override
    public Path getRenderedTargetName(Path sourceName) {
        Matcher matcher = sourceNamePattern.matcher(sourceName.toString());
        if (matcher.matches()) {
            String basepath = matcher.group("basepath");
            String extension = matcher.group("extension");
            if (basepath == null || extension == null) {
                throw new RuntimeException(
                        "Unable to extract named fields 'basepath' or 'extension' from source " +
                                "name '" + sourceName + "' with pattern '" + sourceNamePattern + "'");
            }

            return sourceName.getFileSystem().getPath(basepath + targetExtension);
        }
        throw new RuntimeException("Unable to match source name '" + sourceName + "' with pattern '" + sourceNamePattern + "'");

    }

    public ByteBuffer getRenderedContent(Path sourceName) {
        try {
            byte[] bytes = Files.readAllBytes(sourceName);
            return apply(ByteBuffer.wrap(bytes));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public String getTargetSuffix() {
        return this.targetExtension;
    }

    @Override
    public final ByteBuffer apply(ByteBuffer byteBuffer) {
        byteBuffer = transform(byteBuffer);
        return byteBuffer;
    }

    public ByteBuffer transform(ByteBuffer buffer) {
        byte[] buf = new byte[buffer.remaining()];
        buffer.get(buf);
        String rawMarkdown = new String(buf, StandardCharsets.UTF_8);
        String result = transform(rawMarkdown);
        ByteBuffer htmlBytes = ByteBuffer.wrap(result.getBytes(StandardCharsets.UTF_8));
        return htmlBytes;

    }


    public String transform(String string) {
        return string;
    }

    protected boolean isCaseSensitive() {
        return isCaseSensitive;
    }
}