package org.pathwayeditor.visualeditor.query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.pathwayeditor.notations.annotator.ndom.IInteraction;

public class PathwayQueryResult implements IPathwayQueryResult {
	private final List<IInteraction> resultsList;
	
	public PathwayQueryResult(IQueryResult iQueryResult){
		this.resultsList = new ArrayList<IInteraction>(iQueryResult.numInterations());
		Iterator<IInteraction> iter = iQueryResult.interationIter();
		while(iter.hasNext()){
			IInteraction intern = iter.next();
			this.resultsList.add(intern);
		}
	}
	
	public PathwayQueryResult() {
		this.resultsList = Collections.emptyList();
	}

	@Override
	public int numInteractions() {
		return this.resultsList.size();
	}

	@Override
	public Iterator<IInteraction> resultIterator() {
		return this.resultsList.iterator();
	}

	@Override
	public IInteraction getInteraction(int idx) {
		return this.resultsList.get(idx);
	}

}
