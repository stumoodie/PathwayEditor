package org.pathwayeditor.visualeditor.controller;



public abstract class NodeController extends DrawingElementController implements INodeController {

	protected NodeController(IViewControllerModel viewController, int index){
		super(viewController, index);
	}

}
