package org.pathwayeditor.visualeditor.query;

import java.util.Iterator;

import org.pathwayeditor.notations.annotator.ndom.IInteraction;

public interface IQueryResult {

	Iterator<IInteraction> interationIter();
	
	int numInterations();
	
	IQueryObject getQuery();
	
}
