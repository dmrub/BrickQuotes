/*
 * This file is part of BrickQuotes. It is subject to the license terms in
 * the LICENSE file found in the top-level directory of this distribution.
 * You may not use this file except in compliance with the License.
 */
package de.dfki.resc28.brickquotes;

import java.io.IOException;
import java.net.URI;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.UriBuilder;

/**
 * A pre-matching filter that detects reverse proxy X-* headers and sets the
 * request URI accordingly.
 *
 * https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/X-Forwarded-Host
 * https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/X-Forwarded-Proto
 * https://docs.aws.amazon.com/elasticloadbalancing/latest/classic/x-forwarded-headers.html
 *
 */
@PreMatching
public class ForwardedHeaderFilter implements ContainerRequestFilter {

    @Override
    public void filter(final ContainerRequestContext requestContext)
            throws IOException {

        String xForwardedProto = requestContext
                .getHeaderString("x-forwarded-proto");
        if (xForwardedProto == null) {
            xForwardedProto = requestContext
                    .getHeaderString("x-forwarded-protocol");
            if (xForwardedProto == null) {
                xForwardedProto = requestContext
                        .getHeaderString("x-url-scheme");
                if (xForwardedProto == null) {
                    xForwardedProto = requestContext
                            .getHeaderString("x-scheme");

                    if (xForwardedProto == null) {
                        if ("on".equalsIgnoreCase(
                                requestContext
                                        .getHeaderString("x-forwarded-ssl"))
                                || "on".equalsIgnoreCase(
                                        requestContext
                                                .getHeaderString("front-end-https"))) {
                            xForwardedProto = "https";
                        }
                    }
                }
            }
        }

        final String xForwardedPort = requestContext
                .getHeaderString("x-forwarded-port");
        final String xForwardedHost = requestContext
                .getHeaderString("x-forwarded-host");

        System.out.format(
                "X-Forwarded-Proto: %s, "
                + "X-Forwarded-Host: %s "
                + "X-Forwarded-Port: %s%n",
                xForwardedProto, xForwardedHost, xForwardedPort);

        final UriBuilder reqUriB = requestContext.getUriInfo().getRequestUriBuilder();
        final UriBuilder baseUriB = requestContext.getUriInfo().getBaseUriBuilder();

        boolean uriChanged = false;

        if (xForwardedProto != null) {
            reqUriB.scheme(xForwardedProto);
            baseUriB.scheme(xForwardedProto);
            uriChanged = true;
        }

        if (xForwardedHost != null) {
            reqUriB.host(xForwardedHost);
            baseUriB.host(xForwardedHost);
            uriChanged = true;
        }

        if (xForwardedPort != null) {
            try {
                int port = Integer.parseUnsignedInt(xForwardedPort);
                reqUriB.port(port);
                baseUriB.port(port);
                uriChanged = true;
            } catch (NumberFormatException ex) {
                // ignore
            }
        }

        if (uriChanged) {
            final URI reqUri = reqUriB.build();
            final URI baseUri = baseUriB.build();
            System.out.format("set request uri to baseUri: %s reqUri: %s%n", baseUri, reqUri);

            requestContext.setRequestUri(baseUri, reqUri);
        }

    }
}
