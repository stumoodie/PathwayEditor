package org.pathwayeditor.visualeditor.commands;

public interface ICommand {

	void execute();
	
	void undo();
	
	void redo();
	
}
