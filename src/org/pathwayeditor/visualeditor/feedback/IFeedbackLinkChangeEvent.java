package org.pathwayeditor.visualeditor.feedback;

import org.pathwayeditor.visualeditor.geometry.ILinkPointDefinition;

public interface IFeedbackLinkChangeEvent {

	ILinkPointDefinition getOriginalLinkDefinition();
	
	ILinkPointDefinition getNewLinkDefintion();
	
}
