/*
 * This file is part of BrickQuotes. It is subject to the license terms in
 * the LICENSE file found in the top-level directory of this distribution.
 * You may not use this file except in compliance with the License.
 */
package de.dfki.resc28.brickquotes;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import static org.apache.commons.lang.StringEscapeUtils.escapeHtml;

/**
 *
 * @author Dmitri Rubinstein
 */
@Provider
public class WebAppExceptionMapper implements ExceptionMapper<WebApplicationException> {

    // 1 - title
    // 2 - htmlMessage
    // 3 - status code
    // 4 - status phrase
    // 5 - application name
    static final String HTML_TEMPLATE = "<!DOCTYPE html>\n"
            + "<html>\n"
            + "<head><title>%1$s</title>\n"
            + "    <style type=\"text/css\">\n"
            + "    H1 {\n"
            + "        font-family: Tahoma, Arial, sans-serif;\n"
            + "        color: white;\n"
            + "        background-color: #9f3a41;\n"
            + "        font-size: 22px;\n"
            + "    }\n"
            + "\n"
            + "    H2 {\n"
            + "        font-family: Tahoma, Arial, sans-serif;\n"
            + "        color: white;\n"
            + "        background-color: #9f3a41;\n"
            + "        font-size: 16px;\n"
            + "    }\n"
            + "\n"
            + "    H3 {\n"
            + "        font-family: Tahoma, Arial, sans-serif;\n"
            + "        color: white;\n"
            + "        background-color: #525D76;\n"
            + "        font-size: 14px;\n"
            + "    }\n"
            + "\n"
            + "    BODY {\n"
            + "        font-family: Tahoma, Arial, sans-serif;\n"
            + "        color: black;\n"
            + "        background-color: white;\n"
            + "    }\n"
            + "\n"
            + "    B {\n"
            + "        font-family: Tahoma, Arial, sans-serif;\n"
            + "        color: white;\n"
            + "        background-color: #525D76;\n"
            + "    }\n"
            + "\n"
            + "    P {\n"
            + "        font-family: Tahoma, Arial, sans-serif;\n"
            + "        background: white;\n"
            + "        color: black;\n"
            + "        font-size: 12px;\n"
            + "    }\n"
            + "\n"
            + "    A {\n"
            + "        color: black;\n"
            + "    }\n"
            + "\n"
            + "    A.name {\n"
            + "        color: black;\n"
            + "    }\n"
            + "\n"
            + "    .line {\n"
            + "        height: 1px;\n"
            + "        background-color: #525D76;\n"
            + "        border: none;\n"
            + "    }</style>\n"
            + "</head>\n"
            + "<body>\n"
            + "<h1>HTTP Status %3$d - %4$s</h1>\n"
            + "<div class=\"line\"></div>\n"
            + "<h1>%1$s</h1>\n"
            + "<pre>%2$s</pre>\n"
            + "<hr class=\"line\">\n"
            + "<h3>%5$s</h3>\n"
            + "</body>\n"
            + "</html>";

    @Override
    public Response toResponse(WebApplicationException ex) {
        final String appName = "BrickQuotes";
        final Response response = ex.getResponse();
        final String htmlMessage = escapeHtml(ex.getMessage());
        final String entity = String.format(HTML_TEMPLATE,
                appName + " - Error Report",
                htmlMessage,
                response.getStatus(),
                response.getStatusInfo().getReasonPhrase(),
                appName);
        return Response.status(ex.getResponse().getStatus())
                .entity(entity).type(MediaType.TEXT_HTML).build();
    }
}
