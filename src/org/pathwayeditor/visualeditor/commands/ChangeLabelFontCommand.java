package org.pathwayeditor.visualeditor.commands;

import org.pathwayeditor.businessobjects.drawingprimitives.ILabelAttribute;
import org.pathwayeditor.figure.rendering.GenericFont;

public class ChangeLabelFontCommand implements ICommand {
	private final ILabelAttribute attribute;
	private final GenericFont newFont;
	private GenericFont oldFont;
	
	public ChangeLabelFontCommand(ILabelAttribute attribute, GenericFont newFont) {
		this.attribute = attribute;
		this.newFont = newFont;
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
		this.attribute.setFont(this.newFont);
	}

}
