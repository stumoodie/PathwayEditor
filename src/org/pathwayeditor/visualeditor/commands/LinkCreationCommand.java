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

import org.pathwayeditor.businessobjects.drawingprimitives.IBendPointContainer;
import org.pathwayeditor.businessobjects.drawingprimitives.ILinkAttribute;
import org.pathwayeditor.businessobjects.drawingprimitives.ILinkEdge;
import org.pathwayeditor.businessobjects.drawingprimitives.ILinkEdgeFactory;
import org.pathwayeditor.businessobjects.drawingprimitives.IShapeNode;
import org.pathwayeditor.businessobjects.impl.facades.LinkEdgeFactoryFacade;
import org.pathwayeditor.businessobjects.typedefn.ILinkObjectType;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.visualeditor.geometry.ILinkPointDefinition;

import uk.ac.ed.inf.graph.compound.ICompoundGraph;
import uk.ac.ed.inf.graph.state.IGraphState;

public class LinkCreationCommand implements ICommand {
	private final IShapeNode srcShape;
	private final IShapeNode tgtShape;
	private final ILinkObjectType linkObjectType;
	private IGraphState originalState;
	private IGraphState createdState;
	private final ILinkPointDefinition linkPointDefinition;

	public LinkCreationCommand(IShapeNode srcNode, IShapeNode tgtNode,
			ILinkObjectType linkObjectType, ILinkPointDefinition linkDefinition) {
		this.srcShape = srcNode;
		this.tgtShape = tgtNode;
		this.linkObjectType = linkObjectType;
		this.linkPointDefinition = linkDefinition.getCopy();
	}

	@Override
	public void execute() {
		ICompoundGraph graph = this.srcShape.getGraphElement().getGraph();
		this.originalState = graph.getCurrentState();
		ILinkEdgeFactory fact = new LinkEdgeFactoryFacade(graph.edgeFactory());
		fact.setShapeNodePair(srcShape, tgtShape);
		fact.setObjectType(linkObjectType);
		ILinkEdge link = fact.createLinkEdge();
		ILinkAttribute linkAttribute = link.getAttribute();
		linkAttribute.getSourceTerminus().setLocation(linkPointDefinition.getSrcAnchorPosition());
		linkAttribute.getTargetTerminus().setLocation(linkPointDefinition.getTgtAnchorPosition());
		Iterator<Point> ptIter = linkPointDefinition.bendPointIterator();
		IBendPointContainer bpContainer = linkAttribute.getBendPointContainer();
		while(ptIter.hasNext()){
			Point pt = ptIter.next();
			bpContainer.createNewBendPoint(pt);
		}
		this.createdState = graph.getCurrentState();
	}

	@Override
	public void undo() {
		this.srcShape.getGraphElement().getGraph().restoreState(this.originalState);
	}

	@Override
	public void redo() {
		this.srcShape.getGraphElement().getGraph().restoreState(createdState);
	}

}
