package org.pathwayeditor.visualeditor.commands;

public interface ICommandChangeEvent {
	public enum CommandChangeType { EXECUTE, UNDO, REDO, CLEAR }; 
	
	ICommand getLastExecutedCommand();

	CommandChangeType getChangeType();
	
}
