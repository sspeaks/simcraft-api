package ru.simcraftwebapi.executor;

import java.util.HashMap;
import java.util.UUID;

public class SimulationResult {
    public UUID uuid;
    public String engineVersion;
    public String wowVersion;
    public String talents;
    public double dps;
    public double dpsmin;
    public double dpsmax;
    public double dpse;
    public double dpsemin;
    public double dpsemax;
    public HashMap pawn;
    public String pawnString;
    public double dummyDPS;
}
