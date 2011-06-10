package org.pathwayeditor.visualeditor.commands;

import org.pathwayeditor.businessobjects.drawingprimitives.IShapeAttribute;
import org.pathwayeditor.businessobjects.drawingprimitives.attributes.RGB;

public class ChangeShapeFillPropertyChange implements ICommand {
	private IShapeAttribute attribute;
	private RGB newFillColour;
	private RGB oldFillColour;

	public ChangeShapeFillPropertyChange(IShapeAttribute att, RGB newFillColour){
		this.attribute = att;
		this.newFillColour = newFillColour;
	}
	
	@Override
	public void execute() {
		this.oldFillColour = this.attribute.getFillColour(); 
		redo();
	}

	@Override
	public void undo() {
		this.attribute.setFillColour(this.oldFillColour);
	}

	@Override
	public void redo() {
		this.attribute.setFillColour(newFillColour);
	}

}
