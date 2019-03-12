package io.virtdata.docsys.handlers;

import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.util.IO;
import org.eclipse.jetty.util.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.security.InvalidParameterException;

public class FavIconHandler extends AbstractHandler {
    private final static Logger logger = LoggerFactory.getLogger(FavIconHandler.class);
    private final Path faviconPath;

    byte[] iconData;
    private long lastModified = 0L;

    public FavIconHandler(String faviconPath, boolean requireFile) {
        this.faviconPath = Path.of(faviconPath);

        if (!this.faviconPath.toFile().exists() && requireFile) {
            throw new InvalidParameterException("favicon faviconPath " + this.faviconPath.toString() + " does not exist.");
        }

    }

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

        if (baseRequest.isHandled() || response.isCommitted()) {
            return;
        }

        if (!HttpMethod.GET.is(request.getMethod()) || !target.equals("/favicon.ico")) {
            return;
        }

        if (!this.faviconPath.toFile().exists()) {
            return;
        }

        lastModified = faviconPath.toFile().lastModified();

        if (request.getDateHeader(HttpHeader.IF_MODIFIED_SINCE.toString()) == lastModified) {
            response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
            return;
        }

        baseRequest.setHandled(true);

        URL fav = this.getClass().getClassLoader().getResource("org/eclipse/jetty/favicon.ico");
        if (fav != null) {
            Resource r = Resource.newResource(fav);
            iconData = IO.readBytes(r.getInputStream());
        }

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("image/x-icon");
        response.setContentLength(iconData.length);
        response.setDateHeader(HttpHeader.LAST_MODIFIED.toString(), lastModified);
        response.setHeader(HttpHeader.CACHE_CONTROL.toString(), "max-age=360000,public");
        response.getOutputStream().write(iconData);


    }
}
