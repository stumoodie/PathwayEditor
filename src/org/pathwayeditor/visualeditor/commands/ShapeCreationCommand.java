package org.pathwayeditor.visualeditor.commands;

import org.pathwayeditor.businessobjects.drawingprimitives.ICanvasElementAttribute;
import org.pathwayeditor.businessobjects.drawingprimitives.IShapeAttribute;
import org.pathwayeditor.businessobjects.drawingprimitives.IShapeAttributeFactory;
import org.pathwayeditor.businessobjects.typedefn.IShapeObjectType;
import org.pathwayeditor.figure.geometry.Envelope;

import uk.ac.ed.inf.graph.compound.ICompoundNode;
import uk.ac.ed.inf.graph.compound.ICompoundNodeFactory;
import uk.ac.ed.inf.graph.state.IGraphState;

public class ShapeCreationCommand implements ICommand {
	private final ICompoundNode parentNode;
	private final IShapeObjectType objectType;
	private final Envelope bounds;
	private IGraphState createdState;
	private IGraphState originalState;
	
	public ShapeCreationCommand(ICompoundNode rootNode, IShapeObjectType shapeObjectType, Envelope bounds) {
		this.parentNode = rootNode;
		this.objectType = shapeObjectType;
		this.bounds = bounds; 
	}

	@Override
	public void execute() {
		this.originalState = this.parentNode.getGraph().getCurrentState();
		ICompoundNodeFactory fact = this.parentNode.getChildCompoundGraph().nodeFactory();
		IShapeAttributeFactory attFact = ((ICanvasElementAttribute)this.parentNode.getAttribute()).getRootAttribute().shapeAttributeFactory();
		attFact.setObjectType(objectType);
		ICompoundNode node = fact.createNode();
		((IShapeAttribute)node.getAttribute()).setBounds(bounds);
		this.createdState = node.getGraph().getCurrentState();
	}

	@Override
	public void redo() {
		this.parentNode.getGraph().restoreState(createdState);
	}

	@Override
	public void undo() {
		this.parentNode.getGraph().restoreState(this.originalState);
	}

	@Override
	public String toString(){
		StringBuilder buf = new StringBuilder(this.getClass().getSimpleName());
		buf.append("(");
		buf.append("parentNodeIdx=");
		buf.append(parentNode.getIndex());
		buf.append(",bounds=");
		buf.append(bounds);
		buf.append(",objectType=");
		buf.append(objectType.getName());
		return buf.toString();
	}
}
