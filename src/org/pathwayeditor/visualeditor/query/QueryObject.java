package org.pathwayeditor.visualeditor.query;

import java.net.URI;
import java.util.Iterator;
import java.util.List;


public class QueryObject implements IQueryObject {
	private final List<URI> nopList;
	private final double score;
	private final int diameter;
	
	QueryObject(double score, int diameter, List<URI> nopList){
		this.score = score;
		this.diameter = diameter;
		this.nopList = nopList;
	}
	
	@Override
	public double getScoreCutoff() {
		return this.score;
	}

	@Override
	public Iterator<URI> nopUriIterator() {
		return this.nopList.iterator();
	}

	@Override
	public int getDiameter() {
		return this.diameter;
	}

	@Override
	public int numNopUris() {
		return this.nopList.size();
	}


}
