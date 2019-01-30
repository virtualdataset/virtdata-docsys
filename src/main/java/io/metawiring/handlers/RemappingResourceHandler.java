package io.metawiring.handlers;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class RemappingResourceHandler extends ResourceHandler {
    private final static Logger logger = LoggerFactory.getLogger(RemappingResourceHandler.class);

    private String[][] remaps = new String[][] {
            new String[] {"html", "md"}
    };

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

        // Already handled? Nothing to do here.
        if (baseRequest.isHandled() || response.isCommitted()) {
            return;
        }

        // Handle by default logic? nothing to do here?
        super.handle(target, baseRequest, request, response);
        if (baseRequest.isHandled() || response.isCommitted()) {
            return;
        }

        // ELSE, maybe look elsewhere!
        for (String[] remap : remaps) {
            String from = remap[0];
            String to = remap[1];

            if (target.endsWith(from)) {
                String candidate = target.substring(0,target.length()-from.length()) + to;
                String pathInfo = request.getPathInfo();
                String candidatePathInfo = pathInfo.substring(0,pathInfo.length()-from.length()) + to;
                baseRequest.setPathInfo(candidatePathInfo);
                super.handle(candidate, baseRequest, request, response);
                if (baseRequest.isHandled() || response.isCommitted()) {
                    logger.debug("Handled " + target + " with alternate resource path: " + candidate);
//                    baseRequest.setPathInfo(pathInfo);
                    return;
                }
            }
            logger.debug("Unable to handle " + target + " with any alternate resource path.");
        }

    }

}
