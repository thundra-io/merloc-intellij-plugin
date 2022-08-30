package io.thundra.merloc.intellij;

import com.intellij.execution.RunManagerListener;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.project.Project;
import io.thundra.merloc.intellij.config.ConfigManager;
import io.thundra.merloc.intellij.runtime.RuntimeManager;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * @author serkan
 */
public class MerLocConfigurationFactory extends ConfigurationFactory {

    private static final String FACTORY_NAME = "MerLoc Configuration Factory";

    private final RuntimeManager runtimeManager = new RuntimeManager();
    private final Map<String, Boolean> projectToMessageBusConnected = new HashMap<>();

    public MerLocConfigurationFactory(ConfigurationType type) {
        super(type);
    }

    @Override
    public @NotNull @NonNls String getId() {
        return FACTORY_NAME;
    }

    @Override
    public String getName() {
        return FACTORY_NAME;
    }

    private static void applyConfigChanges(MerLocRunConfiguration runConfiguration, String existingId) {
        String oldProfileName = null;
        if (null != existingId) {
            String[] arr = StringUtils.split(existingId, ".");
            if (arr.length >= 1) {
                oldProfileName = arr[arr.length - 1];
            }
        }
        String profileName = runConfiguration.getName();
        Map<String, Object> config = new HashMap<>();
        config.put("brokerURL", runConfiguration.getBrokerURL());
        config.put("connectionName", runConfiguration.getConnectionName());
        config.put("runtimeVersion", runConfiguration.getRuntimeVersion());
        ConfigManager.writeConfigToFile(config, profileName, oldProfileName);
    }

    @NotNull
    @Override
    public RunConfiguration createTemplateConfiguration(Project project) {
        String projectName = project.getName();
        if (projectToMessageBusConnected.get(projectName) == null) {
            project.getMessageBus().connect().subscribe(RunManagerListener.TOPIC, new RunManagerListener() {
                @Override
                public void runConfigurationRemoved(RunnerAndConfigurationSettings settings) {
                    if (settings.getConfiguration() instanceof MerLocRunConfiguration) {
                        MerLocRunConfiguration configuration = (MerLocRunConfiguration) settings.getConfiguration();
                        String profileName = configuration.getName();
                        ConfigManager.removeConfigWithProfile(profileName);
                    }
                }

                @Override
                public void runConfigurationAdded(RunnerAndConfigurationSettings settings) {
                    if (settings.getConfiguration() instanceof MerLocRunConfiguration) {
                        MerLocRunConfiguration runConfiguration = (MerLocRunConfiguration) settings.getConfiguration();
                        settings.setTemporary(false);
                        applyConfigChanges(runConfiguration, null);
                    }
                }

                @Override
                public void runConfigurationChanged(RunnerAndConfigurationSettings settings, String existingId) {
                    if (settings.getConfiguration() instanceof MerLocRunConfiguration) {
                        MerLocRunConfiguration runConfiguration = (MerLocRunConfiguration) settings.getConfiguration();
                        applyConfigChanges(runConfiguration, existingId);
                    }
                }

            });
            projectToMessageBusConnected.put(projectName, true);
        }

        return new MerLocRunConfiguration(runtimeManager, project, this);
    }

}
