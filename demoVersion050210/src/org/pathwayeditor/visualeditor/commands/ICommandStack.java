package org.pathwayeditor.visualeditor.commands;

public interface ICommandStack {

	void execute(ICommand cmd);
	
	void undo();
	
	boolean canUndo();
	
	void redo();
	
	boolean canRedo();
	
}
