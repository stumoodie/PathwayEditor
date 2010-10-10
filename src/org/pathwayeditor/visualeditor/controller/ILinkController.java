package org.pathwayeditor.visualeditor.controller;

import org.pathwayeditor.visualeditor.geometry.ILinkPointDefinition;

import uk.ac.ed.inf.graph.compound.ICompoundEdge;

public interface ILinkController extends IDrawingElementController {

	ILinkPointDefinition getLinkDefinition();
	
	@Override
	ICompoundEdge getDrawingElement();
	
}
