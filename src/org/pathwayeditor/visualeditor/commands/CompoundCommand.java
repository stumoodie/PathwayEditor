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

	@Override
	public boolean isEmpty() {
		return this.commands.isEmpty();
	}

}
