package org.pathwayeditor.visualeditor.controller;

import org.pathwayeditor.businessobjects.drawingprimitives.IRootNode;

public interface IRootController extends INodeController {

	@Override
	IRootNode getDrawingElement();

}
