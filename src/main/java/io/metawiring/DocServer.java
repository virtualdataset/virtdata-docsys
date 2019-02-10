package io.metawiring;

import io.metawiring.handlers.FavIconHandler;
import io.metawiring.metafs.layer.LayerFS;
import io.metawiring.metafs.render.RenderFS;
import io.metawiring.metafs.render.renderertypes.MarkdownRenderer;
import io.metawiring.metafs.virtual.VirtFS;
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


        LayerFS layers = new LayerFS();
        layers.addLayer(new VirtFS(basePath));
        RenderFS renderer = new RenderFS(layers);
        renderer.addRenderer(new MarkdownRenderer());

//        VirtFS fs = new VirtFS(virtFSProvider, basePath.toUri(),)
//        Path path = fs.getPath("/");

        // Handle Static Resources
        Resource baseResource = new PathResource(renderer.getRootPath());
        logger.info("Setting root path of server: " + baseResource.toString());
        ResourceHandler resourceHandler = new ResourceHandler();
        resourceHandler.setDirAllowed(true);
        resourceHandler.setAcceptRanges(true);
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
