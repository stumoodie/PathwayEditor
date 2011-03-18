package org.pathwayeditor.visualeditor.commands;

import org.pathwayeditor.businessobjects.drawingprimitives.IDrawingElement;
import org.pathwayeditor.businessobjects.drawingprimitives.IShapeNode;
import org.pathwayeditor.businessobjects.drawingprimitives.IShapeNodeFactory;
import org.pathwayeditor.businessobjects.impl.facades.ShapeNodeFactoryFacade;
import org.pathwayeditor.businessobjects.typedefn.IShapeObjectType;
import org.pathwayeditor.figure.geometry.Envelope;

import uk.ac.ed.inf.graph.state.IGraphState;

public class ShapeCreationCommand implements ICommand {
	private final IDrawingElement parentNode;
	private final IShapeObjectType objectType;
	private final Envelope bounds;
	private IGraphState createdState;
	private IGraphState originalState;
	
	public ShapeCreationCommand(IDrawingElement rootNode, IShapeObjectType shapeObjectType, Envelope bounds) {
		this.parentNode = rootNode;
		this.objectType = shapeObjectType;
		this.bounds = bounds; 
	}

	@Override
	public void execute() {
		this.originalState = this.parentNode.getGraphElement().getGraph().getCurrentState();
		IShapeNodeFactory fact = new ShapeNodeFactoryFacade(parentNode.getGraphElement().getChildCompoundGraph().nodeFactory());
		fact.setObjectType(objectType);
		IShapeNode node = fact.createShapeNode();
		node.getAttribute().setBounds(bounds);
		this.createdState = node.getGraphElement().getGraph().getCurrentState();
	}

	@Override
	public void redo() {
		this.parentNode.getGraphElement().getGraph().restoreState(createdState);
	}

	@Override
	public void undo() {
		this.parentNode.getGraphElement().getGraph().restoreState(this.originalState);
	}

	@Override
	public String toString(){
		StringBuilder buf = new StringBuilder(this.getClass().getSimpleName());
		buf.append("(");
		buf.append("parentNodeIdx=");
		buf.append(parentNode.getGraphElement().getIndex());
		buf.append(",bounds=");
		buf.append(bounds);
		buf.append(",objectType=");
		buf.append(objectType.getName());
		return buf.toString();
	}
}
