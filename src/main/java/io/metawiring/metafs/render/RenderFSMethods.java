package io.metawiring.metafs.render;

public class RenderFSMethods {
//    private final static Logger logger = LoggerFactory.getLogger(RenderFSMethods.class);
//
//    private static List<FileContentRenderer> transformers = new ArrayList<>();
//    static {
//        transformers.add(new MarkdownFileContentTransformer.MarkdownFileContentRenderer());
//    }
//
//    public DirectoryStream<Path> transformDirectoryStream(DirectoryStream<Path> upstream) {
//        List<Path> paths = new ArrayList<>();
//        for (Path path : paths) {
//            for (FileContentRenderer t : transformers) {
//                if (t.matchesSource(path)) {
//                    Path substitute = t.getSourceTargetMapper().apply(path);
//                    paths.add(substitute);
//                    logger.debug("substituted " + substitute + " for previous " + path);
//                } else {
//                    paths.add(path);
//                }
//            }
//        }
//
//        return new DirectoryStream<>() {
//            @Override
//            public Iterator<Path> iterator() {
//                return paths.iterator();
//            }
//            @Override
//            public void close() throws IOException {
//
//            }
//        };
//    }
//
//    private RenderedFileContent maybeTransform(Path path) {
//
//        for (FileContentRenderer t: transformers) {
//            if (t.matchesSource(path)) {
//                RenderedFileContent transformed = t.getFileTransformer().apply(path);
//                return transformed;
//            }
//        }
//        return null;
//    }
//
//
//
//    public <A extends BasicFileAttributes> A maybeReadSourceAttributes(Path path, Class<A> type, LinkOption[] options) throws IOException {
//        for (FileContentRenderer transformer : transformers) {
//            if (transformer.matchesTarget(path)) {
//                Path sourceNameForTarget = FileSystems.getDefault().getPath(transformer.getTargetSourceMapper().apply(path).toString());
//                A attributes = FileSystems.getDefault().provider().readAttributes(sourceNameForTarget, type, options);
//                Function<Path, RenderedFileContent> fileTransformer = transformer.getFileTransformer();
//                RenderedFileContent applied = fileTransformer.apply(path);
//                ByteBuffer rendered = applied.getRenderedContent();
//                int remaining = rendered.remaining();
//                return (A) new RenderedFileAttributes(attributes, remaining);
//            }
//        }
//        return null;
//    }
//
//    public Map<String, Object> maybeReadSourceAttributes(Path path, String attributes, LinkOption... options) throws IOException {
//        for (FileContentRenderer transformer : transformers) {
//            if (transformer.matchesTarget(path)) {
//                Path sourceNameForTarget = FileSystems.getDefault().getPath(transformer.getTargetSourceMapper().apply(path).toString());
//                return FileSystems.getDefault().provider().readAttributes(sourceNameForTarget, attributes, options);
//            }
//        }
//        return null;
//    }
//
//    public void maybeCheckAccess(Path getContainerPath) throws IOException {
//        for (FileContentRenderer transformer : transformers) {
//            if (transformer.matchesTarget(getContainerPath)) {
//                Path sourceNameForTarget= FileSystems.getDefault().getPath(transformer.getTargetSourceMapper().apply(getContainerPath).toString());
//                FileSystems.getDefault().provider().checkAccess(sourceNameForTarget);
//            }
//        }
//    }
}
