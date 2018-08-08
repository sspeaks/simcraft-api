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

@Path("")
public class SimulationService {
    final Logger logger = LoggerFactory.getLogger(SimulationService.class);

    @GET
    @Path("/simulate")
    @Produces({ MediaType.APPLICATION_JSON , MediaType.TEXT_HTML})
    public Response simulate(@QueryParam("zone") String areaId,
                                         @QueryParam("realm") String serverId,
                                         @QueryParam("character") String characterName,
                                         @QueryParam("type") String type) {
        logger.info(String.format("GET for %s %s %s %s", areaId, serverId, characterName, type));
        String json = null;
        try {
            json = SimExecutor.getResult(areaId, serverId, characterName, type);
        } catch (IOException e) {
            logger.error(e.getMessage());
            return Response.serverError().entity(e.getMessage()).build();
        }
        return Response.ok().entity(json).build();
    }
}