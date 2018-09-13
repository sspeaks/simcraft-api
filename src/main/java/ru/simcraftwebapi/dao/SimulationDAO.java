package ru.simcraftwebapi.dao;

import ru.simcraftwebapi.core.Simulation;
import ru.simcraftwebapi.executor.SimExecutorRunner;

import java.util.*;
import javax.inject.Singleton;

/*
Класс всех инстансов симуляций
 */
@Singleton
public class SimulationDAO {

    private static final HashMap<UUID, Simulation> simulations = new HashMap<>();

    static {
    }

    public static Simulation getSimulation(UUID identifier) {
        if (!simulations.containsKey(identifier)) {
            return null;
        }
        return simulations.get(identifier);
    }

    public static UUID addSimulation(String areaId, String serverId, String characterName, String talents,
                                     boolean pawn, int iterNum, boolean withDummy) {
        Simulation newSim = new Simulation(areaId, serverId, characterName, talents, pawn, iterNum, withDummy);
        simulations.put(newSim.uuid, newSim);
        return newSim.uuid;
    }

    public static boolean deleteSimulation(UUID simulationId){
        if (simulations.containsKey(simulationId)) {
            simulations.remove(simulationId);
            return true;
        }
        return false;
    }

    public static UUID getSimpleSimulationUUID() {
        return UUID.randomUUID();
    }

    public static void SimulateAsync(UUID uuid) {
        SimExecutorRunner simExecutorAsync = new SimExecutorRunner(getSimulation(uuid));
        Thread t = new Thread(simExecutorAsync);
        t.start();
    }
}
