package ru.runa.gpd.editor;

import java.util.List;

import org.eclipse.core.resources.IFolder;
import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.graphiti.ui.internal.parts.ContainerShapeEditPart;

import ru.runa.gpd.Localization;
import ru.runa.gpd.editor.gef.part.graph.NodeGraphicalEditPart;
import ru.runa.gpd.lang.model.Action;
import ru.runa.gpd.lang.model.NamedGraphElement;
import ru.runa.gpd.lang.model.bpmn.TextDecorationNode;

import com.google.common.collect.Lists;

public class CopyAction extends SelectionAction {
    private final ProcessEditorBase editor;

    public CopyAction(ProcessEditorBase editor) {
        super(editor);
        this.editor = editor;
        setText(Localization.getString("button.copy"));
    }

    @Override
    protected boolean calculateEnabled() {
        return extractNodes().size() > 0;
    }

    private List<NamedGraphElement> extractNodes() {
        List<EditPart> editParts = editor.getGraphicalViewer().getSelectedEditParts();
        List<NamedGraphElement> result = Lists.newArrayList();
        for (EditPart editPart : editParts) {
            if (!(editPart instanceof AbstractGraphicalEditPart)) {
                continue;
            }
            NamedGraphElement node = null;
            if (editPart instanceof NodeGraphicalEditPart) {
                // gef way
                node = ((NodeGraphicalEditPart) editPart).getModel();
            } else if (editPart instanceof ContainerShapeEditPart) {
                // graphiti way
                ContainerShapeEditPart container = (ContainerShapeEditPart) editPart;
                node = (NamedGraphElement) container.getFeatureProvider().getBusinessObjectForPictogramElement(container.getPictogramElement());
                if (node instanceof Action) {
                    continue;
                }
            }
            // 1. If transition selected, it is not able to detect from NodeGraphicalEditPart/ContainerShapeEditPart and return null
            // 2. Text decoration for Start end End created automatically and don't need copy too.
            if (node != null && !(node instanceof TextDecorationNode)) {
                result.add(node);
            }
        }
        return result;
    }

    @Override
    public void run() {
        CopyBuffer copyBuffer = new CopyBuffer((IFolder) editor.getDefinitionFile().getParent(), editor.getDefinition().getLanguage(),
                extractNodes(), editor.toString(), ((FigureCanvas) editor.getGraphicalViewer().getControl()).getViewport().getViewLocation().getCopy());
        copyBuffer.setToClipboard();
    }
}