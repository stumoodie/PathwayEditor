package org.pathwayeditor.visualeditor.query;

import java.util.Iterator;

import org.pathwayeditor.notations.annotator.ndom.IInteraction;

public interface IPathwayQueryResult {

	int numInteractions();
	
	Iterator<IInteraction> resultIterator();
	
	IInteraction getInteraction(int idx);
}
