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
import org.pathwayeditor.businessobjects.drawingprimitives.ICurveSegment;
import org.pathwayeditor.businessobjects.drawingprimitives.IDrawingElement;
import org.pathwayeditor.businessobjects.typedefn.IAnchorNodeObjectType;
import org.pathwayeditor.figure.rendering.IFigureRenderingController;

import uk.ac.ed.inf.graph.compound.ICompoundNode;
import uk.ac.ed.inf.graph.compound.ICompoundNodeFactory;
import uk.ac.ed.inf.graph.state.IGraphState;

public class AnchorNodeCreationCommand implements ICommand {
	private final Logger logger = Logger.getLogger(this.getClass());
	private final IDrawingElement parentNode;
	private final IAnchorNodeObjectType objectType;
	private final IFigureRenderingController figController;
	private IGraphState createdState;
	private IGraphState originalState;
	private final ICurveSegment parentSegment;
	
	public AnchorNodeCreationCommand(IDrawingElement rootNode, IAnchorNodeObjectType shapeObjectType, IFigureRenderingController iFigureRenderingController,
			ICurveSegment parentSegment) {
		this.parentNode = rootNode;
		this.objectType = shapeObjectType;
		this.figController = iFigureRenderingController;
		this.parentSegment = parentSegment;
	}

	@Override
	public void execute() {
		this.originalState = this.parentNode.getGraphElement().getGraph().getCurrentState();
		ICompoundNodeFactory fact = parentNode.getGraphElement().getChildCompoundGraph().nodeFactory();
		IAnchorNodeAttributeFactory attFact = parentNode.getAttribute().getModel().anchorNodeAttributeFactory();
		fact.setAttributeFactory(attFact);
		attFact.setObjectType(objectType);
		attFact.setDestinationAttribute(parentNode.getAttribute());
		attFact.setAssociateCurveSegment(parentSegment);
		ICompoundNode node = fact.createNode();
		IAnchorNodeAttribute nodeAtt = (IAnchorNodeAttribute)node.getAttribute(); 
		nodeAtt.setBounds(figController.getEnvelope());
		if(logger.isDebugEnabled()){
			logger.debug("Creating shapeNode=" + node + ", bounds=" + nodeAtt.getBounds());
		}
		this.createdState = node.getGraph().getCurrentState();
	}

	@Override
	public void redo() {
		this.parentNode.getGraphElement().getGraph().restoreState(createdState);
	}

	@Override
	public void undo() {
		this.parentNode.getGraphElement().getGraph().restoreState(this.originalState);
	}

	@Override
	public String toString(){
		StringBuilder buf = new StringBuilder(this.getClass().getSimpleName());
		buf.append("(");
		buf.append("parentNodeIdx=");
		buf.append(parentNode.getGraphElement().getIndex());
		buf.append(",bounds=");
		buf.append(this.figController.getEnvelope());
		buf.append(",objectType=");
		buf.append(objectType.getName());
		return buf.toString();
	}
}
