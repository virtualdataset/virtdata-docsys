package io.metawiring;

import io.metawiring.handlers.FavIconHandler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.*;
import org.eclipse.jetty.util.resource.PathResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public class DocServer implements Runnable{

    private final static Logger logger = LoggerFactory.getLogger(DocServer.class);

    private String bindHost = "localhost";
    private int bindPort = 12345;

    private Path basePath;

    public DocServer(Path basePath, String bindHost, int bindPort) {
        this.basePath=basePath;
        this.bindHost = bindHost;
        this.bindPort = bindPort;
    }

    public DocServer(Path basePath) {
        this.basePath = basePath;
    }

    public void run() {

        Server server = new Server(bindPort);
        HandlerList handlers = new HandlerList();


        // Handle Static Resources
        ResourceHandler resourceHandle = new ResourceHandler();
        resourceHandle.setDirAllowed(false);
        resourceHandle.setAcceptRanges(false);
        resourceHandle.setBaseResource(new PathResource(basePath));
        logger.info("Setting root path of server: " + basePath.toString());
        handlers.addHandler(resourceHandle);


//        // Debug
//        DebugListener debugListener = new DebugListener();

//        If needed to limit to local only
//        InetAccessHandler handler = new InetAccessHandler();
//        handler.include("127.0.0.1");

        InetAccessHandler accessHandler;
        ShutdownHandler shutdownHandler; // for easy recycles

        FavIconHandler favIconHandler = new FavIconHandler("/favicon.ico", false);
        handlers.addHandler(favIconHandler);

        // Make a dedicated Favicon handler

        server.setHandler(handlers);
        try {
            server.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
