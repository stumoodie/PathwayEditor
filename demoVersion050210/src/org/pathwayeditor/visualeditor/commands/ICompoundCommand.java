package org.pathwayeditor.visualeditor.commands;

public interface ICompoundCommand extends ICommand {

	void addCommand(ICommand cmd);
	
	int numCommands();
	
}
