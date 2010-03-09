package org.pathwayeditor.curationtool.sentences;

import org.pathwayeditor.curationtool.dataviewer.IRowDefn;
import org.pathwayeditor.curationtool.dataviewer.ITableRow;
import org.pathwayeditor.notations.annotator.ndom.ISentence;

public class SentenceRow implements ITableRow {
	private static final int STATUS_COL = 0;
	private static final int PMID_COL = 1;
	private static final int SENT_NUM_COL = 2;
	private static final int NAME_COL = 3;
	private static final int DESCN_COL = 5;
	private static final int SCORE_COL = 4;
	private static final String VALIDATED_FLAG = "V";
	private static final String FOCUS_FLAG = "F";
	private static final String INT_FLAG = "I";
	private static final String REL_FLAG = "R";

	private final ISentence sentence;
	private final IRowDefn rowDefn;
	

	public SentenceRow(ISentence sentence){
		this.sentence = sentence;
		this.rowDefn = new SentenceRowDefinition();
	}
	
	private String constructStatusString(){
		StringBuilder builder = new StringBuilder();
		if(sentence.hasSentenceBeenValidated()){
			builder.append(VALIDATED_FLAG);
		}
		if(sentence.isFocusNodeValid()){
			builder.append(FOCUS_FLAG);
		}
		if(sentence.isInteractingNodeValid()){
			builder.append(INT_FLAG);
		}
		if(sentence.isSentenceRelevant()){
			builder.append(REL_FLAG);
		}
		return builder.toString();
	}
	
	@Override
	public Object getColumnValue(int col) {
		Object retVal = null;
		if(col == STATUS_COL){
			retVal = constructStatusString();  
		}
		else if(col == PMID_COL){
			retVal = sentence.getPmid();  
		}
		else if(col == SENT_NUM_COL){
			retVal = sentence.getSentNum();  
		}
		else if(col == NAME_COL){
			retVal = sentence.getInteractionText();  
		}
		else if(col == DESCN_COL){
			retVal = sentence.getRawSentence();
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
		return this.sentence.compareTo(sentence);
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
