package io.virtdata.docsys.core;

import io.virtdata.docsys.api.DocSystemEndpoint;
import io.virtdata.docsys.handlers.EndpointsHandler;
import io.virtdata.docsys.handlers.FavIconHandler;
import io.virtdata.docsys.metafs.fs.layerfs.LayerFS;
import io.virtdata.docsys.metafs.fs.renderfs.api.FileRenderer;
import io.virtdata.docsys.metafs.fs.renderfs.fs.RenderFS;
import io.virtdata.docsys.metafs.fs.renderfs.renderers.MarkdownProcessor;
import io.virtdata.docsys.metafs.fs.renderfs.renderers.MarkdownProcessorDebugger;
import io.virtdata.docsys.metafs.fs.renderfs.renderers.MustacheProcessor;
import io.virtdata.docsys.metafs.fs.virtual.VirtFS;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.PathResource;
import org.eclipse.jetty.util.resource.Resource;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DocServer implements Runnable {

    private final static Logger logger = LoggerFactory.getLogger(DocServer.class);
    private final List<Path> basePaths = new ArrayList<>();
    private final List<Object> servletObjects = new ArrayList<>();
    private ServletContextHandler contextHandler;
    private ServletHolder servlets;
    private String bindHost = "localhost";
    private int bindPort = 12345;

    public DocServer withHost(String bindHost) {
        this.bindHost = bindHost;
        return this;
    }

    public DocServer withPort(int bindPort) {
        this.bindPort = bindPort;
        return this;
    }

    public DocServer addWebObject(Object... objects) {
        servletObjects.addAll(Arrays.asList(objects));
        getServlets().setInitParameter(
                "jersey.config.server.provider.classnames",
                this.servletObjects.stream().map(o -> o.getClass().getCanonicalName()).collect(Collectors.joining(","))
        );
        return this;
    }

    private ServletContextHandler getContextHandler() {
        if (contextHandler == null) {
            contextHandler = new ServletContextHandler();
            contextHandler.setContextPath("/");
        }
        return contextHandler;
    }

    private ServletHolder getServlets() {
        if (servlets == null) {
            servlets = getContextHandler().addServlet(
                    ServletContainer.class,
                    "/*"
            );
        }
        return servlets;
    }

    public DocServer addPaths(Path... paths) {
        this.basePaths.addAll(Arrays.asList(paths));
        return this;
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
        FavIconHandler favIconHandler =
                new FavIconHandler(basePaths.get(0) + "/favicon.ico", false);
        handlers.addHandler(favIconHandler);

//        // Hook dynamic endpoints in process, not in fs layers
        EndpointsHandler endpointsHandler = new EndpointsHandler();
        handlers.addHandler(endpointsHandler);


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
        LayerFS layerfs = new LayerFS();

        for (Path basePath : basePaths) {
            VirtFS vfs = new VirtFS(basePath);
            RenderFS rfs = new RenderFS(vfs);

            MustacheProcessor msp = new MustacheProcessor();
            MarkdownProcessor mdp = new MarkdownProcessor();
            MarkdownProcessorDebugger mdd = new MarkdownProcessorDebugger();

            FileRenderer htmlRenderer = new FileRenderer(".md", ".html", false, msp, mdp);
            rfs.addRenderer(htmlRenderer);

            MustacheProcessor ms_html = new MustacheProcessor();
            FileRenderer mustacheToHtmlRenderer = new FileRenderer(
                    ".mustache_html", ".html", false, ms_html
            );
            rfs.addRenderer(mustacheToHtmlRenderer);

            MustacheProcessor ms_md = new MustacheProcessor();
            FileRenderer mustacheToMarkdown = new FileRenderer(
                    ".mustache_md", ".md", false, ms_md
            );
            rfs.addRenderer(mustacheToMarkdown);
            //            FileRenderer debugRenderer = new FileRenderer(".md", ".mustache", false, msp);
//            rfs.addRenderer(debugRenderer);

//            FileRenderer mdparserRenderer = new FileRenderer(".md", ".mdparse", false, mdd);
//            rfs.addRenderer(mdparserRenderer);
            // Handle Static Resources

            layerfs.addLayer(rfs);
        }

        Resource baseResource = new PathResource(layerfs.getRootPath());
//        Resource baseResource = new PathResource(basePath);

        logger.info("Setting root path of server: " + baseResource.toString());
        ResourceHandler resourceHandler = new ResourceHandler();
        resourceHandler.setDirAllowed(true);
        resourceHandler.setAcceptRanges(true);
        //baseResource=new PathResource(basePath);

        resourceHandler.setWelcomeFiles(new String[]{"index.html"});
        resourceHandler.setRedirectWelcome(true);
        resourceHandler.setBaseResource(baseResource);
        handlers.addHandler(resourceHandler);

        // Show contexts
        DefaultHandler defaultHandler = new DefaultHandler();
        defaultHandler.setShowContexts(true);
        defaultHandler.setServeIcon(false);

        List<DocSystemEndpoint> autoendpoints = EndpointLoader.load();
        autoendpoints.forEach(e -> {
            if (!servletObjects.contains(e)) servletObjects.add(e);
        });

        if (this.servletObjects.size()>0) {
            logger.info("adding " + servletObjects.size() + " context handlers");
            handlers.addHandler(getContextHandler());
        } else {
            logger.info("No context handlers defined, not adding context container.");
        }

        handlers.addHandler(defaultHandler);

        server.setHandler(handlers);
        try {
            server.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
