package io.thundra.merloc.intellij;

import com.intellij.execution.Executor;
import com.intellij.execution.configurations.*;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.JDOMExternalizerUtil;
import com.intellij.openapi.util.WriteExternalException;
import io.thundra.merloc.intellij.config.ConfigManager;
import io.thundra.merloc.intellij.runtime.RuntimeConfig;
import io.thundra.merloc.intellij.runtime.RuntimeManager;
import org.apache.commons.lang3.StringUtils;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.Collection;
import java.util.Map;

/**
 * @author serkan
 */
public class MerLocRunConfiguration
        extends ModuleBasedConfiguration<JavaRunConfigurationModule, MerLocRunConfiguration> {

    private final RuntimeManager runtimeManager;
    private String brokerURL;
    private String connectionName = "default";
    private String apiKey;
    private String runtimeVersion = RuntimeManager.RUNTIME_DEFAULT_VERSION;

    public MerLocRunConfiguration(RuntimeManager runtimeManager,
                                  Project project, ConfigurationFactory configurationFactory) {
        super(new JavaRunConfigurationModule(project, true), configurationFactory);
        this.runtimeManager = runtimeManager;
        readConfigurations(null);
    }

    public String getBrokerURL() {
        return brokerURL;
    }

    public String getConnectionName() {
        return connectionName;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getRuntimeVersion() {
        return runtimeVersion;
    }

    private void readConfigurations(String profile) throws InvalidDataException {
        Map<String, Object> config = ConfigManager.loadConfigFromFile(profile);
        if (null != config) {
            brokerURL = (String) config.get("brokerURL");
            connectionName = (String) config.getOrDefault("connectionName", "default");
            apiKey = (String) config.get("apiKey");
            runtimeVersion = (String) config.getOrDefault("runtimeVersion", RuntimeManager.RUNTIME_DEFAULT_VERSION);
        }
    }

    @Override
    public void writeExternal(@NotNull final Element element) throws WriteExternalException {
        super.writeExternal(element);
        JDOMExternalizerUtil.writeField(element, "brokerURL", brokerURL);
        JDOMExternalizerUtil.writeField(element, "connectionName", connectionName);
        JDOMExternalizerUtil.writeField(element, "apiKey", apiKey);
        JDOMExternalizerUtil.writeField(element, "runtimeVersion", runtimeVersion);
        readConfigurations(getName());
    }

    @Override
    public void readExternal(@NotNull final Element element) throws InvalidDataException {
        super.readExternal(element);
        brokerURL = JDOMExternalizerUtil.readField(element, "brokerURL");
        connectionName = JDOMExternalizerUtil.readField(element, "connectionName");
        apiKey = JDOMExternalizerUtil.readField(element, "apiKey");
        runtimeVersion = JDOMExternalizerUtil.readField(element, "runtimeVersion");
        readConfigurations(getName());
    }

    @NotNull
    @Override
    public SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
        return new MerLocSettingsEditor(this, getProject());
    }

    void apply(RuntimeConfig runtimeConfig) {
        this.brokerURL = runtimeConfig.brokerURL();
        this.connectionName = runtimeConfig.connectionName();
        if (runtimeConfig.apiKey() != null) {
            this.apiKey = runtimeConfig.apiKey();
        }
        if (runtimeConfig.runtimeVersion() != null) {
            this.runtimeVersion = runtimeConfig.runtimeVersion();
        }
    }

    private RuntimeConfig createRuntimeConfig() {
        return RuntimeConfig.
                builder().
                brokerURL(brokerURL).
                connectionName(connectionName).
                apiKey(apiKey).
                runtimeVersion(runtimeVersion).
                build();
    }

    @Nullable
    @Override
    public RunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment executionEnvironment) {
        Project project = getProject();
        Module module = getConfigurationModule().getModule();
        RuntimeConfig runtimeConfig = createRuntimeConfig();
        return new MerLocRunProfileState(project, module, executionEnvironment, runtimeConfig, runtimeManager);
    }

    @Override
    public void checkConfiguration() throws RuntimeConfigurationException {
        if (StringUtils.isEmpty(brokerURL)) {
            throw new RuntimeConfigurationException("Broker URL must be specified");
        }
        if (StringUtils.isEmpty(connectionName)) {
            throw new RuntimeConfigurationException("Connection name must be specified");
        }
        if (StringUtils.isEmpty(runtimeVersion)) {
            throw new RuntimeConfigurationException("Runtime version must be specified");
        }
    }

    @Override
    public Collection<Module> getValidModules() {
        return getAllModules();
    }

}