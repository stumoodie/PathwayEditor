package org.pathwayeditor.curationtool.sentences;

import org.pathwayeditor.notations.annotator.ndom.ISentence;

public interface ISentenceSelectionChangeEvent {

	int getCurrentRowIdx();
	
	ISentence getOldSentence();
	
	ISentence getSelectedSentence();
	
}
