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
    public String talents;

    public Simulation(String areaId, String serverId, String characterName, String talents, boolean pawn, int iterNum) {
        this.uuid = UUID.randomUUID();
        this.areaId = areaId;
        this.serverId = serverId;
        this.characterName = characterName;
        this.pawn = pawn;
        this.iterNum = iterNum;
        this.talents = talents;
    }

    public String getResult(String type) {
        return (type.equals("json") ? this.resultJson : this.resultHtml);
    }
}
