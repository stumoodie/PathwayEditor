package org.pathwayeditor.visualeditor.behaviour;

public interface IViewBehaviourModeChangeEvent {
	enum ModeType { SELECTION, SHAPE_CREATION, LINK_CREATION };
	
	ModeType getOldModeType();
	
	ModeType getNewModeType();
	
};
