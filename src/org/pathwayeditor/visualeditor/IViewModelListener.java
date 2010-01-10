package org.pathwayeditor.visualeditor;

public interface IViewModelListener {

	void modelStructureChanged(IViewModelStructureEvent event);
	
	void modelLocationChanged(IViewModelLocationEvent event);
	
}
