package org.pathwayeditor.curationtool.sentences;

import org.pathwayeditor.curationtool.dataviewer.IRowDefn;
import org.pathwayeditor.curationtool.dataviewer.ITableRow;
import org.pathwayeditor.notations.annotator.ndom.ISentence;

public class SentenceRow implements ITableRow {
	private static final int NAME_COL = 0;
	private static final int DESCN_COL = 1;
	private static final int PMID_COL = 2;
	private static final int SCORE_COL = 3;

	private final ISentence sentence;
	private final IRowDefn rowDefn;
	

	public SentenceRow(ISentence sentence){
		this.sentence = sentence;
		this.rowDefn = new SentenceRowDefinition();
	}
	
	@Override
	public Object getColumnValue(int col) {
		Object retVal = null;
		if(col == NAME_COL ){
			retVal = sentence.getFocusText();  
		}
		else if(col == DESCN_COL){
			retVal = sentence.getMarkedUpSentence();
		}
		else if(col == PMID_COL){
			retVal = sentence.getPmid();
		}
		else if(col == SCORE_COL){
			retVal = sentence.getScore();
		}
		return retVal;
	}

	@Override
	public ITableRow getCopy() {
		return new SentenceRow(sentence);
	}

	@Override
	public IRowDefn getRowDefn() {
		return this.rowDefn;
	}

	@Override
	public void setColumnValue(int col, Object newValue) {
		// RO do nothing
	}

	public ISentence getSentence(){
		return this.sentence;
	}
	
	@Override
	public int compareTo(Object o) {
		ISentence sentence = ((SentenceRow)o).sentence;
		int retVal = this.sentence.getFocusText().compareTo(sentence.getFocusText());
		if(retVal == 0){
			retVal = this.sentence.getInteractionText().compareTo(sentence.getInteractionText());
			if(retVal == 0){
				retVal = this.sentence.getPmid().compareTo(sentence.getPmid());
			}
		}
		return retVal;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((sentence == null) ? 0 : sentence.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SentenceRow other = (SentenceRow) obj;
		if (sentence == null) {
			if (other.sentence != null)
				return false;
		} else if (!sentence.equals(other.sentence))
			return false;
		return true;
	}

}
