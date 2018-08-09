package ru.simcraftwebapi.executor;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.simcraftwebapi.configs.AppConfig;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.*;

public class SimExecutor {

    public String json;
    public String html;
    public boolean errorFlag = false;

    public void simulate(UUID uuid,
                           String areaId,
                           String serverId,
                           String characterName,
                           boolean scaleFactors,
                           int iterationsNumber) throws IOException {
        final Logger logger = LoggerFactory.getLogger(SimExecutor.class);


        logger.info(String.format("Starting simulation for %s, %s, %s, pawn=%s, iterations=%s -- UUID = %s",
                areaId, serverId, characterName, scaleFactors, iterationsNumber, uuid));
        Date stDate = new Date();
        ProcessBuilder pb = new ProcessBuilder("./simc",
                String.format("armory=%s,%s,%s", areaId, serverId, characterName),
                String.format("calculate_scale_factors=%s", (scaleFactors ? "1" : "0")),
                String.format("output=result/%s.log", uuid),
                String.format("iterations=%s", iterationsNumber),
                String.format("json2=result/%s.json", uuid),
                String.format("html=result/%s.html", uuid));

        Map<String, String> env = pb.environment();
        env.clear();

        String workingPath = AppConfig.getInstance().getSimcraftExecutablePath();

        File workingFolder = new File(workingPath);
        pb.directory(workingFolder);

        Process proc = null;
        proc = pb.start();


        BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));

        BufferedReader stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));

        String s = null;
        while ((s = stdInput.readLine()) != null)
        {
            System.out.println(s);
            logger.debug(s);
        }

        String err = "";
        while ((s = stdError.readLine()) != null) {
            errorFlag = true;
            logger.error(s);
            err += s + System.lineSeparator();
        }

        if (errorFlag) {
            this.html = err;
            this.json = err;
            return;
        }

        logger.info(String.format("Simulation for %s,%s,%s ended in %s", areaId, serverId, characterName,
                Duration.between(stDate.toInstant(), (new Date()).toInstant())));

        Path htmlPath = FileSystems.getDefault().getPath(workingPath + "result/", uuid + ".html");
        String html = String.join("", Files.readAllLines(htmlPath, StandardCharsets.UTF_8));
        Path jsonPath = FileSystems.getDefault().getPath(workingPath + "result/", uuid + ".json");
        String json = String.join("", Files.readAllLines(jsonPath, StandardCharsets.UTF_8));
        Path logPath = FileSystems.getDefault().getPath(workingPath + "result/", uuid + ".log");
        String log = String.join("", Files.readAllLines(logPath, StandardCharsets.UTF_8));

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

        if (scaleFactors) {
            HashMap pawn = new HashMap();
            JsonObject inner = jsonObject.getAsJsonObject("sim").getAsJsonArray("players").get(0).getAsJsonObject().
                    getAsJsonObject("scale_factors");
            Set<String> keys = inner.keySet();
            for (String key :
                    keys) {
                pawn.put(key, inner.get(key));
            }
            simResult.pawn = pawn;
        }

        json = jsonParser.toJson(simResult);

        this.html = html;
        this.json = json;
    }

    public String buildPawnString(SimulationResult res){
        return null;
        //rankings Wdps > Agi ~= AP > Haste > Mastery > Crit ~= Vers > WOHdps
        //( Pawn: v1: "Мичикко-Assassination": Class=Rogue, Spec=Assassination, Agility=2.30, Ap=2.19, CritRating=1.48, HasteRating=2.03, MasteryRating=1.78, Versatility=1.45, Dps=11.87 )
    }
}
