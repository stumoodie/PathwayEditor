/*
  Licensed to the Court of the University of Edinburgh (UofE) under one
  or more contributor license agreements.  See the NOTICE file
  distributed with this work for additional information
  regarding copyright ownership.  The UofE licenses this file
  to you under the Apache License, Version 2.0 (the
  "License"); you may not use this file except in compliance
  with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing,
  software distributed under the License is distributed on an
  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  KIND, either express or implied.  See the License for the
  specific language governing permissions and limitations
  under the License.
*/
package org.pathwayeditor.visualeditor.commands;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.pathwayeditor.visualeditor.commands.ICommandChangeEvent.CommandChangeType;

public class CommandStack implements ICommandStack {
	private final Logger logger = Logger.getLogger(this.getClass());
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
		if(logger.isDebugEnabled()){
			logger.debug("Executing cmd=" + cmd);
		}
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
		if(logger.isDebugEnabled()){
			logger.debug("Executing redo cmd=" + cmd);
		}
		this.undoStack.push(cmd);
		notifyChange(CommandChangeType.REDO, cmd);
	}

	@Override
	public void undo() {
		if(!this.canUndo()) throw new IllegalStateException("Cannot undo");
		
		ICommand cmd = this.undoStack.pop();
		cmd.undo();
		if(logger.isDebugEnabled()){
			logger.debug("Executing undo cmd=" + cmd);
		}
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
