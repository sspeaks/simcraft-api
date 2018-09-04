package ru.simcraftwebapi.executor;

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
            simExec.simulate(sim.uuid, sim.areaId, sim.serverId, sim.characterName, sim.talents,  sim.pawn, sim.iterNum);
            sim.resultHtml = simExec.html;
            sim.resultJson = simExec.json;
            sim.isFinished = true;
            sim.isError = simExec.errorFlag;
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }
}
