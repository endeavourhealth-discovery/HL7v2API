package org.endeavourhealth.endpoints;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.endeavourhealth.common.security.annotations.RequiresAdmin;
import org.endeavourhealth.coreui.endpoints.AbstractEndpoint;
import org.endeavourhealth.dal.DbInstance;
import org.endeavourhealth.dal.Hl7JDBCDAL;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/")
@Api(description = "API endpoint related to the HL7v2 API")
public final class HL7v2Endpoint extends AbstractEndpoint {
    private static final Logger LOG = LoggerFactory.getLogger(HL7v2Endpoint.class);


    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes({MediaType.APPLICATION_JSON})
    @Timed(absolute = true, name = "HL7v2API.HL7v2Endpoint.Post")
    @Path("/$process-message")
    @ApiOperation(value = "Save the message in the database")
    @RequiresAdmin
    public Response processMessage(String request) throws Exception {

        LOG.info("HL7v2 message received ");

        return StoreMessageInDatabase(request);
    }

    @GET
    @Path("/healthCheck")
    @ApiOperation(value = "health check")
    @RequiresAdmin
    public Response healthCheck(String request) {

        LOG.debug("HL7v2 health check  successful ");
        return Response
                .ok().build();
    }

    /**
     * API to process and pass HL7 Message v2 to DAO layer
     *
     * @param request
     * @return
     */
    private Response StoreMessageInDatabase(String request) {
        try {

            ObjectMapper mapper = new ObjectMapper();
            JSONParser parser = new JSONParser();
            JSONObject jsonobj = (JSONObject) parser.parse(request);
            Object body = jsonobj.get("body");
            jsonobj.remove("body");
            String wrapper = mapper.writeValueAsString(jsonobj);

            try (Hl7JDBCDAL viewerDAL = new Hl7JDBCDAL()) {
                String bodyStr= body.toString().replace("\r\n","#@#@#@");
                bodyStr= bodyStr.replace("\n","\r\n");
                bodyStr= bodyStr.replace("#@#@#@","\r\n");
                viewerDAL.saveHL7Message(wrapper, bodyStr, (jsonobj.get("id")).toString());

                String test = "{ \"Response\" : \" "/*+request.getResourceType()*/ + " : Message Filed Successfully! \"}";

            /*    DbInstance dbInstanceConfiguration = viewerDAL.getInstanceConfiguration();
                JSONObject jsonobj1 = (JSONObject) parser.parse(jsonobj.get("meta").toString());

                Object odscode = jsonobj1.get("tag");
                JSONArray array = (JSONArray) odscode;

                JSONObject jsonobj2 = (JSONObject) parser.parse((array.get(0)).toString());
                String s = jsonobj2.get("code").toString();

                EdsSenderClient.sendmsg(s, true, body.toString(), (jsonobj.get("id")).toString(), dbInstanceConfiguration.getEdsConfiguration());
            */
                return Response
                        .ok()
                        .entity(test)
                        .build();
            }
        } catch (Exception e) {
            throw new RuntimeException("Exception while persisting HL7 message", e);
        }
    }


}

