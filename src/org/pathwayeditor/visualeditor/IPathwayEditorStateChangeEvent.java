package org.pathwayeditor.visualeditor;

import org.pathwayeditor.businessobjects.drawingprimitives.IModel;

public interface IPathwayEditorStateChangeEvent {
	enum StateChangeType { OPEN, CLOSED, EDITED, UNEDITED }; 
	
	PathwayEditor getSource();
	
	IModel getModel();
	
	StateChangeType getChangeType();
	
}
