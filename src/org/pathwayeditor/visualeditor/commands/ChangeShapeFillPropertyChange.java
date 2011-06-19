package org.pathwayeditor.visualeditor.commands;

import org.pathwayeditor.businessobjects.drawingprimitives.IShapeAttribute;
import org.pathwayeditor.businessobjects.drawingprimitives.attributes.Colour;
import org.pathwayeditor.businessobjects.drawingprimitives.attributes.Colour;

public class ChangeShapeFillPropertyChange implements ICommand {
	private IShapeAttribute attribute;
	private Colour newFillColour;
	private Colour oldFillColour;

	public ChangeShapeFillPropertyChange(IShapeAttribute att, Colour newFillColour){
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
