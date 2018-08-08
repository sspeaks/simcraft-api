package ru.simcraftwebapi.service;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.simcraftwebapi.executor.SimExecutor;

import java.io.IOException;
import java.util.Date;

@Path("/simulate")
public class SimulationService {
    final Logger logger = LoggerFactory.getLogger(SimulationService.class);

    @GET
    @Path("/{area_id}/{server_id}/{character_name}")
    @Produces({ MediaType.APPLICATION_JSON })
    public Response getKnowledgeBaseRule(@PathParam("area_id") String areaId, @PathParam("server_id") String serverId,
                                         @PathParam("character_name") String characterName) {
        logger.info(String.format("GET for /%s/%s/%s", areaId, serverId, characterName));
        Gson jsonParse = new Gson();
        String json = null;
        try {
            json = jsonParse.toJson(SimExecutor.getResult(areaId, serverId, characterName, MediaType.APPLICATION_JSON));
        } catch (IOException e) {
            logger.error(e.getMessage());
            return Response.status(200).entity(e.getMessage()).build();
        }
        return Response.ok().entity(json).build();
    }
}