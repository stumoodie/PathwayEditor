package org.pathwayeditor.visualeditor.commands;

import java.util.List;

public interface ICommandStack {

	void execute(ICommand cmd);
	
	void undo();
	
	boolean canUndo();
	
	void redo();
	
	boolean canRedo();

	void addCommandChangeListener(ICommandChangeListener l);

	void removeCommandChangeListener(ICommandChangeListener l);

	List<ICommandChangeListener> getCommandChangeListeners();

	boolean isEmpty();

	void clear();
}
