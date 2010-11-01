package org.pathwayeditor.visualeditor.selection;

import org.pathwayeditor.visualeditor.controller.INodeController;

public interface INodeSelection extends ISelection {

	@Override
	INodeController getPrimitiveController();

}
