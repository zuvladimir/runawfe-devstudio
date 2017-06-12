package ru.runa.gpd.office;

import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;

import ru.runa.gpd.lang.ValidationError;
import ru.runa.gpd.lang.model.GraphElement;

import com.google.common.base.Strings;

public class InputOutputModel {
    public String inputPath;
    public String inputVariable;
    public String outputDir;
    public String outputVariable;
    public String outputFilename;

    public void serialize(Document document, Element parent, FilesSupplierMode mode) {
        if (mode.isInSupported()) {
            Element input = parent.addElement("input");
            if (!Strings.isNullOrEmpty(inputPath)) {
                input.addAttribute("path", inputPath);
            }
            if (!Strings.isNullOrEmpty(inputVariable)) {
                input.addAttribute("variable", inputVariable);
            }
        }
        if (mode.isOutSupported()) {
            Element output = parent.addElement("output");
            if (!Strings.isNullOrEmpty(outputDir)) {
                output.addAttribute("dir", outputDir);
            }
            if (!Strings.isNullOrEmpty(outputVariable)) {
                output.addAttribute("variable", outputVariable);
            }
            if (!Strings.isNullOrEmpty(outputFilename)) {
                output.addAttribute("fileName", outputFilename);
            }
        }
    }

    public static InputOutputModel deserialize(Element input, Element output) {
        InputOutputModel model = new InputOutputModel();
        if (input != null) {
            model.inputPath = input.attributeValue("path");
            model.inputVariable = input.attributeValue("variable");
        }
        if (output != null) {
            model.outputFilename = output.attributeValue("fileName");
            model.outputDir = output.attributeValue("dir");
            model.outputVariable = output.attributeValue("variable");
        }
        return model;
    }

    public void validate(GraphElement graphElement, FilesSupplierMode mode, List<ValidationError> errors) {
        if (mode.isInSupported() && Strings.isNullOrEmpty(inputPath) && Strings.isNullOrEmpty(inputVariable)) {
            errors.add(ValidationError.createError(graphElement, Messages.getString("model.validation.in.file.empty")));
        }
        if (mode.isOutSupported()) {
            if (Strings.isNullOrEmpty(outputVariable) && Strings.isNullOrEmpty(outputDir)) {
                errors.add(ValidationError.createError(graphElement, Messages.getString("model.validation.out.file.empty")));
            }
            if (Strings.isNullOrEmpty(outputFilename)) {
                errors.add(ValidationError.createError(graphElement, Messages.getString("model.validation.out.filename.empty")));
            }
        }
    }

}