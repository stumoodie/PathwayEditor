package org.pathwayeditor.visualeditor.controller;

import org.pathwayeditor.businessobjects.drawingprimitives.ITypedDrawingNodeAttribute;
import org.pathwayeditor.figure.rendering.IFigureRenderingController;

public interface IConnectingNodeController extends INodeController {

	IFigureRenderingController getFigureController();

	@Override
	ITypedDrawingNodeAttribute getAssociatedAttribute();
	
}
