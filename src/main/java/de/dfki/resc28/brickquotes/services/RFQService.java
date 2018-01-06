/*
 * This file is part of BrickQuotes. It is subject to the license terms in
 * the LICENSE file found in the top-level directory of this distribution.
 * You may not use this file except in compliance with the License.
 */
package de.dfki.resc28.brickquotes.services;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.XSD;

import de.dfki.resc28.brickquotes.constants.MIME;
import de.dfki.resc28.brickquotes.vocabularies.GR;
import java.util.List;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Variant;

@Path("")
public class RFQService {

    @GET
    @Path("/")
    @Produces({MediaType.TEXT_HTML})
    public String index(@Context UriInfo uriInfo) {
        return "<!DOCTYPE html>\n"
                + "<html>\n"
                + "<head>\n"
                + "    <title>BrickQuotes Service</title>\n"
                + "    <style>\n"
                + "        table {\n"
                + "            font-family: arial, sans-serif;\n"
                + "            border-collapse: collapse;\n"
                + "            width: 100%;\n"
                + "        }\n"
                + "\n"
                + "        td, th {\n"
                + "            border: 1px solid #dddddd;\n"
                + "            text-align: left;\n"
                + "            padding: 8px;\n"
                + "        }\n"
                + "\n"
                + "    </style>\n"
                + "</head>\n"
                + "<body>\n"
                + "<h2>BrickQuotes Service</h2>\n"
                + "<p><b>Service Base URI:</b><pre>" + uriInfo.getAbsolutePath() + "</pre></p>\n"
                + "<table>\n"
                + "    <caption><h3>Available REST API</h3></caption>\n"
                + "    <tr>\n"
                + "        <th>Path</th>\n"
                + "        <th>Description</th>\n"
                + "    </tr><tr><td>/brick/<b><i>brickID</i></b></td><td>Get brick information</td></tr>\n"
                + "    <tr><td>/minifig/<b><i>minifigID</i></b></td><td>Get minifig information</td></tr>\n"
                + "</table>\n"
                + "</body>\n"
                + "</html>";
    }

    @GET
    @Path("/brick/{id}")
    public Response quoteBrick(@PathParam("id") String brickID, @Context Request request) {

        final List<Variant> defaultVariant = Variant.mediaTypes(MediaType.valueOf(MIME.CT_TEXT_TURTLE)).build();
        Variant bestVariant = request.selectVariant(defaultVariant);
        if (bestVariant == null) {
            final List<Variant> reqVariants = Variant.mediaTypes(
                    MediaType.valueOf(MIME.CT_APPLICATION_JSON_LD),
                    MediaType.valueOf(MIME.CT_APPLICATION_NQUADS),
                    MediaType.valueOf(MIME.CT_APPLICATION_NTRIPLES),
                    MediaType.valueOf(MIME.CT_APPLICATION_RDF_JSON),
                    MediaType.valueOf(MIME.CT_APPLICATION_RDFXML),
                    MediaType.valueOf(MIME.CT_APPLICATION_TRIX),
                    MediaType.valueOf(MIME.CT_APPLICATION_XTURTLE),
                    MediaType.valueOf(MIME.CT_TEXT_N3),
                    MediaType.valueOf(MIME.CT_TEXT_TRIG),
                    MediaType.valueOf(MIME.CT_TEXT_TURTLE)
            ).build();

            bestVariant = request.selectVariant(reqVariants);
            if (bestVariant == null) {
                /* Based on results, the optimal response variant can not be determined from the list given.  */
                return Response.notAcceptable(reqVariants).build();
            }
        }
        final MediaType responseMediaType = bestVariant.getMediaType();

        try {
            URL brickRFQ = new URL(String.format("http://www.bricklink.com/ajax/clone/search/searchproduct.ajax?q=%s&st=0&cond=&type=P&cat=&yf=0&yt=0&loc=&reg=0&ca=0&ss=&pmt=&nmp=0&color=-1&min=0&max=0&minqty=0&nosuperlot=1&incomplete=0&showempty=1&rpp=25&pi=1&ci=0", brickID));
            JsonArray rfqItems = retrieveData(brickRFQ);

            if (rfqItems == null) {
                return Response.status(Status.NOT_FOUND).build();
            } else if (rfqItems.isEmpty()) {
                return Response.status(Status.NOT_FOUND).build();
            } else {
                final Model rfqModel = ModelFactory.createDefaultModel();
                rfqModel.setNsPrefixes(GR.NAMESPACE);
                rfqModel.setNsPrefix("dcterms", DCTerms.NS);
                rfqModel.setNsPrefix("rdf", RDF.uri);
                rfqModel.setNsPrefix("rdfs", RDFS.uri);
                rfqModel.setNsPrefix("xsd", XSD.NS);

                Resource priceSpecification = rfqModel.createResource(fRequestUrl.getRequestUri().toString());
                rfqModel.add(priceSpecification, RDF.type, GR.UnitPriceSpecification);
                rfqModel.add(priceSpecification, GR.hasCurrency, rfqModel.createLiteral("EUR"));
                rfqModel.add(priceSpecification, GR.hasCurrencyValue, rfqModel.createTypedLiteral(Float.parseFloat(rfqItems.getJsonObject(0).getString("mUsedMinPrice").substring(4))));
                rfqModel.add(priceSpecification, GR.validFrom, rfqModel.createTypedLiteral(Calendar.getInstance()));
                rfqModel.add(priceSpecification, GR.hasUnitOfMeasurement, rfqModel.createLiteral("H87"));
                rfqModel.add(priceSpecification, DCTerms.source, rfqModel.createTypedLiteral(brickRFQ, XSDDatatype.XSDanyURI));

                final String responseMediaTypeStr = responseMediaType.getType() + "/" + responseMediaType.getSubtype();
                StreamingOutput out = new StreamingOutput() {
                    public void write(OutputStream output) throws IOException, WebApplicationException {
                        RDFDataMgr.write(output, rfqModel, RDFDataMgr.determineLang(null, responseMediaTypeStr, null));
                    }
                };

                return Response.ok(out)
                        .type(responseMediaType)
                        .build();
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
            throw new WebApplicationException("Bricklink URI invalid!", Status.INTERNAL_SERVER_ERROR);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw new WebApplicationException("Bricklink not available!", Status.INTERNAL_SERVER_ERROR);
        }
    }

    @GET
    @Path("/minifig/{id}")
    @Produces({MIME.CT_APPLICATION_JSON_LD, MIME.CT_APPLICATION_NQUADS, MIME.CT_APPLICATION_NTRIPLES, MIME.CT_APPLICATION_RDF_JSON, MIME.CT_APPLICATION_RDFXML, MIME.CT_APPLICATION_TRIX, MIME.CT_APPLICATION_XTURTLE, MIME.CT_TEXT_N3, MIME.CT_TEXT_TRIG, MIME.CT_TEXT_TURTLE})
    public Response quoteMinifig(@PathParam("id") String minifigID, @Context Request request) {

        final List<Variant> defaultVariant = Variant.mediaTypes(MediaType.valueOf(MIME.CT_TEXT_TURTLE)).build();
        Variant bestVariant = request.selectVariant(defaultVariant);
        if (bestVariant == null) {
            final List<Variant> reqVariants = Variant.mediaTypes(
                    MediaType.valueOf(MIME.CT_APPLICATION_JSON_LD),
                    MediaType.valueOf(MIME.CT_APPLICATION_NQUADS),
                    MediaType.valueOf(MIME.CT_APPLICATION_NTRIPLES),
                    MediaType.valueOf(MIME.CT_APPLICATION_RDF_JSON),
                    MediaType.valueOf(MIME.CT_APPLICATION_RDFXML),
                    MediaType.valueOf(MIME.CT_APPLICATION_TRIX),
                    MediaType.valueOf(MIME.CT_APPLICATION_XTURTLE),
                    MediaType.valueOf(MIME.CT_TEXT_N3),
                    MediaType.valueOf(MIME.CT_TEXT_TRIG),
                    MediaType.valueOf(MIME.CT_TEXT_TURTLE)
            ).build();

            bestVariant = request.selectVariant(reqVariants);
            if (bestVariant == null) {
                /* Based on results, the optimal response variant can not be determined from the list given.  */
                return Response.notAcceptable(reqVariants).build();
            }
        }
        final MediaType responseMediaType = bestVariant.getMediaType();

        try {
            URL minifigRFQ = new URL(String.format("http://www.bricklink.com/ajax/clone/search/searchproduct.ajax?q=%s&st=0&cond=&type=M&cat=&yf=0&yt=0&loc=&reg=0&ca=0&ss=&pmt=&nmp=0&color=-1&min=0&max=0&minqty=0&nosuperlot=1&incomplete=0&showempty=1&rpp=25&pi=1&ci=0", minifigID));
            JsonArray rfqItems = retrieveData(minifigRFQ);

            if (rfqItems == null) {
                return Response.status(Status.NOT_FOUND).build();
            } else if (rfqItems.isEmpty()) {
                return Response.status(Status.NOT_FOUND).build();
            } else {
                final Model rfqModel = ModelFactory.createDefaultModel();
                rfqModel.setNsPrefixes(GR.NAMESPACE);
                rfqModel.setNsPrefix("dcterms", DCTerms.NS);
                rfqModel.setNsPrefix("rdf", RDF.uri);
                rfqModel.setNsPrefix("rdfs", RDFS.uri);
                rfqModel.setNsPrefix("xsd", XSD.NS);

                Resource priceSpecification = rfqModel.createResource(fRequestUrl.getRequestUri().toString());
                rfqModel.add(priceSpecification, RDF.type, GR.UnitPriceSpecification);
                rfqModel.add(priceSpecification, GR.hasCurrency, rfqModel.createLiteral("EUR"));
                rfqModel.add(priceSpecification, GR.hasCurrencyValue, rfqModel.createTypedLiteral(Float.parseFloat(rfqItems.getJsonObject(0).getString("mUsedMinPrice").substring(4))));
                rfqModel.add(priceSpecification, GR.validFrom, rfqModel.createTypedLiteral(Calendar.getInstance().getTime()));
                rfqModel.add(priceSpecification, GR.hasUnitOfMeasurement, rfqModel.createLiteral("H87"));
                rfqModel.add(priceSpecification, DCTerms.source, rfqModel.createTypedLiteral(minifigRFQ, XSDDatatype.XSDanyURI));

                final String responseMediaTypeStr = responseMediaType.getType() + "/" + responseMediaType.getSubtype();

                StreamingOutput out = new StreamingOutput() {
                    public void write(OutputStream output) throws IOException, WebApplicationException {
                        RDFDataMgr.write(output, rfqModel, RDFDataMgr.determineLang(null, responseMediaTypeStr, null));
                    }
                };

                return Response.ok(out)
                        .type(responseMediaType)
                        .build();
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
            throw new WebApplicationException("Bricklink URI invalid!", Status.INTERNAL_SERVER_ERROR);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw new WebApplicationException("Bricklink not available!", Status.INTERNAL_SERVER_ERROR);
        }
    }

    private JsonArray retrieveData(URL dataURL) throws IOException {
        InputStream is = dataURL.openStream();
        JsonReader rdr = Json.createReader(is);
        JsonObject obj = rdr.readObject();
        if (obj.getJsonObject("result").getJsonArray("typeList").isEmpty()) {
            throw new WebApplicationException("No quotation available.", Status.NOT_FOUND);
        } else {
            return obj.getJsonObject("result").getJsonArray("typeList").getJsonObject(0).getJsonArray("items");
        }
    }

    @Context
    protected UriInfo fRequestUrl;
}
