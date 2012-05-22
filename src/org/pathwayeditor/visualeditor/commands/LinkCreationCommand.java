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
import org.pathwayeditor.businessobjects.drawingprimitives.IDrawingElement;
import org.pathwayeditor.businessobjects.drawingprimitives.ILinkAttribute;
import org.pathwayeditor.businessobjects.drawingprimitives.ILinkAttributeFactory;
import org.pathwayeditor.businessobjects.drawingprimitives.IRootAttribute;
import org.pathwayeditor.businessobjects.drawingprimitives.IShapeAttribute;
import org.pathwayeditor.businessobjects.drawingprimitives.IShapeAttributeFactory;
import org.pathwayeditor.businessobjects.typedefn.ILinkObjectType;
import org.pathwayeditor.businessobjects.typedefn.IShapeObjectType;
import org.pathwayeditor.figure.geometry.Envelope;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.visualeditor.geometry.ILinkPointDefinition;

import uk.ac.ed.inf.graph.compound.CompoundNodePair;
import uk.ac.ed.inf.graph.compound.ICompoundEdge;
import uk.ac.ed.inf.graph.compound.ICompoundEdgeFactory;
import uk.ac.ed.inf.graph.compound.ICompoundGraph;
import uk.ac.ed.inf.graph.compound.ICompoundGraphElement;
import uk.ac.ed.inf.graph.compound.ICompoundGraphElementVisitor;
import uk.ac.ed.inf.graph.compound.ICompoundNode;
import uk.ac.ed.inf.graph.compound.ICompoundNodeFactory;
import uk.ac.ed.inf.graph.state.IGraphState;

public class LinkCreationCommand implements ICommand {
	private final ICompoundGraphElement srcShape;
	private final ICompoundGraphElement tgtShape;
	private final ILinkObjectType linkObjectType;
	private IGraphState originalState;
	private IGraphState createdState;
	private final ILinkPointDefinition linkPointDefinition;

	public LinkCreationCommand(IDrawingElement srcNode, IDrawingElement tgtNode,
			ILinkObjectType linkObjectType, ILinkPointDefinition linkDefinition) {
		this.srcShape = srcNode.getGraphElement();
		this.tgtShape = tgtNode.getGraphElement();
		this.linkObjectType = linkObjectType;
		this.linkPointDefinition = linkDefinition.getCopy();
	}

	@Override
	public void execute() {
		ICompoundGraph graph = this.srcShape.getGraph();
		this.originalState = graph.getCurrentState();
		ICompoundEdgeFactory edgeFact = graph.edgeFactory();
		IRootAttribute rootAttribute = (IRootAttribute)graph.getRoot().getAttribute();
		ILinkAttributeFactory attFactory = rootAttribute.getModel().linkAttributeFactory();
		edgeFact.setAttributeFactory(attFactory);
		SrcVisitor srcVisitor = new SrcVisitor();
		this.srcShape.visit(srcVisitor);
		TgtVisitor tgtVisitor = new TgtVisitor();
		this.tgtShape.visit(tgtVisitor);
		edgeFact.setPair(new CompoundNodePair(srcVisitor.getNode(), tgtVisitor.getNode()));
//		ILinkEdgeFactory fact = new LinkEdgeFactoryFacade(graph.edgeFactory());
//		fact.setShapeNodePair(srcShape, tgtShape);
//		fact.setObjectType(linkObjectType);
		attFactory.setObjectType(linkObjectType);
//		ILinkEdge link = fact.createLinkEdge();
		ICompoundEdge edge = edgeFact.createEdge();
//		ILinkAttribute linkAttribute = link.getAttribute();
		ILinkAttribute linkAttribute = (ILinkAttribute) edge.getAttribute();
		linkAttribute.getSourceTerminus().setLocation(linkPointDefinition.getSrcAnchorPosition());
		linkAttribute.getTargetTerminus().setLocation(linkPointDefinition.getTgtAnchorPosition());
//		Envelope srcBounds = ((IDrawingNodeAttribute)srcVisitor.getNode().getAttribute()).getBounds();
//		((IDrawingNodeAttribute)srcVisitor.getNode().getAttribute()).setBounds(srcBounds.changeOrigin(linkPointDefinition.getSrcAnchorPosition()));
//		Envelope tgtBounds = ((IDrawingNodeAttribute)tgtVisitor.getNode().getAttribute()).getBounds();
//		((IDrawingNodeAttribute)tgtVisitor.getNode().getAttribute()).setBounds(tgtBounds.changeOrigin(linkPointDefinition.getTgtAnchorPosition()));
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
		this.srcShape.getGraph().restoreState(this.originalState);
	}

	@Override
	public void redo() {
		this.srcShape.getGraph().restoreState(createdState);
	}

	private class SrcVisitor implements ICompoundGraphElementVisitor{
		ICompoundNode newNode;
		
		@Override
		public void visitEdge(ICompoundEdge edge) {
			ICompoundNodeFactory fact = edge.getChildCompoundGraph().nodeFactory();
			ILinkObjectType linkOt = ((ILinkAttribute)edge.getAttribute()).getObjectType();
			IShapeObjectType linkEndObjectType = ((ILinkAttribute)edge.getAttribute()).getModel().getNotationSubsystem().getSyntaxService().getLinkEndObjectType(linkOt);
			IShapeAttributeFactory attFact = ((ILinkAttribute)edge.getAttribute()).getModel().shapeAttributeFactory();
			attFact.setObjectType(linkEndObjectType);
			fact.setAttributeFactory(attFact); 
			newNode = fact.createNode();
			IShapeAttribute newAtt = (IShapeAttribute)newNode.getAttribute();
			Envelope origEnv = newAtt.getBounds();
			newAtt.setBounds(origEnv.changeOrigin(linkPointDefinition.getSrcAnchorPosition()));
		}

		public ICompoundNode getNode() {
			return this.newNode;
		}

		@Override
		public void visitNode(ICompoundNode node) {
			this.newNode = node;
		}
	}

	private class TgtVisitor implements ICompoundGraphElementVisitor{
		ICompoundNode newNode;
		
		@Override
		public void visitEdge(ICompoundEdge edge) {
			ICompoundNodeFactory fact = edge.getChildCompoundGraph().nodeFactory();
			ILinkObjectType linkOt = ((ILinkAttribute)edge.getAttribute()).getObjectType();
			IShapeObjectType linkEndObjectType = ((ILinkAttribute)edge.getAttribute()).getModel().getNotationSubsystem().getSyntaxService().getLinkEndObjectType(linkOt);
			IShapeAttributeFactory attFact = ((ILinkAttribute)edge.getAttribute()).getModel().shapeAttributeFactory();
			attFact.setObjectType(linkEndObjectType);
			fact.setAttributeFactory(attFact); 
			newNode = fact.createNode();
			IShapeAttribute newAtt = (IShapeAttribute)newNode.getAttribute();
			Envelope origEnv = newAtt.getBounds();
			newAtt.setBounds(origEnv.changeOrigin(linkPointDefinition.getTgtAnchorPosition()));
		}

		public ICompoundNode getNode() {
			return this.newNode;
		}

		@Override
		public void visitNode(ICompoundNode node) {
			this.newNode = node;
		}
	}

}
