package org.pathwayeditor.visualeditor.feedback;

import java.util.Iterator;

import org.pathwayeditor.visualeditor.controller.IDrawingPrimitiveController;
import org.pathwayeditor.visualeditor.selection.ILinkSelection;
import org.pathwayeditor.visualeditor.selection.ISelectionRecord;

public interface IFeedbackModel {

	void rebuildIncludingHierarchy();
	
	void rebuildWithStrictSelection();
	
	void clear();
	
	ISelectionRecord getSelectionRecord();
	
	Iterator<IFeedbackNode> nodeIterator();
	
	Iterator<IFeedbackLink> linkIterator();
	
	Iterator<IFeedbackElement> elementIterator();

	void rebuildOnLinkSelection(ILinkSelection selection);

	IFeedbackLink uniqueFeedbackLink();

	IFeedbackElement getFeedbackElement(IDrawingPrimitiveController element);

	IFeedbackNodeBuilder getFeedbackNodeBuilder();

	IFeedbackNode uniqueFeedbackNode();
}
