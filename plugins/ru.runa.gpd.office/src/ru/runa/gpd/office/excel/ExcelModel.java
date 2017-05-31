package ru.runa.gpd.office.excel;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

import org.dom4j.Document;
import org.dom4j.Element;

import ru.runa.gpd.lang.ValidationError;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.lang.model.VariableUserType;
import ru.runa.gpd.office.FilesSupplierMode;
import ru.runa.gpd.office.InputOutputModel;
import ru.runa.gpd.office.Messages;
import ru.runa.gpd.util.XmlUtil;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

public class ExcelModel extends Observable {
    private final FilesSupplierMode mode;
    private final InputOutputModel inOutModel;
    public final List<ConstraintsModel> constraints = new ArrayList<ConstraintsModel>();

    public ExcelModel(FilesSupplierMode mode) {
        this(mode, new InputOutputModel());
    }

    public ExcelModel(FilesSupplierMode mode, InputOutputModel inOutModel) {
        this.mode = mode;
        this.inOutModel = inOutModel;
    }

    public InputOutputModel getInOutModel() {
        return inOutModel;
    }

    public static ExcelModel fromXml(String xml, FilesSupplierMode mode) {
        Document document = XmlUtil.parseWithoutValidation(xml);
        Element input = null;
        if (mode.isInSupported()) {
            input = document.getRootElement().element("input");
        }
        Element output = null;
        if (mode.isInSupported()) {
            output = document.getRootElement().element("output");
        }
        InputOutputModel inOutModel = InputOutputModel.deserialize(input, output);
        ExcelModel model = new ExcelModel(mode, inOutModel);
        List<Element> constraintsElements = document.getRootElement().elements("binding");
        for (Element constraintsElement : constraintsElements) {
            model.constraints.add(ConstraintsModel.deserialize(constraintsElement));
        }
        return model;
    }

    @Override
    public String toString() {
        Document document = XmlUtil.createDocument("config");
        Element root = document.getRootElement();
        inOutModel.serialize(document, root, mode);
        for (ConstraintsModel model : constraints) {
            model.serialize(document, root);
        }
        return XmlUtil.toString(document);
    }

    public void validate(GraphElement graphElement, List<ValidationError> errors) {
        List<Variable> variables = graphElement.getProcessDefinition().getChildren(Variable.class);
        for (ConstraintsModel constraintsModel : constraints) {
            if (Strings.isNullOrEmpty(constraintsModel.variableName)) {
                errors.add(ValidationError.createError(graphElement, Messages.getString("model.validation.xlsx.constraint.variable.empty")));
            }
            List<String> variablesNames = Lists.newArrayList();
            variablesNames.addAll(fillVariableNames(variables));
            String constraintsModelVariableName = constraintsModel.variableName;

            if (variablesNames.contains(constraintsModelVariableName) != true) {
                errors.add(ValidationError.createError(graphElement, Messages.getString("model.validation.xlsx.constraint.variable.nonexistent")
                        + " \"" + constraintsModelVariableName + "\""));
            }
        }
        inOutModel.validate(graphElement, mode, errors);
    }

    public static List<String> fillVariableNames(List<Variable> variables) {
        List<String> result = Lists.newArrayList();
        for (int i = 0; i < variables.size(); i++) {
            result.add(variables.get(i).getName());
            result.addAll(fillUsertypeFields(variables.get(i).getName(), variables.get(i).getUserType()));
        }
        return result;
    }

    public static List<String> fillUsertypeFields(String variableName, VariableUserType userType) {
        List<String> result = Lists.newArrayList();
        if (userType != null && userType.getAttributes() != null) {
            for (Variable currentVariable : userType.getAttributes()) {
                String complexVariable = "";
                if (variableName.endsWith(".") != true) {
                    if (currentVariable.getUserType() != null) {
                        complexVariable = variableName + "." + currentVariable.getUserType().getName();
                    } else {
                        complexVariable = variableName + "." + currentVariable.getName();
                    }
                    result.add(complexVariable);
                    if (currentVariable.getUserType() != null && currentVariable.getUserType().getAttributes() != null) {
                        result.addAll(fillUsertypeFields(complexVariable + ".", currentVariable.getUserType()));
                    }
                } else {
                    complexVariable = variableName + currentVariable.getName();
                    result.add(complexVariable);
                }
            }
        }
        return result;
    }
}
