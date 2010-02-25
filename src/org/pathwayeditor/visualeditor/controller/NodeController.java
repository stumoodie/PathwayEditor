package org.pathwayeditor.visualeditor.controller;



public abstract class NodeController extends DrawingPrimitiveController implements INodeController {

	protected NodeController(IViewControllerStore viewController, int index){
		super(viewController, index);
	}

}
