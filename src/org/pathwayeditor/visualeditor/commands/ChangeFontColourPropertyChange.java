package org.pathwayeditor.visualeditor.commands;

import org.pathwayeditor.businessobjects.drawingprimitives.ILabelAttribute;
import org.pathwayeditor.businessobjects.drawingprimitives.attributes.Colour;

public class ChangeFontColourPropertyChange implements ICommand {
	private final ILabelAttribute attribute;
	private final Colour newFontColour;
	private Colour oldFontColour; 
	
	public ChangeFontColourPropertyChange(ILabelAttribute attribute, Colour fontCol) {
		this.attribute = attribute;
		this.newFontColour = fontCol;
	}

	@Override
	public void execute() {
		this.oldFontColour = this.attribute.getFontColour();
		redo();
	}

	@Override
	public void undo() {
		this.attribute.setFontColour(oldFontColour);
	}

	@Override
	public void redo() {
		this.attribute.setFontColour(newFontColour);
	}

}
