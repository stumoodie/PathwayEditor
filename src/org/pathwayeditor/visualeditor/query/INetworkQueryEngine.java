package org.pathwayeditor.visualeditor.query;



public interface INetworkQueryEngine {

	void queryIDb();

	IQueryResult getQueryResult();

	void loadIDb();

	void setQueryObject(IQueryObject queryObject);
	
	IQueryObject getQueryObject();
}