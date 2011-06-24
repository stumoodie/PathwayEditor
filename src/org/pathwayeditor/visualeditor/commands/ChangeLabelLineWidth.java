package org.pathwayeditor.visualeditor.commands;

import org.pathwayeditor.businessobjects.drawingprimitives.ILabelAttribute;

public class ChangeLabelLineWidth implements ICommand {
	private ILabelAttribute attribute;
	private double newLineWidth;
	private double oldLineWidth;

	public ChangeLabelLineWidth(ILabelAttribute attribute, double lineWidth) {
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
