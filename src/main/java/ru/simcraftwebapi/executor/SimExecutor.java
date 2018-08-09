package ru.simcraftwebapi.executor;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.hibernate.id.GUIDGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.simcraftwebapi.configs.AppConfig;
import ru.simcraftwebapi.core.Simulation;
import ru.simcraftwebapi.core.SimulationResult;

import javax.ws.rs.core.MediaType;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.*;

public class SimExecutor {

    public static String getResult(String areaId, String serverId, String characterName, String type, int scaleFactors) throws IOException {
        final Logger logger = LoggerFactory.getLogger(SimExecutor.class);

        Simulation sim = new Simulation();

        SimulationParams params = new SimulationParams();
        params.areaId = areaId;
        params.serverId = serverId;
        params.characterName = characterName;

                String uniqueName = String.format("Starting simulation for %s,%s,%s", areaId, serverId, characterName);
        UUID uuid =UUID.randomUUID();

        logger.info(String.format("Starting simulation for %s,%s,%s UUID  = %s", areaId, serverId, characterName, uuid));
        Date stDate = new Date();
        ProcessBuilder pb = new ProcessBuilder("./simc",
                String.format("armory=%s,%s,%s", areaId, serverId, characterName),
                String.format("calculate_scale_factors=%s", scaleFactors),
                String.format("output=result/%s.log", uuid),
                "iterations=1000",
                String.format("json2=result/%s.json", uuid),
                String.format("html=result/%s.html", uuid));

        Map<String, String> env = pb.environment();
        env.clear();

        String workingPath = AppConfig.getInstance().getSimcraftExecutablePath();

        File workingFolder = new File(workingPath);
        pb.directory(workingFolder);

        Process proc = null;
        proc = pb.start();


        //BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));

        BufferedReader stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));

        String s = null;
        //while ((s = stdInput.readLine()) != null)
        //read any errors from the attempted command
        //{
        //    System.out.println(s);
        //}

        while ((s = stdError.readLine()) != null)
        {
            logger.error(s);
        }
        logger.info(String.format("Simulation for %s,%s,%s ended in %s", areaId, serverId, characterName,
                Duration.between(stDate.toInstant(), (new Date()).toInstant())));

        Path htmlPath = FileSystems.getDefault().getPath(workingPath + "result/", uuid + ".html");
        String html = String.join("", Files.readAllLines(htmlPath, StandardCharsets.UTF_8));
        Path jsonPath = FileSystems.getDefault().getPath(workingPath + "result/", uuid + ".json");
        String json = String.join("", Files.readAllLines(jsonPath, StandardCharsets.UTF_8));
        Path logPath = FileSystems.getDefault().getPath(workingPath + "result/", uuid + ".log");

        if (Files.exists(htmlPath)) {
            Files.delete(htmlPath);
        }
        if (Files.exists(jsonPath)) {
            Files.delete(jsonPath);
        }
        if (Files.exists(logPath)) {
            Files.delete(logPath);
        }


        //парсим json в SimulationResult
        SimulationResult simResult = new SimulationResult();

        Gson jsonParser = new Gson();

        JsonObject jsonObject = new JsonParser().parse(json).getAsJsonObject();

        simResult.uuid = uuid;
        simResult.engineVersion = jsonObject.get("version").getAsString();
        simResult.wowVersion = jsonObject.getAsJsonObject("sim").
                getAsJsonObject("options").getAsJsonObject("dbc").getAsJsonObject("bfa-BETA").
                get("wow_version").getAsString();

        simResult.dps = jsonObject.getAsJsonObject("sim").
                getAsJsonArray("players").get(0).getAsJsonObject().
                getAsJsonObject("collected_data").getAsJsonObject("dps").
                get("mean").getAsDouble();
        simResult.dpsmin = jsonObject.getAsJsonObject("sim").
                getAsJsonArray("players").get(0).getAsJsonObject().
                getAsJsonObject("collected_data").getAsJsonObject("dps").
                get("min").getAsDouble();
        simResult.dpsmax = jsonObject.getAsJsonObject("sim").
                getAsJsonArray("players").get(0).getAsJsonObject().
                getAsJsonObject("collected_data").getAsJsonObject("dps").
                get("max").getAsDouble();

        simResult.dpse = jsonObject.getAsJsonObject("sim").
                getAsJsonArray("players").get(0).getAsJsonObject().
                getAsJsonObject("collected_data").getAsJsonObject("dpse").
                get("mean").getAsDouble();
        simResult.dpsemin = jsonObject.getAsJsonObject("sim").
                getAsJsonArray("players").get(0).getAsJsonObject().
                getAsJsonObject("collected_data").getAsJsonObject("dpse").
                get("min").getAsDouble();
        simResult.dpsemax = jsonObject.getAsJsonObject("sim").
                getAsJsonArray("players").get(0).getAsJsonObject().
                getAsJsonObject("collected_data").getAsJsonObject("dpse").
                get("max").getAsDouble();

        if (scaleFactors == 1) {
            HashMap pawn = new HashMap();
            JsonObject inner = jsonObject.getAsJsonObject("sim").getAsJsonArray("players").get(0).getAsJsonObject().
                    getAsJsonObject("scale_factors");
            Set<String> keys = inner.keySet();
            for (String key:
                 keys) {
                pawn.put(key, inner.get(key));
            }
            simResult.pawn = pawn;
        }

        json = jsonParser.toJson(simResult);

        return (type.equals("json") ? json : html);

    }

    public static void main(String[] args) {
        try {
            System.out.println(getResult("eu","borean-tundra",
                    "мичикко","json", 1));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
