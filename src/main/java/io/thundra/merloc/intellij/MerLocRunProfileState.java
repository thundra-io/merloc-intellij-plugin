package io.thundra.merloc.intellij;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.application.ApplicationConfiguration;
import com.intellij.execution.configurations.JavaParameters;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import io.thundra.merloc.intellij.runtime.RuntimeConfig;
import io.thundra.merloc.intellij.runtime.RuntimeManager;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

/**
 * @author serkan
 */
public class MerLocRunProfileState extends ApplicationConfiguration.JavaApplicationCommandLineState {

    private static final String LAMBDA_RUNTIME_MAIN_CLASS_NAME =
            "io.thundra.merloc.aws.lambda.runtime.embedded.LambdaRuntime";
    private static final String BROKER_URL_CONFIG_ENV_VAR_NAME =
            "MERLOC_BROKER_URL";
    private static final String BROKER_CONNECTION_NAME_CONFIG_ENV_VAR_NAME =
            "MERLOC_BROKER_CONNECTION_NAME";

    private final RuntimeConfig runtimeConfig;
    private final RuntimeManager runtimeManager;

    public MerLocRunProfileState(Project project, Module module, ExecutionEnvironment environment,
                                 RuntimeConfig runtimeConfig, RuntimeManager runtimeManager) {
        super(createApplicationConfiguration(project, module, runtimeConfig), environment);
        this.runtimeConfig = runtimeConfig;
        this.runtimeManager = runtimeManager;
    }

    private static ApplicationConfiguration createApplicationConfiguration(
            Project project, Module module, RuntimeConfig runtimeConfig) {
        ApplicationConfiguration applicationConfiguration =
                new ApplicationConfiguration("merloc", project);
        applicationConfiguration.setMainClassName(LAMBDA_RUNTIME_MAIN_CLASS_NAME);
        applicationConfiguration.setModule(module);
        applicationConfiguration.setEnvs(new HashMap<>() {{
            put(BROKER_URL_CONFIG_ENV_VAR_NAME, runtimeConfig.brokerURL());
            put(BROKER_CONNECTION_NAME_CONFIG_ENV_VAR_NAME, runtimeConfig.connectionName());
        }});
        return applicationConfiguration;
    }

    @Override
    protected void setupJavaParameters(@NotNull JavaParameters params) throws ExecutionException {
        super.setupJavaParameters(params);
        try {
            String runtimeJarPath = runtimeManager.getRuntimeJarPath(runtimeConfig.runtimeVersion());
            params.getClassPath().add(runtimeJarPath);
        } catch (Exception e) {
            throw new ExecutionException(e);
        }
    }

}