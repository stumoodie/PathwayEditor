package org.pathwayeditor.visualeditor.commands;

import org.pathwayeditor.businessobjects.drawingprimitives.ILabelAttribute;
import org.pathwayeditor.figure.rendering.GenericFont;

public class ChangeLabelFontSize implements ICommand {
	private final ILabelAttribute attribute;
	private final double newFontSize;
	private GenericFont oldFont;
	
	public ChangeLabelFontSize(ILabelAttribute attribute, double fontSize) {
		this.attribute = attribute;
		this.newFontSize = fontSize;
	}

	@Override
	public void execute() {
		this.oldFont = this.attribute.getFont();
		redo();
	}

	@Override
	public void undo() {
		this.attribute.setFont(oldFont);
	}

	@Override
	public void redo() {
		this.attribute.setFont(this.oldFont.newSize(newFontSize));
	}

}
