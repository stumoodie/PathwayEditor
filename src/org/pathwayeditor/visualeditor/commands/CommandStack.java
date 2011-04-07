package org.pathwayeditor.visualeditor.commands;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import org.pathwayeditor.visualeditor.commands.ICommandChangeEvent.CommandChangeType;

public class CommandStack implements ICommandStack {
	private final Deque<ICommand> undoStack;
	private final Deque<ICommand> redoStack;
	private final List<ICommandChangeListener> listeners = new LinkedList<ICommandChangeListener>();
	
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
		notifyChange(CommandChangeType.EXECUTE, cmd);
	}

	private void notifyChange(final CommandChangeType type, final ICommand cmd) {
		ICommandChangeEvent e = new ICommandChangeEvent(){
			@Override
			public ICommand getLastExecutedCommand() {
				return cmd;
			}

			@Override
			public CommandChangeType getChangeType() {
				return type;
			}
		};
		for(ICommandChangeListener l : this.listeners){
			l.notifyCommandChange(e);
		}
	}


	@Override
	public void redo() {
		if(!this.canRedo()) throw new IllegalStateException("Cannot redo");
		
		ICommand cmd = this.redoStack.pop();
		cmd.redo();
		this.undoStack.push(cmd);
		notifyChange(CommandChangeType.REDO, cmd);
	}

	@Override
	public void undo() {
		if(!this.canUndo()) throw new IllegalStateException("Cannot undo");
		
		ICommand cmd = this.undoStack.pop();
		cmd.undo();
		this.redoStack.push(cmd);
		notifyChange(CommandChangeType.UNDO, cmd);
	}


	@Override
	public void addCommandChangeListener(ICommandChangeListener l) {
		this.listeners.add(l);
	}


	@Override
	public void removeCommandChangeListener(ICommandChangeListener l) {
		this.listeners.remove(l);
	}


	@Override
	public List<ICommandChangeListener> getCommandChangeListeners() {
		return new ArrayList<ICommandChangeListener>(this.listeners);
	}


	@Override
	public boolean isEmpty() {
		return this.redoStack.isEmpty() && this.undoStack.isEmpty();
	}


	@Override
	public void clear() {
		this.redoStack.clear();
		this.undoStack.clear();
		notifyChange(CommandChangeType.CLEAR, null);
	}

}
