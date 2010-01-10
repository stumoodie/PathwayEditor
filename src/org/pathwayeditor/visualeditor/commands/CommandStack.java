package org.pathwayeditor.visualeditor.commands;

import java.util.Stack;

public class CommandStack implements ICommandStack {
	private final Stack<ICommand> undoStack;
	private final Stack<ICommand> redoStack;
	
	public CommandStack(){
		this.undoStack = new Stack<ICommand>();
		this.redoStack = new Stack<ICommand>();
	}
	
	
	@Override
	public boolean canRedo() {
		return !this.redoStack.empty();
	}

	@Override
	public boolean canUndo() {
		return !this.undoStack.empty();
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
