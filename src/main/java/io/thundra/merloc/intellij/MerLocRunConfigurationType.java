package io.thundra.merloc.intellij;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.openapi.util.IconLoader;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * @author serkan
 */
public class MerLocRunConfigurationType implements ConfigurationType {

    private static final Icon MERLOC_LOGO =
            IconLoader.getIcon("/META-INF/pluginIcon.svg", MerLocRunConfigurationType.class);

    @Override
    public String getDisplayName() {
        return "MerLoc";
    }

    @Override
    public String getConfigurationTypeDescription() {
        return "MerLoc Run Configuration Type";
    }

    @Override
    public Icon getIcon() {
        return MERLOC_LOGO;
    }

    @NotNull
    @Override
    public String getId() {
        return "merloc";
    }

    @Override
    public ConfigurationFactory[] getConfigurationFactories() {
        return new ConfigurationFactory[] {
                new MerLocConfigurationFactory(this)
        };
    }

}