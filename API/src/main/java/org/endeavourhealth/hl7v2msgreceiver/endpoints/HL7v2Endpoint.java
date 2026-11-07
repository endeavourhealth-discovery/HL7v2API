package org.endeavourhealth.hl7v2msgreceiver.endpoints;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.endeavourhealth.common.security.annotations.RequiresAdmin;
import org.endeavourhealth.common.utility.MetricsHelper;
import org.endeavourhealth.common.utility.MetricsTimer;
import org.endeavourhealth.common.utility.SlackHelper;
import org.endeavourhealth.coreui.endpoints.AbstractEndpoint;
import org.endeavourhealth.hl7v2msgreceiver.dal.Hl7JDBCDAL;
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
    @Path("/$process-message")
    @ApiOperation(value = "Save the message in the database")
    @RequiresAdmin
    public Response processMessage(String request) throws Exception {

        LOG.info("HL7v2 message received!  ");
        try (MetricsTimer t = MetricsHelper.recordTime("HL7v2Endpoint.processMessage")) {
            return StoreMessageInDatabase(request);
        }
    }

    @GET
    @Path("/healthCheck")
    @ApiOperation(value = "health check")
    @RequiresAdmin
    public Response healthCheck(String request) {

        LOG.debug("HL7v2 health check successful");
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
            JSONObject jsonObj = (JSONObject) parser.parse(request);
            Object body = jsonObj.get("body");
            jsonObj.remove("body");
            String wrapper = mapper.writeValueAsString(jsonObj);

            try (Hl7JDBCDAL viewerDAL = new Hl7JDBCDAL()) {
                viewerDAL.setAcuteConnection();
                String bodyStr = body.toString().replace("\r\n", "#@#@#@");
                bodyStr = bodyStr.replace("\n", "\r\n");
                bodyStr = bodyStr.replace("#@#@#@", "\r\n");

                JSONObject metaJson = (JSONObject) parser.parse(jsonObj.get("meta").toString());

                Object tagObj = metaJson.get("tag");
                JSONObject tagJson = (JSONObject) (tagObj instanceof JSONArray ? parser.parse((((JSONArray) tagObj).get(0)).toString()) : tagObj);
                String odsCode = tagJson.get("code").toString();
                Object msgIdObj = jsonObj.get("identifier");
                JSONObject msgIdentifier = (JSONObject) (msgIdObj instanceof JSONArray ? parser.parse((((JSONArray) msgIdObj).get(0)).toString()) : msgIdObj);
                String msgId = msgIdentifier.get("value").toString();
                String response="";

                if(null!=odsCode && !odsCode.isEmpty()) {
                    viewerDAL.saveHL7Message(wrapper, bodyStr, (jsonObj.get("id")).toString(), odsCode, msgId);
                    LOG.info("HL7v2 message saved to database");
                    response = "{ \"Response\" : \" " + " : Message Filed Successfully! \"}";
                    return Response.ok().entity(response).build();
                } else {
                    String msg="Skipping the HL7v2 message because it doesn't have an ods code for the message id" +msgId + " investigate further";
                    LOG.error(msg);
                    response = "{ \"Response\" : \" " + " Message failed to process because ODS CODE is empty for the message id: "+msgId+ " \"}";
                    SlackHelper.sendSlackMessage(SlackHelper.Channel.HL7v2Api, msg);
                    return Response.status(400).entity(response).build();
                }

            }
        } catch (Exception e) {
            LOG.error("Error saving payload to database: " + request, e);

            //let the operations team know what's going on
            String msg = "Error parsing or saving HL7v2 message to database"; //not including payload as this will contain PID
            SlackHelper.sendSlackMessage(SlackHelper.Channel.HL7v2Api, msg, e);

            throw new RuntimeException("Received the Invalid Payload or Exception while persisting HL7 message", e);
        }
    }

}

