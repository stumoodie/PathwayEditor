package org.pathwayeditor.visualeditor.query;

import java.net.URI;
import java.util.Iterator;

public interface IQueryObject {

	double getScoreCutoff(); 
	
	Iterator<URI> nopUriIterator();
	
	int numNopUris();
	
	int getDiameter();
	
}
