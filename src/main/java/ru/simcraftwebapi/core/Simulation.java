package ru.simcraftwebapi.core;

import java.util.UUID;

public class Simulation {

    public UUID uuid;
    public boolean isFinished = false;

    public String areaId;
    public String serverId;
    public String characterName;
    public boolean pawn;
    public int iterNum;

    public String resultJson;
    public String resultHtml;
    public boolean isError;

    public Simulation(String areaId, String serverId, String characterName, boolean pawn, int iterNum) {
        this.uuid = UUID.randomUUID();
        this.areaId = areaId;
        this.serverId = serverId;
        this.characterName = characterName;
        this.pawn = pawn;
        this.iterNum = iterNum;
    }

    public String getResult(String type) {
        return (type.equals("json") ? this.resultJson : this.resultHtml);
    }
}
