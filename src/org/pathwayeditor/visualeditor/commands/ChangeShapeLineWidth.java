package org.pathwayeditor.visualeditor.commands;

import org.pathwayeditor.businessobjects.drawingprimitives.IShapeAttribute;

public class ChangeShapeLineWidth implements ICommand {
	private IShapeAttribute attribute;
	private double newLineWidth;
	private double oldLineWidth;

	public ChangeShapeLineWidth(IShapeAttribute attribute, double lineWidth) {
		this.attribute = attribute;
		this.newLineWidth= lineWidth;
	}

	@Override
	public void execute() {
		this.oldLineWidth = this.attribute.getLineWidth();
		redo();
	}

	@Override
	public void undo() {
		this.attribute.setLineWidth(oldLineWidth);
	}

	@Override
	public void redo() {
		this.attribute.setLineWidth(newLineWidth);
	}

}
