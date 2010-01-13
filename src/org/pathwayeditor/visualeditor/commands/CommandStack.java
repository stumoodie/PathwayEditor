package org.pathwayeditor.visualeditor.commands;

import java.util.Deque;
import java.util.LinkedList;

public class CommandStack implements ICommandStack {
	private final Deque<ICommand> undoStack;
	private final Deque<ICommand> redoStack;
	
	public CommandStack(){
		this.undoStack = new LinkedList<ICommand>();
		this.redoStack = new LinkedList<ICommand>();
	}
	
	
	@Override
	public boolean canRedo() {
		return !this.redoStack.isEmpty();
	}

	@Override
	public boolean canUndo() {
		return !this.undoStack.isEmpty();
	}

	@Override
	public void execute(ICommand cmd) {
		cmd.execute();
		this.undoStack.push(cmd);
		this.redoStack.clear();
	}

	@Override
	public void redo() {
		if(!this.canRedo()) throw new IllegalStateException("Cannot redo");
		
		ICommand cmd = this.redoStack.pop();
		cmd.redo();
		this.undoStack.push(cmd);
	}

	@Override
	public void undo() {
		if(!this.canUndo()) throw new IllegalStateException("Cannot undo");
		
		ICommand cmd = this.undoStack.pop();
		cmd.undo();
		this.redoStack.push(cmd);
	}

}
