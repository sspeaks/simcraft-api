package ru.simcraftwebapi.executor;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.IOUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.simcraftwebapi.configs.AppConfig;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
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
    public SimulationResult simResult;

    public void simulate(UUID uuid,
                           String areaId,
                           String serverId,
                           String characterName,
                           String talents,
                           boolean scaleFactors,
                           int iterationsNumber,
                           boolean dummy) throws IOException, InterruptedException {


        String workingPath = AppConfig.getInstance().getSimcraftExecutablePath();
        File workingFolder = new File(workingPath);
        final Logger logger = LoggerFactory.getLogger(SimExecutor.class);

        SimulationResult simResult = new SimulationResult();

        logger.info(String.format("Starting simulation for %s, %s, %s, pawn=%s, iterations=%s, talents=%s," +
                        " dummy=%s -- UUID = %s",
                areaId, serverId, characterName, scaleFactors, iterationsNumber, talents, dummy, uuid));
        Date stDate = new Date();

        System.out.println(System.getProperty("user.dir"));
        List<String> cmd = new ArrayList<>();

        cmd.add(workingFolder.toPath().resolve("simc").toString());
        cmd.add(String.format("armory=%s,%s,%s", areaId, serverId, characterName));
        if (scaleFactors) cmd.add(String.format("calculate_scale_factors=%s", "1"));
        else cmd.add(String.format("calculate_scale_factors=%s", "0"));
        if (!talents.equals("")) {
            cmd.add(String.format("talents=%s", talents));
        }
        if (dummy) cmd.add(String.format("optimal_raid=%s", "0"));
        else cmd.add(String.format("optimal_raid=%s", "1"));
        cmd.add(String.format("output=result/%s.log", uuid));
        cmd.add(String.format("iterations=%s", iterationsNumber));
        cmd.add(String.format("json2=result/%s.json", uuid));
        cmd.add(String.format("html=result/%s.html", uuid));

        ProcessBuilder pb = new ProcessBuilder();
        pb.command(cmd);
        pb.inheritIO();
//        Map<String, String> env = pb.environment();
//        env.clear();

        pb.directory(workingFolder);

        Process proc = null;
        proc = pb.start();
        proc.waitFor();

        errorFlag = proc.exitValue() != 0;
        if(errorFlag) {
            JsonObject result = new JsonObject();
            result.addProperty("error", "simc did not succeed");
            json = result.toString();
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

        Gson jsonParser = new GsonBuilder().disableHtmlEscaping().create();

        JsonObject jsonObject = new JsonParser().parse(json).getAsJsonObject();

        simResult.uuid = uuid;
        simResult.engineVersion = jsonObject.get("version").getAsString();
        simResult.wowVersion = Optional.ofNullable(jsonObject.getAsJsonObject("sim"))
                .map(item -> item.getAsJsonObject("options"))
                .map(item -> item.getAsJsonObject("dbc"))
                .map(item -> item.getAsJsonObject("Live"))
                .map(item -> item.get("wow_version").getAsString()).orElse("");

        simResult.talents = talents;

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
            HashMap pawnMap = new HashMap();
            JsonObject inner = jsonObject.getAsJsonObject("sim").getAsJsonArray("players").get(0).getAsJsonObject().
                    getAsJsonObject("scale_factors");
            Set<String> keys = inner.keySet();
            for (String key :
                    keys) {

                if (CheckIfPawnDeprecated(key)) {
                    continue;
                }

                pawnMap.put(key, Double.parseDouble(inner.get(key).toString()));
            }

            simResult.pawnString = GetPawnString(html);
            simResult.pawn = sortMapByValue(pawnMap);
        }

        json = jsonParser.toJson(simResult);

        this.html = html;
        this.json = json;
        this.simResult = simResult;
    }

    private Map<String, Double> sortMapByValue(HashMap pawnMap) {
        //String res = "{";
        Map<String, Double> res = new HashMap<>();

        while (!pawnMap.isEmpty()) {
            //if (!res.equals("{")) { res+= ","; }
            Set<String> keys = pawnMap.keySet();
            String maxKey = "";
            double max = -100;
            for (String key :
                    keys) {
                double val = (double) pawnMap.get(key);
                if (val > max) {
                    maxKey = key;
                    max = val;
                }
            }
            //{"Agi":0.7136935134157123,"Mastery":0.15567589055059644,"Vers":0.6017676584384817,"Crit":0.3876516537011335,"Haste":0.4915296534211259}
            if (!maxKey.equals("")) {
                //res += String.format("\"%s\": %s" , maxKey, pawnMap.get(maxKey));
                res.put(maxKey, (double)pawnMap.get(maxKey));
                pawnMap.remove(maxKey);
                keys.remove(maxKey);
            }
        }
        res = sortByValue(res);
        return res;
    }

    private static Map<String, Double> sortByValue(Map<String, Double> unsortedMap) {

        List<Map.Entry<String, Double>> list =
                new LinkedList<>(unsortedMap.entrySet());
        Collections.sort(list, (o1, o2) -> (o2.getValue()).compareTo(o1.getValue()));
        Map<String, Double> sortedMap = new LinkedHashMap<>();
        for (Map.Entry<String, Double> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }
        return sortedMap;
    }


    private boolean CheckIfPawnDeprecated(String key) {
        boolean res = false;
        switch (key) {
            case "SP": //spell power
                res = true;
                break;
            case "WOHdps": //Weapon offhand dps
                res = true;
                break;
            case "Wdps": //Main hand weapon dps
                res = true;
                break;
            case "AP": //attack power
                res = true;
                break;
        }
        return res;
    }

    private String GetPawnString(String html) {
        Document doc = Jsoup.parse(html, "UTF-8");
        Elements elements = doc.select("tr > td");
        for(Element element: elements) {
            String pawnString = element.html();
            if (pawnString.contains("Pawn")) {
                return StringEscapeUtils.unescapeHtml4(pawnString);
            }
        }
        return "No Pawn string found";
    }

    public String buildPawnString(SimulationResult res){
        return null;
        //rankings Wdps > Agi ~= AP > Haste > Mastery > Crit ~= Vers > WOHdps
        //( Pawn: v1: "Мичикко-Assassination": Class=Rogue, Spec=Assassination, Agility=2.30, Ap=2.19, CritRating=1.48,
        // HasteRating=2.03, MasteryRating=1.78, Versatility=1.45, Dps=11.87 )
    }
}
