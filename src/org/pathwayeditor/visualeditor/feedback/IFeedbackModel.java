package org.pathwayeditor.visualeditor.feedback;

import java.util.Iterator;

import org.pathwayeditor.visualeditor.controller.IDrawingElementController;
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

	IFeedbackElement getFeedbackElement(IDrawingElementController element);

	IFeedbackNodeBuilder getFeedbackNodeBuilder();

	IFeedbackLinkBuilder getFeedbackLinkBuilder();

	IFeedbackNode uniqueFeedbackNode();
}
