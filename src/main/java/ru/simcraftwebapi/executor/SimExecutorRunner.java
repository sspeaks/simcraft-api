package ru.simcraftwebapi.executor;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.simcraftwebapi.core.Simulation;
import ru.simcraftwebapi.dao.SimulationDAO;

import java.io.IOException;

public class SimExecutorRunner implements Runnable {

    private Simulation sim;
    final Logger logger = LoggerFactory.getLogger(SimExecutorRunner.class);

    public SimExecutorRunner(Simulation sim) {
        this.sim = sim;
    }

    public void run() {
        SimExecutor simExec = new SimExecutor();
        try {
            simExec.simulate(sim.uuid, sim.areaId, sim.serverId, sim.characterName, sim.talents,  sim.pawn, sim.iterNum, false);
            Gson jsonParser = new GsonBuilder().disableHtmlEscaping().create();
            SimulationResult simResult = simExec.simResult;
            sim.resultHtml = simExec.html;
            sim.resultJson = simExec.json;
            sim.isError = simExec.errorFlag;
            if (sim.dummy && !simExec.errorFlag) {
                simExec.simulate(sim.uuid, sim.areaId, sim.serverId, sim.characterName, sim.talents,  false, 1000, true);
                sim.resultJsonForDummy = simExec.json;
                sim.isError = sim.isError || simExec.errorFlag;
                simResult.dummyDPS = simExec.simResult.dps;
            }
            sim.resultJson = jsonParser.toJson(simResult);
            if (simExec.errorFlag) {
                sim.resultJson = String.format("{ " +
                        "\"uuid\": \"%s\"," +
                        "\"status\": -2," +
                        "\"message\": \"" + simExec.html.replace("\"", "'").replace("\n", "") + "\"" +
                        "}", sim.uuid);
            }
            sim.isFinished = true;
        } catch (IOException e) {
            logger.error(e.getMessage());
            sim.resultJson = String.format("{ " +
                    "\"uuid\": \"%s\"," +
                    "\"status\": -2," +
                    "\"message\": \"" + e.getMessage().replace("\"", "'") + "\"" +
                    "}", sim.uuid);
            sim.isFinished = true;
        }
    }
}
