package org.pathwayeditor.visualeditor.query;

import java.net.URI;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class QueryBuilder implements IQueryBuilder {
	private static final double DEFAULT_PRTR_CUTOFF = 6.906e-06;
	private static final int DEFAULT_DIAMETER = 1;

	private double minPrtrScore = DEFAULT_PRTR_CUTOFF;
	private int diameter = DEFAULT_DIAMETER;
	private final List<URI> nopList;
	private IQueryObject queryObject;
	
	public QueryBuilder(){
		this.nopList = new LinkedList<URI>();
	}
	
	
	@Override
	public void addNopUri(URI nopUri) {
		this.nopList.add(nopUri);
	}

	@Override
	public void setCutoff(double cutoff) {
		this.minPrtrScore = cutoff;
	}

	@Override
	public void setDiameter(int diameter) {
		this.diameter = diameter;
	}

	@Override
	public void buildQuery() {
		this.queryObject = new QueryObject(this.minPrtrScore, this.diameter, this.nopList);
	}

	@Override
	public IQueryObject getCurrentQuery() {
		return this.queryObject;
	}


	@Override
	public Iterator<URI> nopIds() {
		return this.nopList.iterator();
	}


	@Override
	public int numNopIds() {
		return this.nopList.size();
	}


	@Override
	public double getCutoff() {
		return this.minPrtrScore;
	}


	@Override
	public void reset() {
		this.diameter = DEFAULT_DIAMETER;
		this.minPrtrScore = DEFAULT_PRTR_CUTOFF;
		this.nopList.clear();
	}

}
