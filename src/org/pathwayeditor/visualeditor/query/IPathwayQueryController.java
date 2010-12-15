package org.pathwayeditor.visualeditor.query;

import java.util.List;





public interface IPathwayQueryController {

	void setQueryData(QueryData queryData);
	
	QueryData getQueryData();
	
	void runQuery();

	IPathwayQueryResult getQueryResult();

	void initialise();

	void addQueryEventListener(IQueryEventListener listener);

	void removeQueryEventListener(IQueryEventListener listener);

	List<IQueryEventListener> getQueryEventListeners();
}