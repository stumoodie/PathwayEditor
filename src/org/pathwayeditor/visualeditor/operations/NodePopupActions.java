package org.pathwayeditor.visualeditor.operations;

import javax.swing.JComponent;
import javax.swing.JFrame;

import org.pathwayeditor.businessobjects.drawingprimitives.ICanvasElementAttributeVisitor;
import org.pathwayeditor.businessobjects.drawingprimitives.ILabelAttribute;
import org.pathwayeditor.businessobjects.drawingprimitives.ILinkAttribute;
import org.pathwayeditor.businessobjects.drawingprimitives.IRootAttribute;
import org.pathwayeditor.businessobjects.drawingprimitives.IShapeAttribute;
import org.pathwayeditor.visualeditor.behaviour.operation.INodePopupActions;
import org.pathwayeditor.visualeditor.commands.DeleteSelectionCommand;
import org.pathwayeditor.visualeditor.commands.ICommandStack;
import org.pathwayeditor.visualeditor.controller.ILabelController;
import org.pathwayeditor.visualeditor.controller.INodeController;
import org.pathwayeditor.visualeditor.controller.IShapeController;
import org.pathwayeditor.visualeditor.editingview.IShapePane;
import org.pathwayeditor.visualeditor.selection.ISelectionRecord;
import org.pathwayeditor.visualeditor.ui.LabelFormatDialog;
import org.pathwayeditor.visualeditor.ui.PropertyChangeDialog;
import org.pathwayeditor.visualeditor.ui.ShapeFormatDialog;

public class NodePopupActions implements INodePopupActions {
	private final IShapePane shapePane;
	private final ISelectionRecord selectionRecord;
	private final ICommandStack commandStack;

	
	public NodePopupActions(IShapePane shapePane, ISelectionRecord selectionRecord, ICommandStack commandStack){
		this.shapePane = shapePane;
		this.selectionRecord = selectionRecord;
		this.commandStack = commandStack;
	}
	
	private void deleteSelection() {
		this.commandStack.execute(new DeleteSelectionCommand(this.selectionRecord.getSubgraphSelection()));
	}

	@Override
	public void delete() {
		deleteSelection();
		selectionRecord.clear();
		shapePane.updateView();
	}

	@Override
	public void changeNodeFormat() {
        final JComponent invokerAsJComponent = (JComponent) shapePane;  
        final JFrame topLevel = (JFrame)invokerAsJComponent.getTopLevelAncestor();  
		final INodeController node = (INodeController)selectionRecord.getPrimarySelection().getPrimitiveController();
		node.getDrawingElement().getAttribute().visit(new ICanvasElementAttributeVisitor() {
			@Override
			public void visitShape(IShapeAttribute attribute) {
		        ShapeFormatDialog shapeFormatDialog = new ShapeFormatDialog(topLevel);
		        shapeFormatDialog.setLocationRelativeTo(invokerAsJComponent);
				shapeFormatDialog.setSelectedShape((IShapeController)node);
				shapeFormatDialog.setVisible(true);
				if(shapeFormatDialog.hasFormatChanged()){
					commandStack.execute(shapeFormatDialog.getCommand());
					shapePane.updateView();
				}
			}
			@Override
			public void visitRoot(IRootAttribute attribute) {
				throw new UnsupportedOperationException("Should not be called");
			}
			
			@Override
			public void visitLink(ILinkAttribute attribute) {
				throw new UnsupportedOperationException("Should not be called");
			}
			
			@Override
			public void visitLabel(ILabelAttribute attribute) {
		        LabelFormatDialog labelFormatDialog = new LabelFormatDialog(topLevel);
		        labelFormatDialog.setLocationRelativeTo(invokerAsJComponent);
				labelFormatDialog.setSelectedLabel((ILabelController)node);
				labelFormatDialog.setVisible(true);
				if(labelFormatDialog.hasFormatChanged()){
					commandStack.execute(labelFormatDialog.getCommand());
					selectionRecord.restoreSelection();
					shapePane.updateView();
				}
			}
		});
	}

	@Override
	public void properties() {
        JComponent invokerAsJComponent = (JComponent) shapePane;  
        JFrame topLevel = (JFrame)invokerAsJComponent.getTopLevelAncestor();  
        final PropertyChangeDialog propChangeDialog = new PropertyChangeDialog(topLevel);
        propChangeDialog.setLocationRelativeTo(invokerAsJComponent);
		final INodeController nodeController = (INodeController)selectionRecord.getPrimarySelection().getPrimitiveController();
		nodeController.getDrawingElement().getAttribute().visit(new ICanvasElementAttributeVisitor() {
			@Override
			public void visitRoot(IRootAttribute attribute) {
				throw new UnsupportedOperationException("Should not be called");
			}
			@Override
			public void visitShape(IShapeAttribute attribute) {
				IShapeController shape = (IShapeController)nodeController;
				propChangeDialog.setAnnotatedObject(shape.getDrawingElement().getAttribute().propertyIterator());
			}
			@Override
			public void visitLink(ILinkAttribute attribute) {
				throw new UnsupportedOperationException("Should not be called");
			}
			@Override
			public void visitLabel(ILabelAttribute attribute) {
				ILabelController shape = (ILabelController)nodeController;
				propChangeDialog.setAnnotatedObject(shape.getDrawingElement().getAttribute().getProperty());
			}
		});
		propChangeDialog.setVisible(true);
		if(propChangeDialog.hasFormatChanged()){
			commandStack.execute(propChangeDialog.getCommand());
			shapePane.updateView();
		}
	}

}
