package org.pathwayeditor.visualeditor.commands;

import org.pathwayeditor.businessobjects.drawingprimitives.IShapeAttribute;
import org.pathwayeditor.businessobjects.drawingprimitives.attributes.RGB;

public class ChangeShapeLinePropertyChange implements ICommand {
	private IShapeAttribute attribute;
	private RGB newLineColour;
	private RGB oldLineColour;

	public ChangeShapeLinePropertyChange(IShapeAttribute attribute, RGB lineRGB) {
		this.attribute = attribute;
		this.newLineColour = lineRGB;
	}

	@Override
	public void execute() {
		this.oldLineColour = this.attribute.getLineColour(); 
		redo();
	}

	@Override
	public void undo() {
		this.attribute.setLineColour(oldLineColour);
	}

	@Override
	public void redo() {
		this.attribute.setLineColour(newLineColour);
	}

}
