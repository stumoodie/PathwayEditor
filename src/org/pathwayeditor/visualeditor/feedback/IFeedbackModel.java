package org.pathwayeditor.visualeditor.feedback;

import java.util.Iterator;

import org.pathwayeditor.visualeditor.selection.ISelectionRecord;

public interface IFeedbackModel {

	void rebuildIncludingHierarchy();
	
	void rebuildWithStrictSelection();
	
	void clear();
	
	ISelectionRecord getSelectionRecord();
	
	Iterator<IFeedbackNode> nodeIterator();
	
}
