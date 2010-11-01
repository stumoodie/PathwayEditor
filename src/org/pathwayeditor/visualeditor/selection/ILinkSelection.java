package org.pathwayeditor.visualeditor.selection;

import org.pathwayeditor.visualeditor.controller.ILinkController;

public interface ILinkSelection extends ISelection {

	@Override
	public ILinkController getPrimitiveController();
	
}
