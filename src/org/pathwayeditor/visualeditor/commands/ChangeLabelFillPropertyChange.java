package org.pathwayeditor.visualeditor.commands;

import org.pathwayeditor.businessobjects.drawingprimitives.ILabelAttribute;
import org.pathwayeditor.businessobjects.drawingprimitives.attributes.Colour;

public class ChangeLabelFillPropertyChange implements ICommand {
	private ILabelAttribute attribute;
	private Colour newFillColour;
	private Colour oldFillColour;

	public ChangeLabelFillPropertyChange(ILabelAttribute att, Colour newFillColour){
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
