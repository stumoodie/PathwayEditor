package org.pathwayeditor.visualeditor.query;

import java.util.Iterator;
import java.util.List;

import org.pathwayeditor.notations.annotator.ndom.IInteraction;

public class QueryResult implements IQueryResult {
	private final IQueryObject queryObject;
	private final List<IInteraction> interations;

	public QueryResult(IQueryObject queryObject, List<IInteraction> interationList) {
		this.queryObject = queryObject;
		this.interations = interationList;
	}

	@Override
	public Iterator<IInteraction> interationIter() {
		return this.interations.iterator();
	}

	@Override
	public int numInterations() {
		return this.interations.size();
	}

	@Override
	public IQueryObject getQuery() {
		return this.queryObject;
	}

}
