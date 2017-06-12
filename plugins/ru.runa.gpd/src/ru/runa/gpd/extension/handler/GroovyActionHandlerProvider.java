package ru.runa.gpd.extension.handler;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.events.HyperlinkEvent;

import ru.runa.gpd.Localization;
import ru.runa.gpd.extension.DelegableConfigurationDialog;
import ru.runa.gpd.extension.DelegableProvider;
import ru.runa.gpd.extension.HandlerArtifact;
import ru.runa.gpd.lang.model.Delegable;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.search.VariableSearchVisitor;
import ru.runa.gpd.ui.custom.JavaHighlightTextStyling;
import ru.runa.gpd.ui.custom.LoggingHyperlinkAdapter;
import ru.runa.gpd.ui.custom.SWTUtils;
import ru.runa.gpd.ui.dialog.ChooseVariableNameDialog;
import ru.runa.gpd.util.VariableUtils;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

public class GroovyActionHandlerProvider extends DelegableProvider {
    @Override
    protected DelegableConfigurationDialog createConfigurationDialog(Delegable delegable) {
        if (!HandlerArtifact.ACTION.equals(delegable.getDelegationType())) {
            return super.createConfigurationDialog(delegable);
        }
        ProcessDefinition definition = ((GraphElement) delegable).getProcessDefinition();
        return new ConfigurationDialog(delegable.getDelegationConfiguration(), definition.getVariables(true, true));
    }

    @Override
    public List<String> getUsedVariableNames(Delegable delegable) {
        String configuration = delegable.getDelegationConfiguration();
        if (Strings.isNullOrEmpty(configuration)) {
            return Lists.newArrayList();
        }
        List<String> result = Lists.newArrayList();
        if (delegable instanceof GraphElement) {
            for (Variable variable : ((GraphElement) delegable).getProcessDefinition().getVariables(true, true)) {
                if (Pattern.compile(String.format(VariableSearchVisitor.REGEX_SCRIPT_VARIABLE, variable.getScriptingName())).matcher("(" + configuration + ")").find()) {
                    result.add(variable.getName());
                }
            }
        }
        return result;
    }

    @Override
    public String getConfigurationOnVariableRename(Delegable delegable, Variable currentVariable, Variable previewVariable) {
        return delegable.getDelegationConfiguration().replaceAll(Pattern.quote(currentVariable.getScriptingName()),
                Matcher.quoteReplacement(previewVariable.getScriptingName()));
    }

    private class ConfigurationDialog extends DelegableConfigurationDialog {
        private final List<String> variableNames;

        public ConfigurationDialog(String initialValue, List<Variable> variables) {
            super(initialValue);
            this.variableNames = VariableUtils.getVariableNamesForScripting(variables);
        }

        @Override
        protected void createDialogHeader(Composite parent) {
            Composite composite = new Composite(parent, SWT.NONE);
            composite.setLayout(new GridLayout(2, false));
            composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            SWTUtils.createLink(parent, Localization.getString("button.insert_variable"), new LoggingHyperlinkAdapter() {

                @Override
                protected void onLinkActivated(HyperlinkEvent e) throws Exception {
                    ChooseVariableNameDialog dialog = new ChooseVariableNameDialog(variableNames);
                    String variableName = dialog.openDialog();
                    if (variableName != null) {
                        styledText.insert(variableName);
                        styledText.setFocus();
                        styledText.setCaretOffset(styledText.getCaretOffset() + variableName.length());
                    }
                }
            }).setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.HORIZONTAL_ALIGN_END));
        }

        @Override
        protected void createDialogFooter(Composite composite) {
            styledText.addLineStyleListener(new JavaHighlightTextStyling(variableNames));
        }
    }
}
