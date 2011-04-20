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

import org.apache.log4j.Logger;
import org.pathwayeditor.businessobjects.drawingprimitives.IDrawingElement;
import org.pathwayeditor.businessobjects.drawingprimitives.IDrawingElementSelection;

import uk.ac.ed.inf.graph.compound.ICompoundGraphMoveBuilder;
import uk.ac.ed.inf.graph.state.IGraphState;
import uk.ac.ed.inf.graph.state.IRestorableGraph;

public class ReparentSelectionCommand implements ICommand {
	private final Logger logger = Logger.getLogger(this.getClass());
	/** Shape to manipulate. */
	private IDrawingElementSelection selection;
	private IDrawingElement newParent;
	private IGraphState beforeChangeMomento;
	private IGraphState afterChangeMomento;

	public ReparentSelectionCommand(IDrawingElement newParent, IDrawingElementSelection selection) {
		this.newParent = newParent;
		this.selection = selection;
	}

	@Override
	public void execute() {
		this.beforeChangeMomento = this.newParent.getGraphElement().getGraph().getCurrentState();
		ICompoundGraphMoveBuilder moveBuilder = this.newParent.getGraphElement().getChildCompoundGraph().newMoveBuilder();
		moveBuilder.setSourceSubgraph(selection.getSubgraph());
		moveBuilder.makeMove();
		this.afterChangeMomento = this.newParent.getGraphElement().getGraph().getCurrentState();
		if(logger.isDebugEnabled()){
			logger.debug("Moved shape: " +  this.selection + " to  " + this.newParent);
		}
		this.newParent = null;
		this.selection = null;
	}

	@Override
	public void redo(){
		IRestorableGraph model = this.afterChangeMomento.getGraph();
		model.restoreState(afterChangeMomento);
		if(logger.isDebugEnabled()){
			logger.debug("redo: restored state");
		}
	}
	
	@Override
	public void undo(){
		IRestorableGraph model = this.beforeChangeMomento.getGraph();
		model.restoreState(beforeChangeMomento);
		if(logger.isDebugEnabled()){
			logger.debug("undo: restored state");
		}
	}

	@Override
	public String toString(){
		StringBuilder builder = new StringBuilder(this.getClass().getSimpleName());
		builder.append("newParent=");
		builder.append(this.newParent);
		builder.append(", selection=");
		builder.append(this.selection);
		builder.append(")");
		return builder.toString();
	}
}
