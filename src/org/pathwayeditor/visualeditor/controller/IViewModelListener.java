package org.pathwayeditor.visualeditor.controller;

public interface IViewModelListener {

	void modelStructureChanged(IViewModelStructureEvent event);
	
	void modelLocationChanged(IViewModelLocationEvent event);
	
}
