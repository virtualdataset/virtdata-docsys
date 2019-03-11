package io.metawiring.metafs.fs.renderfs.api.newness;

import io.metawiring.metafs.fs.renderfs.FileContentRenderer;
import io.metawiring.metafs.fs.renderfs.api.RendererIO;

import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.security.InvalidParameterException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("Duplicates")
public class FileRenderer implements FileContentRenderer {

    private final String sourceExtension;
    private final String targetExtension;
    private final Pattern sourceNamePattern;
    private final Pattern targetNamePattern;
    private final boolean isCaseSensitive;

    private final ConcurrentHashMap<Path, Renderable> renderables = new ConcurrentHashMap<>();
    private TemplateCompiler[] compilers;

    /**
     * Create a file renderer from a source extention to a target extension, which will yield the
     * virtual contents of the target file by applying a set of renderers to the source file data.
     *
     * @param sourceExtension The extension of the source (actual) file, including the dot and extension name.
     * @param targetExtension The extension of the target (virtual) file, including the dot and extension name.
     * @param isCaseSensitive Whether or not to do case-sensitive matching against the source and target extensions.
     * @param compilers        A lookup function which can create a renderer for a specific path as needed.
     */
    public FileRenderer(String sourceExtension, String targetExtension, boolean isCaseSensitive, TemplateCompiler... compilers) {
        this.compilers = compilers;

        if (!sourceExtension.startsWith(".")) {
            throw new InvalidParameterException("You must provide a source extension in '.xyz' form.");
        }
        if (!targetExtension.startsWith(".")) {
            throw new InvalidParameterException("You must provide a target extension in '.xyz' form.");
        }
        this.isCaseSensitive = isCaseSensitive;
        this.sourceExtension = sourceExtension;
        this.sourceNamePattern = toNamePattern(sourceExtension);
        this.targetExtension = targetExtension;
        this.targetNamePattern = toNamePattern(targetExtension);

    }

    private Pattern toNamePattern(String fileExtension) {
        Pattern.compile(fileExtension);
        if (fileExtension.matches("\\.[a-zA-Z0-9]+")) {
            StringBuilder sb = new StringBuilder("(?<basepath>.+)(?<extension>");
            if (isCaseSensitive) {
                sb.append(fileExtension.substring(1));
            } else {
                sb.append("(");
                for (int i = 0; i < fileExtension.length(); i++) {
                    String charString = fileExtension.substring(i, i + 1);
                    if (charString.equals(".")) {
                        sb.append("\\.");
                    } else {
                        sb.append("[").append(charString.toUpperCase()).append(charString.toLowerCase()).append("]");
                    }
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

    @Override
    public String getTargetSuffix() {
        return this.targetExtension;
    }

    @Override
    public ByteBuffer render(Path sourcePath, Path targetPath, ByteBuffer byteBuffer) {
        long lastModified = RendererIO.mtimeFor(sourcePath);

        Renderable renderable = renderables.get(targetPath);
        if (renderable == null) {
            if (compilers.length==0) {
                renderable = new RenderableEntry(() -> RendererIO.readBuffer(sourcePath), compilers[0]);
            } else {
                renderable = new RenderableChain(() -> RendererIO.readBuffer(sourcePath), compilers);
            }
            renderables.put(targetPath, renderable);
        }
        return renderable.apply(new TargetPathView(targetPath, lastModified));
    }

}
