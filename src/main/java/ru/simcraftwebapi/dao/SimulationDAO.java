package ru.simcraftwebapi.dao;

import ru.simcraftwebapi.core.Simulation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import javax.inject.Singleton;

/*
Класс всех инстансов симуляций
 */
@Singleton
public class SimulationDAO {

    private static final HashMap<String, Simulation> simulations = new HashMap<>();

    static {
    }

    public static Simulation getSimulation(String identifier) {
        if (!simulations.containsKey(identifier)) {
            return null;
        }
        return simulations.get(identifier);
    }

//    public static Simulation addSimulation(String areaId, String serverId, String characterName ) {
//        Simulation newSim = new Simulation(, simulationId, objectModel);
//        simulations.put(simulationId, newSim);
//        return newSim;
//    }

    public static boolean deleteSimulation(String simulationId){
        if (simulations.containsKey(simulationId)) {
            simulations.remove(simulationId);
            return true;
        }
        return false;
    }
}
