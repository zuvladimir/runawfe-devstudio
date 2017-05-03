package ru.runa.gpd.formeditor.ftl.parameter;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import ru.runa.gpd.PropertyNames;
import ru.runa.gpd.formeditor.ftl.Component;
import ru.runa.gpd.formeditor.ftl.ComponentParameter;
import ru.runa.gpd.formeditor.ftl.ui.VariableListDialog;

import com.google.common.base.Joiner;

public class VariableListParameter extends ParameterType {
    private static final String VALUES_DELIM = ",";

    public VariableListParameter() {
        super(true, true);
    }

    @Override
    public PropertyDescriptor createPropertyDescriptor(Component component, ComponentParameter parameter, int propertyId) {
        return new VariableListPropertyDescriptor(propertyId, parameter.getLabel(), getVariableNames(parameter));
    }

    @Override
    public Object createEditor(Composite parent, Component component, final ComponentParameter parameter, final Object oldValue, final PropertyChangeListener listener) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        composite.setLayout(new GridLayout(2, false));
        final Text text = new Text(composite, SWT.READ_ONLY | SWT.BORDER);
        text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        if (oldValue != null) {
            text.setText(Joiner.on(VALUES_DELIM).join((List<String>) oldValue));
            text.setData(oldValue);
        }
        Button selectButton = new Button(composite, SWT.PUSH);
        selectButton.setText("...");
        selectButton.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
        final List<String> variableNames = getVariableNames(parameter);
        selectButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                List<String> value = (List<String>) text.getData();
                VariableListDialog dialog = new VariableListDialog(variableNames, value);
                List<String> result = dialog.openDialog();
                if (result != null) {
                    text.setText(Joiner.on(VALUES_DELIM).join(result));
                    text.setData(result);
                    listener.propertyChange(new PropertyChangeEvent(text, PropertyNames.PROPERTY_VALUE, oldValue, result));
                }
            }
        });
        return composite;
    }

}
