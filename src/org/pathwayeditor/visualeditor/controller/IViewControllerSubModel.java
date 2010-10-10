package org.pathwayeditor.visualeditor.controller;

import java.util.Iterator;

import uk.ac.ed.inf.graph.compound.ICompoundNode;

public interface IViewControllerSubModel extends Comparable<IViewControllerSubModel> {

	IViewControllerModel getViewControllerModel();
	
	ICompoundNode getParentNode();
	
	Iterator<ICompoundNode> childNodeIterator();

	int getLevel();
	
}
