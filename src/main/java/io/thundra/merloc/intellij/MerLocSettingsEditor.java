package io.thundra.merloc.intellij;

import com.intellij.application.options.ModuleDescriptionsComboBox;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.impl.RunManagerImpl;
import com.intellij.execution.ui.ConfigurationModuleSelector;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.ui.JBColor;
import com.intellij.ui.SideBorder;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UI;
import io.thundra.merloc.intellij.runtime.RuntimeConfig;
import org.jetbrains.annotations.NotNull;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author serkan
 */
public class MerLocSettingsEditor extends SettingsEditor<MerLocRunConfiguration> {

    private final MerLocRunConfiguration runConfiguration;
    private final JPanel mainPanel;
    private final JTextArea argsArea = new JTextArea();
    private final JTextField brokerURL = new JTextField();
    private final JTextField connectionName = new JTextField();
    private final JTextField apiKey = new JTextField();
    private final ConfigurationModuleSelector moduleSelector;
    private final List<String> configItems = new ArrayList<>();

    public MerLocSettingsEditor(MerLocRunConfiguration runConfiguration, Project project) {
        this.runConfiguration = runConfiguration;

        RunManagerImpl manager = RunManagerImpl.getInstanceImpl(project);
        List<String> rc =
                manager.getAllConfigurationsList().
                    stream().
                        map(RunProfile::getName).
                        collect(Collectors.toList());
        configItems.addAll(rc);

        GridBagConstraints gc =
                new GridBagConstraints(
                        0, 0, 3, 1, 0, 0,
                        GridBagConstraints.LINE_START, GridBagConstraints.NONE,
                        JBUI.insets(2, 0), 0, 0);
        mainPanel = createModePanel(gc);

        argsArea.setLineWrap(true);
        argsArea.setWrapStyleWord(true);
        argsArea.setRows(2);
        argsArea.setEditable(false);
        argsArea.setBorder(new SideBorder(JBColor.border(), SideBorder.ALL));
        argsArea.setMinimumSize(argsArea.getPreferredSize());

        gc.gridy++;
        gc.gridx = 0;
        gc.gridwidth = 3;
        gc.weightx = 1.0;
        gc.fill = GridBagConstraints.HORIZONTAL;

        ModuleDescriptionsComboBox moduleCombo = new ModuleDescriptionsComboBox();
        moduleSelector = new ConfigurationModuleSelector(project, moduleCombo);
        mainPanel.add(UI.PanelFactory.panel(moduleCombo).withLabel("Use module classpath: ").createPanel(), gc);

        gc.gridy++;
        gc.gridx = 0;
        gc.weighty = 1.0;
        gc.fill = GridBagConstraints.REMAINDER;

        mainPanel.add(new JPanel(), gc);
    }

    @Override
    protected void resetEditorFrom(@NotNull MerLocRunConfiguration rc) {
        brokerURL.setText(rc.getBrokerURL());
        connectionName.setText(rc.getConnectionName());
        apiKey.setText(rc.getApiKey());
        moduleSelector.reset(rc);
    }

    private RuntimeConfig createRuntimeConfig() {
        String brokerURLValue = brokerURL.getText().trim();
        String connectionNameValue = connectionName.getText().trim();
        String apiKeyValue = apiKey.getText().trim();
        return RuntimeConfig.
                builder().
                brokerURL(brokerURLValue).
                connectionName(connectionNameValue).
                apiKey(apiKeyValue).
                build();
    }

    @Override
    protected void applyEditorTo(@NotNull MerLocRunConfiguration rc) throws ConfigurationException {
        try {
            Module module = moduleSelector.getModule();
            if (module == null) {
                throw new ConfigurationException("Module must be selected");
            }
            moduleSelector.applyTo(rc);

            RuntimeConfig runtimeConfig = createRuntimeConfig();
            if (StringUtils.isEmpty(runtimeConfig.brokerURL())) {
                throw new ConfigurationException("Broker URL must be specified");
            }
            if (StringUtils.isEmpty(runtimeConfig.connectionName())) {
                throw new ConfigurationException("Connection name must be specified");
            }

            rc.apply(runtimeConfig);
        } catch (ConfigurationException e) {
            throw e;
        } catch (Exception e) {
            throw new ConfigurationException(e.getMessage());
        }
    }

    @NotNull
    @Override
    protected JComponent createEditor() {
        return mainPanel;
    }

    private JPanel createModePanel(GridBagConstraints gc) {
        JPanel panel = new JPanel(new GridBagLayout());

        JLabel brokerURLLabel = new JLabel("Broker URL:");
        JLabel connectionNameLabel = new JLabel("Connection Name:");
        JLabel apiKeyLabel = new JLabel("API Key:");

        gc.gridy++;
        gc.gridx = 0;
        gc.gridwidth = 1;
        gc.fill = GridBagConstraints.NONE;
        panel.add(brokerURLLabel, gc);

        gc.gridx++;
        gc.gridwidth = 3;
        gc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(brokerURL, gc);

        gc.gridy++;
        gc.gridx = 0;
        gc.gridwidth = 1;
        gc.fill = GridBagConstraints.NONE;
        panel.add(connectionNameLabel, gc);

        gc.gridx++;
        gc.gridwidth = 3;
        gc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(connectionName, gc);

        gc.gridy++;
        gc.gridx = 0;
        gc.gridwidth = 1;
        gc.fill = GridBagConstraints.NONE;
        panel.add(apiKeyLabel, gc);

        gc.gridx++;
        gc.gridwidth = 3;
        gc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(apiKey, gc);

        return panel;
    }

}