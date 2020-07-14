package org.endeavourhealth.endpoints;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.endeavourhealth.common.security.annotations.RequiresAdmin;
import org.endeavourhealth.coreui.endpoints.AbstractEndpoint;
import org.endeavourhealth.dal.Hl7JDBCDAL;
import org.endeavourhealth.models.HL7Params;
import org.endeavourhealth.models.HL7Request;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/hl7")
@Api(description = "API endpoint related to the HL7v2 API")
public final class HL7v2Endpoint extends AbstractEndpoint {
    private static final Logger LOG = LoggerFactory.getLogger(HL7v2Endpoint.class);

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.TEXT_PLAIN)
    @Timed(absolute = true, name="HL7v2API.HL7v2Endpoint.Post")
    @Path("/$process-message")
    @ApiOperation(value = "Save the message in the database")
    @RequiresAdmin
    public Response processMessage(String request ) throws Exception {

        LOG.info("HL7v2 message received ");


        return StoreMessageInDatabase(request);
    }

    private Response StoreMessageInDatabase(String request) {
        try {

            ObjectMapper mapper = new ObjectMapper();
            String jsonstring = mapper.writeValueAsString(request);
            JSONParser parser = new JSONParser();
            JSONObject jsonobj = (JSONObject) parser.parse(request);
            Object body = jsonobj.get("body");
            jsonobj.remove("body");
            String wrapper = mapper.writeValueAsString(jsonobj);

            try (Hl7JDBCDAL viewerDAL = new Hl7JDBCDAL()) {
                viewerDAL.saveHL7Message(wrapper, body.toString());
            }

            String test =  "{ \"Test Response\" : \" "/*+request.getResourceType()*/+" : Message Filed Successfully! \"}";

           /* parser = new JSONParser();
            json = (JSONObject) parser.parse(test);*/
            return Response
                    .ok()
                    .entity(test)
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("Resource error:" + e);
        }
    }


}

