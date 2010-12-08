package org.pathwayeditor.visualeditor.query;

import java.net.URI;
import java.util.Iterator;

public interface IQueryBuilder {

	void addNopUri(URI nopUri);
	
	Iterator<URI> nopIds();
	
	int numNopIds();
	
	void setCutoff(double cutoff);
	
	void setDiameter(int diameter);
	
	void buildQuery();
	
	IQueryObject getCurrentQuery();

	double getCutoff();
	
	void reset();
}
