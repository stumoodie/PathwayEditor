package org.pathwayeditor.visualeditor.query;

public interface IQueryCompletedEvent {

	IPathwayQueryResult getQueryResult();
	
	QueryData getQueryData();

}
