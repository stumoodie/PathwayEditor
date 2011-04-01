package org.pathwayeditor.visualeditor.geometry;

public interface IGraphicalLinkTerminusDefinitionChangeEvent {
	public enum GraphicalLinkTerminusDefinitionChangeType { GAP, END_DECORATOR, SIZE };
	
	Object getOldValue();
	
	Object getNewValue();
	
	GraphicalLinkTerminusDefinitionChangeType getChangeType();
	
	IGraphicalLinkTerminusDefinition getSource();
}
