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

import java.util.Iterator;

import org.apache.log4j.Logger;
import org.pathwayeditor.businessobjects.drawingprimitives.ICanvasElementAttribute;
import org.pathwayeditor.figure.geometry.Point;

import uk.ac.ed.inf.graph.compound.ICompoundGraphCopyBuilder;
import uk.ac.ed.inf.graph.compound.ICompoundGraphElement;
import uk.ac.ed.inf.graph.compound.ICompoundNode;
import uk.ac.ed.inf.graph.compound.ISubCompoundGraph;
import uk.ac.ed.inf.graph.state.IGraphState;
import uk.ac.ed.inf.graph.state.IRestorableGraph;

public class CopySelectionCommand implements ICommand {
	private final Logger logger = Logger.getLogger(this.getClass());
	/** Shape to manipulate. */
	private ISubCompoundGraph selection;
	private ICompoundGraphElement newParent;
	private Point delta;
	private IGraphState beforeChangeMomento;
	private IGraphState afterChangeMomento;
	private ISubCompoundGraph copiedElementSelection;

	public CopySelectionCommand(ICompoundGraphElement newParent, ISubCompoundGraph selection, Point delta) {
		this.newParent = newParent;
		this.selection = selection;
		this.delta = delta;
	}

	@Override
	public void execute() {
		this.beforeChangeMomento = this.newParent.getGraph().getCurrentState();
		ICompoundGraphCopyBuilder moveBuilder = this.newParent.getChildCompoundGraph().newCopyBuilder();
		moveBuilder.setSourceSubgraph(selection);
		moveBuilder.makeCopy();
		this.copiedElementSelection = moveBuilder.getCopiedComponents();
		translateElements(this.delta);
		this.afterChangeMomento = this.newParent.getGraph().getCurrentState();
		if(logger.isDebugEnabled()){
			logger.debug("Moved shape: " +  this.selection + " to  " + this.newParent);
		}
		this.newParent = null;
		this.selection = null;
	}

	private void translateElements(Point newDelta) {
		// only need to modify top nodes as children and links will automatically be moved by the
		// controllers.
		Iterator<ICompoundNode> iter = this.copiedElementSelection.topNodeIterator();
		while(iter.hasNext()){
			ICompoundNode el = iter.next();
			((ICanvasElementAttribute)el.getAttribute()).translate(newDelta);
		}
	}

	@Override
	public void undo() {
		translateElements(delta.negate());
		IRestorableGraph model = this.beforeChangeMomento.getGraph();
		model.restoreState(beforeChangeMomento);
		if(logger.isDebugEnabled()){
			logger.debug("undo: restored state");
		}
	}

	@Override
	public void redo() {
		IRestorableGraph model = this.afterChangeMomento.getGraph();
		model.restoreState(afterChangeMomento);
		translateElements(delta);
		if(logger.isDebugEnabled()){
			logger.debug("redo: restored state");
		}
	}

}
