package org.pathwayeditor.visualeditor.query;

import java.math.BigDecimal;

public class QueryData {
	private String term1;
	private String term2;
	private BigDecimal confScoreCutoff;
	
	
	public String getTerm1() {
		return term1;
	}
	public void setTerm1(String term1) {
		this.term1 = term1;
	}
	public String getTerm2() {
		return term2;
	}
	public void setTerm2(String term2) {
		this.term2 = term2;
	}
	public BigDecimal getConfScoreCutoff() {
		return confScoreCutoff;
	}
	public void setConfScoreCutoff(BigDecimal confScoreCutoff) {
		this.confScoreCutoff = confScoreCutoff;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((confScoreCutoff == null) ? 0 : confScoreCutoff.hashCode());
		result = prime * result + ((term1 == null) ? 0 : term1.hashCode());
		result = prime * result + ((term2 == null) ? 0 : term2.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof QueryData)) {
			return false;
		}
		QueryData other = (QueryData) obj;
		if (confScoreCutoff == null) {
			if (other.confScoreCutoff != null) {
				return false;
			}
		} else if (!confScoreCutoff.equals(other.confScoreCutoff)) {
			return false;
		}
		if (term1 == null) {
			if (other.term1 != null) {
				return false;
			}
		} else if (!term1.equals(other.term1)) {
			return false;
		}
		if (term2 == null) {
			if (other.term2 != null) {
				return false;
			}
		} else if (!term2.equals(other.term2)) {
			return false;
		}
		return true;
	}
	
	
	
}
