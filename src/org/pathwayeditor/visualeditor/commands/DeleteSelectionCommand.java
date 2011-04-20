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

import org.pathwayeditor.visualeditor.selection.ISubgraphSelection;

import uk.ac.ed.inf.graph.compound.ICompoundGraph;
import uk.ac.ed.inf.graph.compound.ISubgraphRemovalBuilder;
import uk.ac.ed.inf.graph.state.IGraphState;

public class DeleteSelectionCommand implements ICommand {
	private ISubgraphSelection subgraphSelection;
	private IGraphState beforeChangeMomento;
	private IGraphState afterChangeMomento;

	public DeleteSelectionCommand(ISubgraphSelection subgraphSelection) {
		this.subgraphSelection = subgraphSelection;
	}

	@Override
	public void execute() {
		ICompoundGraph graph = subgraphSelection.getDrawingElementSelection().getSubgraph().getSuperGraph();
		this.beforeChangeMomento = graph.getCurrentState();
		ISubgraphRemovalBuilder removalbuilder = graph.newSubgraphRemovalBuilder();
		removalbuilder.setRemovalSubgraph(subgraphSelection.getDrawingElementSelection().getSubgraph());
		removalbuilder.removeSubgraph();
		this.afterChangeMomento = graph.getCurrentState();
	}

	@Override
	public void undo() {
		this.subgraphSelection.getDrawingElementSelection().getSubgraph().getSuperGraph().restoreState(beforeChangeMomento);
	}

	@Override
	public void redo() {
		this.subgraphSelection.getDrawingElementSelection().getSubgraph().getSuperGraph().restoreState(afterChangeMomento);
	}

}
