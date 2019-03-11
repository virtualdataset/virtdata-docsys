package io.metawiring;

import io.metawiring.handlers.FavIconHandler;
import io.metawiring.metafs.fs.renderfs.RenderFS;
import io.metawiring.metafs.fs.renderfs.api.newness.FileRenderer;
import io.metawiring.metafs.fs.renderfs.renderers.MarkdownProcessor;
import io.metawiring.metafs.fs.renderfs.renderers.MustacheProcessor;
import io.metawiring.metafs.fs.virtual.VirtFS;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.util.resource.PathResource;
import org.eclipse.jetty.util.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public class DocServer implements Runnable {

    private final static Logger logger = LoggerFactory.getLogger(DocServer.class);

    private String bindHost = "localhost";
    private int bindPort = 12345;

    private Path basePath;

    public DocServer(Path basePath, String bindHost, int bindPort) {
        this.basePath = basePath;
        this.bindHost = bindHost;
        this.bindPort = bindPort;
    }

    public DocServer(Path basePath) {
        this.basePath = basePath;
    }

    public void run() {

        //new InetSocketAddress("")
        Server server = new Server(bindPort);
        HandlerList handlers = new HandlerList();

        //        // Debug
//        DebugListener debugListener = new DebugListener();
//        If needed to limit to local only
//        InetAccessHandler handler = new InetAccessHandler();
//        handler.include("127.0.0.1");
//        InetAccessHandler accessHandler;
//        ShutdownHandler shutdownHandler; // for easy recycles

        // Favicon
        FavIconHandler favIconHandler = new FavIconHandler(basePath + "/favicon.ico", true);
        handlers.addHandler(favIconHandler);


//        URI vfsRoot;
//        try {
//            vfsRoot = new URI("meta",null,basePath.toString(),null,null);
//        } catch (URISyntaxException e) {
//            throw new RuntimeException(e);
//        }
//        VirtFS fs = new VirtFS(metaFSProvider, vfsRoot, new HashMap<>());


//        LayerFS layers = new LayerFS();
//        layers.addLayer(new VirtFS(basePath));

//        DefaultRendererResolver rendererResolver = new DefaultRendererResolver(RendererIO::readBuffer, MustacheRenderer::new, MarkdownRenderer::new);
//        new FileRenderer(".md",".html",false, rendererResolver);
//
//
        VirtFS vfs = new VirtFS(basePath);
        RenderFS mvelFS = new RenderFS(vfs);

        MustacheProcessor msp = new MustacheProcessor();
        MarkdownProcessor mdp = new MarkdownProcessor();
        FileRenderer htmlRenderer = new FileRenderer(".md", ".html", false, msp, mdp);
        mvelFS.addRenderer(htmlRenderer);

        // Handle Static Resources

        Resource baseResource = new PathResource(mvelFS.getRootPath());
        logger.info("Setting root path of server: " + baseResource.toString());
        ResourceHandler resourceHandler = new ResourceHandler();
        resourceHandler.setDirAllowed(true);
        resourceHandler.setAcceptRanges(true);
        //baseResource=new PathResource(basePath);
        resourceHandler.setBaseResource(baseResource);
        handlers.addHandler(resourceHandler);


        // Show contexts
        DefaultHandler defaultHandler = new DefaultHandler();
        defaultHandler.setShowContexts(true);
        defaultHandler.setServeIcon(false);
        handlers.addHandler(defaultHandler);


        server.setHandler(handlers);
        try {
            server.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
