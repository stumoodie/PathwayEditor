package org.pathwayeditor.visualeditor.commands;

import org.pathwayeditor.businessobjects.drawingprimitives.IDrawingNode;
import org.pathwayeditor.businessobjects.drawingprimitives.IGraphMomento;
import org.pathwayeditor.businessobjects.drawingprimitives.IRootNode;
import org.pathwayeditor.businessobjects.drawingprimitives.IShapeNode;
import org.pathwayeditor.businessobjects.drawingprimitives.IShapeNodeFactory;
import org.pathwayeditor.businessobjects.typedefn.IShapeObjectType;
import org.pathwayeditor.figure.geometry.Envelope;

public class ShapeCreationCommand implements ICommand {
	private IDrawingNode parentNode;
	private IShapeObjectType objectType;
	private Envelope bounds;
	private IGraphMomento createdState;
	private IGraphMomento originalState;
	
	public ShapeCreationCommand(IRootNode rootNode, IShapeObjectType shapeObjectType, Envelope bounds) {
		this.parentNode = rootNode;
		this.objectType = shapeObjectType;
		this.bounds = bounds; 
	}

	@Override
	public void execute() {
		this.originalState = this.parentNode.getModel().getCurrentState();
		IShapeNodeFactory fact = this.parentNode.getSubModel().shapeNodeFactory();
		fact.setObjectType(objectType);
		IShapeNode node = fact.createShapeNode();
		node.getAttribute().setBounds(bounds);
		this.createdState = node.getModel().getCurrentState();
	}

	@Override
	public void redo() {
		this.parentNode.getModel().restoreToState(createdState);
	}

	@Override
	public void undo() {
		this.parentNode.getModel().restoreToState(this.originalState);
	}

}
