package org.pathwayeditor.visualeditor.controller;

import java.util.List;

import org.pathwayeditor.figure.geometry.Envelope;
import org.pathwayeditor.visualeditor.geometry.IIntersectionCalculator;

import uk.ac.ed.inf.graph.compound.ICompoundGraph;

public interface IViewControllerModel extends IViewControllerCollection {

	ICompoundGraph getDomainModel();
	
	IRootController getRootNode();

	void activate();
	
	void deactivate();
	
	boolean isActive();

	Envelope getCanvasBounds();
	
	IIntersectionCalculator getIntersectionCalculator();
	
//	/**
//	 * Returns the collection of controllers that were the result of the last operation performed that changed the structure of the
//	 * domain model compound graph.
//	 * @return the view controller collection, which cannot be null.
//	 */
//	IViewControllerCollection getLastOperationResult();

	void addViewControllerChangeListener(IViewControllerChangeListener listener);
	
	void removeViewControllerChangeListener(IViewControllerChangeListener listener);
	
	List<IViewControllerChangeListener> getViewControllerChangeListeners();
}
