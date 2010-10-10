package org.pathwayeditor.visualeditor.controller;

import uk.ac.ed.inf.graph.compound.ICompoundNode;


public interface IRootController extends INodeController {

	@Override
	ICompoundNode getDrawingElement();

}
