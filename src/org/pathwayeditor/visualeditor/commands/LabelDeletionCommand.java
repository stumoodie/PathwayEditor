package org.pathwayeditor.visualeditor.commands;

import org.pathwayeditor.businessobjects.drawingprimitives.IModel;
import org.pathwayeditor.businessobjects.drawingprimitives.IShapeAttribute;
import org.pathwayeditor.businessobjects.drawingprimitives.properties.IAnnotationProperty;

import uk.ac.ed.inf.graph.compound.ISubCompoundGraphFactory;
import uk.ac.ed.inf.graph.compound.ISubgraphRemovalBuilder;
import uk.ac.ed.inf.graph.state.IGraphState;

public class LabelDeletionCommand implements ICommand {
	private final IAnnotationProperty prop;
	private IGraphState newState;
	private IGraphState oldState;
	
	
	public LabelDeletionCommand(IAnnotationProperty prop) {
		this.prop = prop;
	}

	@Override
	public void execute() {
		ISubCompoundGraphFactory selnFact = this.prop.getOwner().getCurrentElement().getGraph().subgraphFactory();
		this.oldState = selnFact.getGraph().getCurrentState();
		IModel model =  ((IShapeAttribute)prop.getOwner().getCurrentElement().getAttribute()).getModel();
		selnFact.addElement(model.getLabelForProperty(prop).getCurrentElement());
		ISubgraphRemovalBuilder rmBuilder = selnFact.getGraph().newSubgraphRemovalBuilder();
		rmBuilder.setRemovalSubgraph(selnFact.createSubgraph());
		rmBuilder.removeSubgraph();
		this.newState = selnFact.getGraph().getCurrentState();
	}

	@Override
	public void undo() {
		this.oldState.getGraph().restoreState(oldState);
	}

	@Override
	public void redo() {
		this.newState.getGraph().restoreState(newState);
	}

}
