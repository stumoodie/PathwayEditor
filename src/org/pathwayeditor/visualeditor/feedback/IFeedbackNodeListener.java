package org.pathwayeditor.visualeditor.feedback;

public interface IFeedbackNodeListener {

	void nodeTranslationEvent(IFeedbackNodeTranslationEvent e);
	
	void nodeResizeEvent(IFeedbackNodeResizeEvent e);
}
