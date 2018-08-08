package ru.simcraftwebapi.configs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.simcraftwebapi.hibernate.HibernateWrapper;

import java.util.Properties;

public class AppConfig {
    final Logger logger = LoggerFactory.getLogger(AppConfig.class);

    Properties configFile;

    private static AppConfig _instance;

    public static AppConfig getInstance(){
        if (_instance == null) {
            _instance = new AppConfig();
        }
        return _instance;
    }

    public String getProperty(String key)
    {
        String value = this.configFile.getProperty(key);
        return value;
    }

    public AppConfig(){
        configFile = new java.util.Properties();
        try {
            configFile.load(this.getClass().getClassLoader().
                    getResourceAsStream("config.cfg"));
        }catch(Exception eta){
            eta.printStackTrace();
        }
    }


    public String getSimcraftExecutablePath() {
        return getProperty("SIMCRAFT_PATH");
    }

}
