package org.pathwayeditor.visualeditor.commands;

import org.apache.log4j.Logger;
import org.pathwayeditor.businessobjects.drawingprimitives.IDrawingElementSelection;
import org.pathwayeditor.businessobjects.drawingprimitives.IDrawingNode;
import org.pathwayeditor.businessobjects.drawingprimitives.IGraphMomento;
import org.pathwayeditor.businessobjects.drawingprimitives.IModel;

public class ReparentSelectionCommand implements ICommand {
	private final Logger logger = Logger.getLogger(this.getClass());
	/** Shape to manipulate. */
	private IDrawingElementSelection selection;
	private IDrawingNode newParent;
	private IGraphMomento beforeChangeMomento;
	private IGraphMomento afterChangeMomento;

	public ReparentSelectionCommand(IDrawingNode newParent, IDrawingElementSelection selection) {
		this.newParent = newParent;
		this.selection = selection;
	}

	public void execute() {
		this.beforeChangeMomento = this.newParent.getModel().getCurrentState();
		this.newParent.getSubModel().moveHere(selection);
		this.afterChangeMomento = this.newParent.getModel().getCurrentState();
		if(logger.isDebugEnabled()){
			logger.debug("Moved shape: " +  this.selection + " to  " + this.newParent);
		}
		this.newParent = null;
		this.selection = null;
	}

	@Override
	public void redo(){
		IModel model = this.afterChangeMomento.getModel();
		model.restoreToState(afterChangeMomento);
		if(logger.isDebugEnabled()){
			logger.debug("redo: restored state: " + this.afterChangeMomento.getCreationDate());
		}
	}
	
	@Override
	public void undo(){
		IModel model = this.beforeChangeMomento.getModel();
		model.restoreToState(beforeChangeMomento);
		if(logger.isDebugEnabled()){
			logger.debug("undo: restored state: " + this.afterChangeMomento.getCreationDate());
		}
	}

	@Override
	public String toString(){
		StringBuilder builder = new StringBuilder(this.getClass().getSimpleName());
		builder.append("newParent=");
		builder.append(this.newParent);
		builder.append(", selection=");
		builder.append(this.selection);
		builder.append(")");
		return builder.toString();
	}
}
