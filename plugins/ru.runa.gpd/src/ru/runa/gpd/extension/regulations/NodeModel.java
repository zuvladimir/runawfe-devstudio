package ru.runa.gpd.extension.regulations;

import ru.runa.gpd.lang.model.FormNode;
import ru.runa.gpd.lang.model.Node;
import ru.runa.gpd.lang.model.SubprocessDefinition;
import ru.runa.gpd.lang.model.Swimlane;
import ru.runa.gpd.lang.model.SwimlanedNode;

public class NodeModel {
    private final Node node;
    private final NodeRegulationsProperties properties;
    private Swimlane swimlane;

    // TODO
    // private List<String> globalValidators

    public NodeModel(Node node) {
        this.node = node;
        this.properties = node.getRegulationsProperties();
        if (node instanceof SwimlanedNode) {
            this.swimlane = ((SwimlanedNode) node).getSwimlane();
        }
    }

    public Node getNode() {
        return node;
    }

    public NodeRegulationsProperties getProperties() {
        return properties;
    }

    public Swimlane getSwimlane() {
        return swimlane;
    }

    public boolean isInEmbeddedSubprocess() {
        return node.getProcessDefinition() instanceof SubprocessDefinition;
    }

    public boolean hasFormValidation() {
        if (node instanceof FormNode) {
            return ((FormNode) node).hasFormValidation();
        }
        return false;
    }
    //

}
