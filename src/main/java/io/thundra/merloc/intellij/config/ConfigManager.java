package io.thundra.merloc.intellij.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.intellij.openapi.diagnostic.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author serkan
 */
public final class ConfigManager {

    private static final Logger LOGGER = Logger.getInstance(ConfigManager.class);

    private static final String MERLOC_FOLDER_NAME = ".merloc";
    private static final String MERLOC_CONFIG_FILE_NAME = "config.json";

    private ConfigManager() {
    }

    private static String getConfigFilePath() {
        String userHome = System.getProperty("user.home");
        return userHome + File.separator + MERLOC_FOLDER_NAME + File.separator + MERLOC_CONFIG_FILE_NAME;
    }

    public static Map<String, Object> loadConfigFromFile(String profileName) {
        String configPath = getConfigFilePath();
        File configFile = new File(configPath);

        if (!configFile.exists()) {
            return null;
        }

        try (FileInputStream configFileStream = new FileInputStream(configFile)) {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> props = mapper.readValue(configFileStream, Map.class);
            Map<String, Object> profiles = (Map<String, Object>) props.get("profiles");
            Map<String, Object> defaultConfigProps = (Map<String, Object>) profiles.get("default");
            Map<String, Object> configProps = defaultConfigProps;
            if (null != profileName) {
                Map<String, Object> profileProps = (Map<String, Object>) profiles.get(profileName);
                if (null != profileProps) {
                    configProps = profileProps;
                }
            }
            return configProps;
        } catch (IOException e) {
            LOGGER.error(e);
        }

        return null;
    }

    public static void writeConfigToFile(Map<String, Object> config, String profileName, String oldProfileName) {
        String configPath = getConfigFilePath();
        File configFile = new File(configPath);
        boolean configFileWasExist = configFile.exists();
        if (!configFileWasExist) {
            try {
                if (configFile.getParentFile() != null) {
                    configFile.getParentFile().mkdirs();
                }
                configFile.createNewFile();
            } catch (IOException e) {
                LOGGER.error(e);
                return;
            }
        }

        try {
            ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
            Map<String, Object> props;

            try (FileInputStream configFileStream = new FileInputStream(configFile)) {
                props = configFileWasExist
                                ? mapper.readValue(configFileStream, Map.class)
                                : new HashMap<>();
                Map<String, Object> profiles =
                        (Map<String, Object>) props.getOrDefault("profiles", new HashMap<>());
                Map<String, Object> configProps =
                        (Map<String, Object>) profiles.getOrDefault(profileName, new HashMap<>());

                configProps.put("brokerURL", config.get("brokerURL"));
                configProps.put("connectionName", config.get("connectionName"));
                configProps.put("apiKey", config.get("apiKey"));
                configProps.put("runtimeVersion", config.get("runtimeVersion"));

                if (null != oldProfileName) {
                    profiles.remove(oldProfileName);
                }
                profiles.put(profileName, configProps);

                props.put("profiles", profiles);
            }

            try (FileOutputStream configFileOutputStream = new FileOutputStream(configFile)) {
                String jsonProps = mapper.writeValueAsString(props);
                configFileOutputStream.write(jsonProps.getBytes());
            }
        } catch (IOException e) {
            LOGGER.error(e);
        }
    }

    public static void removeConfigWithProfile(String profileName) {
        if (null == profileName || profileName.equals("default")) {
            return;
        }

        String configPath = getConfigFilePath();
        File configFile = new File(configPath);

        if (!configFile.exists()) {
            return;
        }

        try {
            ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
            Map<String, Object> props;

            try (FileInputStream configFileStream = new FileInputStream(configFile)) {
                props = mapper.readValue(configFileStream, Map.class);
                Map<String, Object> profiles = (Map<String, Object>) props.get("profiles");
                if (null != profiles.get(profileName)) {
                    profiles.remove(profileName);
                    props.put("profiles", profiles);
                }
            }

            if (props != null) {
                try (FileOutputStream configFileOutputStream = new FileOutputStream(configFile)) {
                    String jsonProps = mapper.writeValueAsString(props);
                    configFileOutputStream.write(jsonProps.getBytes());
                }
            }
        } catch (IOException e) {
            LOGGER.error(e);
        }
    }

}
