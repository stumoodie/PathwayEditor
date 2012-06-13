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
import org.pathwayeditor.businessobjects.drawingprimitives.IAnchorNodeAttribute;
import org.pathwayeditor.businessobjects.drawingprimitives.IAnchorNodeAttributeFactory;
import org.pathwayeditor.businessobjects.drawingprimitives.ICanvasElementAttribute;
import org.pathwayeditor.businessobjects.drawingprimitives.ICurveSegment;
import org.pathwayeditor.businessobjects.typedefn.IAnchorNodeObjectType;
import org.pathwayeditor.figure.geometry.Dimension;
import org.pathwayeditor.figure.geometry.Point;

import uk.ac.ed.inf.graph.compound.ICompoundNode;
import uk.ac.ed.inf.graph.compound.ICompoundNodeFactory;
import uk.ac.ed.inf.graph.state.IGraphState;

public class AnchorNodeCreationCommand implements ICommand {
	private final Logger logger = Logger.getLogger(this.getClass());
	private final ICanvasElementAttribute parentNode;
	private final IAnchorNodeObjectType objectType;
	private final Point anchorPosn;
	private final Dimension size;
	private IGraphState createdState;
	private IGraphState originalState;
	private final ICurveSegment parentSegment;
	
	public AnchorNodeCreationCommand(ICanvasElementAttribute rootNode, IAnchorNodeObjectType shapeObjectType,
			Point anchorPoint, Dimension size, ICurveSegment parentSegment) {
		this.parentNode = rootNode;
		this.objectType = shapeObjectType;
		this.parentSegment = parentSegment;
		this.anchorPosn = anchorPoint;
		this.size = size;
	}

	@Override
	public void execute() {
		this.originalState = this.parentNode.getCurrentElement().getGraph().getCurrentState();
		ICompoundNodeFactory fact = parentNode.getCurrentElement().getChildCompoundGraph().nodeFactory();
		IAnchorNodeAttributeFactory attFact = parentNode.getModel().anchorNodeAttributeFactory();
		fact.setAttributeFactory(attFact);
		attFact.setObjectType(objectType);
		attFact.setDestinationAttribute(parentNode);
		attFact.setAssociateCurveSegment(parentSegment);
		ICompoundNode node = fact.createNode();
		IAnchorNodeAttribute nodeAtt = (IAnchorNodeAttribute)node.getAttribute(); 
		nodeAtt.setAnchorLocation(this.anchorPosn);
		nodeAtt.setBounds(nodeAtt.getBounds().changeDimension(this.size));
		if(logger.isDebugEnabled()){
			logger.debug("Creating anchorNode=" + node + ", requested locn= " + this.anchorPosn + ", location=" + nodeAtt.getAnchorLocation() + ",size=" + this.size);
		}
		this.createdState = node.getGraph().getCurrentState();
	}

	@Override
	public void redo() {
		this.parentNode.getCurrentElement().getGraph().restoreState(createdState);
	}

	@Override
	public void undo() {
		this.parentNode.getCurrentElement().getGraph().restoreState(this.originalState);
	}

	@Override
	public String toString(){
		StringBuilder buf = new StringBuilder(this.getClass().getSimpleName());
		buf.append("(");
		buf.append("parentNodeIdx=");
		buf.append(parentNode.getCreationSerial());
		buf.append(",curveSegment=");
		buf.append(this.parentSegment);
		buf.append(",requestedLocn=");
		buf.append(this.anchorPosn);
		buf.append(",objectType=");
		buf.append(objectType.getName());
		return buf.toString();
	}
}
