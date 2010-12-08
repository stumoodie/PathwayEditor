package org.pathwayeditor.visualeditor.query;





public interface IPathwayQueryController {

	void setQueryData(QueryData queryData);
	
	QueryData getQueryData();
	
	void runQuery();

	IPathwayQueryResult getQueryResult();

	void initialise();
}