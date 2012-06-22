package org.pathwayeditor.visualeditor.commands;

import org.pathwayeditor.businessobjects.drawingprimitives.ICanvasElementAttribute;
import org.pathwayeditor.businessobjects.drawingprimitives.IZOrderManager;
import org.pathwayeditor.businessobjects.drawingprimitives.IZOrderState;

public class ZOrderChangeCommand implements ICommand {
	public interface IZOrderCommandChangeOperation {
		
		void changeZOrder(IZOrderManager manager, ICanvasElementAttribute changedAtt);
		
	}
	
	private final ICanvasElementAttribute selection;
	private IZOrderState origState;
	private IZOrderState newState;
	private final IZOrderCommandChangeOperation changeOp;
	
	public ZOrderChangeCommand(ICanvasElementAttribute selection, IZOrderCommandChangeOperation op) {
		this.selection = selection;
		this.changeOp = op;
	}

	@Override
	public void execute() {
		ICanvasElementAttribute parent = (ICanvasElementAttribute)selection.getCurrentElement().getParent().getAttribute();
		this.origState = parent.getZorderManager().getCurrentState();
//		parent.getZorderManager().moveForwardOne(selection);
		this.changeOp.changeZOrder(parent.getZorderManager(), selection);
		this.newState = parent.getZorderManager().getCurrentState();
	}

	@Override
	public void undo() {
		ICanvasElementAttribute parent = (ICanvasElementAttribute)selection.getCurrentElement().getParent().getAttribute();
		parent.getZorderManager().restoreToState(origState);
	}

	@Override
	public void redo() {
		ICanvasElementAttribute parent = (ICanvasElementAttribute)selection.getCurrentElement().getParent().getAttribute();
		parent.getZorderManager().restoreToState(newState);
	}

}
