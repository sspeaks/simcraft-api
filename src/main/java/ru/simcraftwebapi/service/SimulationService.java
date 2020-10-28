package ru.simcraftwebapi.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.simcraftwebapi.core.Simulation;
import ru.simcraftwebapi.dao.SimulationDAO;
import ru.simcraftwebapi.executor.SimExecutor;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

@Path("")
public class SimulationService {
    final Logger logger = LoggerFactory.getLogger(SimulationService.class);

    @GET
    @Path("/simulate")
    @Produces({"application/json;charset=utf-8", "text/html;charset=utf-8"})
    //@Produces({ MediaType.APPLICATION_JSON + ";charset=utf-8", MediaType.TEXT_HTML})
    public Response simulate(@QueryParam("zone") String areaId,
                                         @QueryParam("realm") String serverId,
                                         @QueryParam("character") String characterName,
                                         @DefaultValue("")@QueryParam("talents") String talents,
                                         @QueryParam("type") String type,
                                         @DefaultValue("false")@QueryParam("pawn") boolean pawn,
                                         @DefaultValue("1000")@QueryParam("iterations") int iterNum,
                                         @DefaultValue("false")@QueryParam("dummy") boolean withDummy) {
        logger.info(String.format("GET for %s %s %s %s", areaId, serverId, characterName, type));
        SimExecutor simExec = new SimExecutor();
        try {
            simExec.simulate(SimulationDAO.getSimpleSimulationUUID(), areaId, serverId, characterName, talents,  pawn, iterNum, withDummy);
        } catch (IOException | InterruptedException e) {
            logger.error(e.getMessage());
            return Response.serverError().entity(e.getMessage()).build();
        }
        if (simExec.errorFlag) {
            return Response.serverError().entity(simExec.json).build();
        }
        return Response.ok().entity(type.equals("json") ? simExec.json :simExec.html).build();
    }

    //return uuid of launched sim
    @GET
    @Path("/simulate/async")
    @Produces({"application/json;charset=utf-8"})
    //@Produces({ MediaType.APPLICATION_JSON})
    public Response addNewSimulationToQueue(@QueryParam("zone") String areaId,
                                            @QueryParam("realm") String serverId,
                                            @QueryParam("character") String characterName,
                                            @DefaultValue("")@QueryParam("talents") String talents,
                                            @QueryParam("type") String type,
                                            @DefaultValue("false")@QueryParam("pawn") boolean pawn,
                                            @DefaultValue("1000")@QueryParam("iterations") int iterNum,
                                            @DefaultValue("false")@QueryParam("dummy") boolean withDummy) {
        logger.info(String.format("GET async for %s %s %s %s %s %s %s %s", areaId, serverId, characterName, talents, type, pawn, iterNum, withDummy));
        UUID uuid = SimulationDAO.addSimulation(areaId, serverId, characterName, talents, pawn, iterNum, withDummy);
        SimulationDAO.SimulateAsync(uuid);
        //(String.format("{\"uuid\":\"%s\"}", uuid.toString())).build();
        return Response.accepted().header("Location", String.format("/simulate/async/status/%s", uuid.toString())).build();
    }

    //returns html or json of finished sim, if not - returns json with "not finished" status
    @GET
    @Path("/simulate/async/result")
    @Produces({"application/json;charset=utf-8", "text/html;charset=utf-8"})
    //@Produces({ MediaType.APPLICATION_JSON, MediaType.TEXT_HTML})
    public Response getSimulationResult(@QueryParam("uuid") UUID uuid,
                                                  @QueryParam("type") String type,
                                                  @DefaultValue("true")@QueryParam("delete") boolean deleteNeeded) {
        logger.info(String.format("GET async result for %s %s delete=%s", uuid, type, deleteNeeded));
        Simulation sim = SimulationDAO.getSimulation(uuid);
        if (sim == null) {
            return Response.status(404).entity(String.format("{ " +
                    "\"uuid\": \"%s\"," +
                    "\"status\": -1," +
                    "\"message\": \"Simulation uuid=%s not found\"" +
                    "}", uuid, uuid)).build();
        }
        String result;
        if (sim.isFinished) {
            result = sim.getResult(type);
            if (deleteNeeded) { SimulationDAO.deleteSimulation(uuid); }
        }
        else {
            result = String.format("{" +
                    "\"uuid\": \"%s\"," +
                    "\"status\": 1," +
                    "\"message\": \"Simulation uuid=%s has been started, but not yet finished\"" +
                    "}", uuid, uuid);
        }
        return Response.ok().entity(result).build();
    }

    @GET
    @Path("/simulate/async/status")
    public Response getSimulationStatus(@QueryParam("uuid") UUID uuid) {
        logger.info(String.format("GET async status for %s", uuid));
        Simulation sim = SimulationDAO.getSimulation(uuid);
        if (sim == null) {
            return Response.status(404).entity(String.format("{ " +
                    "\"uuid\": \"%s\"," +
                    "\"status\": -1," +
                    "\"message\": \"Simulation uuid=%s not found\"" +
                    "}", uuid, uuid)).build();
        }
        if (sim.isFinished) {
            try {
                return Response.seeOther(new URI(String.format("/simulate/async/result/%s", uuid))).build();
            } catch (URISyntaxException e) {
                return Response.serverError().build();
            }
        }
        else {
            return Response.ok().header("Location", String.format("/simulate/async/status/%s", uuid)).build();
        }
    }
}