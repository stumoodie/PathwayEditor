package org.pathwayeditor.visualeditor.commands;

import java.util.LinkedList;
import java.util.List;

public class CompoundCommand implements ICompoundCommand {
	private final List<ICommand> commands;
	
	public CompoundCommand(){
		this.commands = new LinkedList<ICommand>();
	}
	
	@Override
	public void execute() {
		for(ICommand cmd : this.commands){
			cmd.execute();
		}
	}

	@Override
	public void redo() {
		for(ICommand cmd : this.commands){
			cmd.redo();
		}
	}

	@Override
	public void undo() {
		for(ICommand cmd : this.commands){
			cmd.undo();
		}
	}

	@Override
	public void addCommand(ICommand cmd) {
		this.commands.add(cmd);
	}

	@Override
	public int numCommands() {
		return this.commands.size();
	}

}
