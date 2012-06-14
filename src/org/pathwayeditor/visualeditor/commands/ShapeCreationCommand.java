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

import java.text.Format;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.pathwayeditor.businessobjects.drawingprimitives.ICanvasElementAttribute;
import org.pathwayeditor.businessobjects.drawingprimitives.ILabelAttribute;
import org.pathwayeditor.businessobjects.drawingprimitives.ILabelAttributeFactory;
import org.pathwayeditor.businessobjects.drawingprimitives.IShapeAttribute;
import org.pathwayeditor.businessobjects.drawingprimitives.IShapeAttributeFactory;
import org.pathwayeditor.businessobjects.drawingprimitives.properties.IAnnotationProperty;
import org.pathwayeditor.businessobjects.notationsubsystem.INotationSyntaxService;
import org.pathwayeditor.businessobjects.typedefn.ILabelObjectType;
import org.pathwayeditor.businessobjects.typedefn.IShapeObjectType;
import org.pathwayeditor.figure.geometry.Envelope;
import org.pathwayeditor.figure.rendering.IFigureRenderingController;
import org.pathwayeditor.visualeditor.layout.ILabelPositionCalculator;

import uk.ac.ed.inf.graph.compound.ICompoundGraphElement;
import uk.ac.ed.inf.graph.compound.ICompoundNode;
import uk.ac.ed.inf.graph.compound.ICompoundNodeFactory;
import uk.ac.ed.inf.graph.state.IGraphState;

public class ShapeCreationCommand implements ICommand {
	private final Logger logger = Logger.getLogger(this.getClass());
	private final ICompoundGraphElement parentNode;
	private final IShapeObjectType objectType;
	private final IFigureRenderingController figController;
	private IGraphState createdState;
	private IGraphState originalState;
	private ILabelPositionCalculator labelPositionCalculator;
	
	public ShapeCreationCommand(ICompoundGraphElement rootNode, IShapeObjectType shapeObjectType, IFigureRenderingController iFigureRenderingController,
			ILabelPositionCalculator labelPosnCalc) {
		this.parentNode = rootNode;
		this.objectType = shapeObjectType;
		this.figController = iFigureRenderingController;
		this.labelPositionCalculator = labelPosnCalc;
	}

	@Override
	public void execute() {
		this.originalState = this.parentNode.getGraph().getCurrentState();
		ICompoundNodeFactory nodeFact = parentNode.getChildCompoundGraph().nodeFactory();
		IShapeAttributeFactory fact = ((ICanvasElementAttribute)parentNode.getAttribute()).getModel().shapeAttributeFactory();
		fact.setObjectType(objectType);
		nodeFact.setAttributeFactory(fact);
		ICompoundNode node = nodeFact.createNode();
		((IShapeAttribute)node.getAttribute()).setBounds(figController.getEnvelope());
		createShapeLabels((IShapeAttribute)node.getAttribute());
		if(logger.isDebugEnabled()){
			logger.debug("Creating shapeNode=" + node + ", bounds=" + ((IShapeAttribute)node.getAttribute()).getBounds());
		}
		this.createdState = node.getGraph().getCurrentState();
	}

	private void createShapeLabels(IShapeAttribute shapeHull){
		INotationSyntaxService syntaxService = shapeHull.getModel().getNotationSubsystem().getSyntaxService();
		ILabelAttributeFactory fact = shapeHull.getModel().labelAttributeFactory();
		Iterator<IAnnotationProperty> defnIter = shapeHull.propertyIterator();
		while(defnIter.hasNext()){
			IAnnotationProperty defn = defnIter.next();
			if(syntaxService.isVisualisableProperty(defn.getDefinition())){
				ILabelObjectType labelObjectType = syntaxService.getLabelObjectTypeByProperty(defn.getDefinition());
				if(labelObjectType.isAlwaysDisplayed()){
					String defaultText = getDisplayedLabelText(labelObjectType, defn);
					ICompoundNodeFactory labelFact = shapeHull.getCurrentElement().getChildCompoundGraph().nodeFactory();
					// display props that are always displayed
					fact.setProperty(defn);
					fact.setLabelObjectType(labelObjectType);
					labelFact.setAttributeFactory(fact);
					ICompoundNode labelNode = labelFact.createNode();
					if(logger.isDebugEnabled()){
						logger.debug("Create labelNode=" + labelNode + ", bounds=" + ((ILabelAttribute)labelNode.getAttribute()).getBounds());
					}
					Envelope labelBounds = this.labelPositionCalculator.calculateLabelPosition(this.figController, labelObjectType, defaultText);
					((ILabelAttribute)labelNode.getAttribute()).setBounds(labelBounds);
				}
			}
		}
//		this.commandStack.execute(currentCmd);
	}
	
	private String getDisplayedLabelText(ILabelObjectType labelObjectType, IAnnotationProperty defn) {
		Format displayFormat = labelObjectType.getDefaultAttributes().getDisplayFormat();
		String retVal = null;
		if(displayFormat != null){
			retVal = displayFormat.format(defn.getValue());
		}
		else{
			retVal = defn.getValue().toString();
		}
		return retVal;
	}

	@Override
	public void redo() {
		this.parentNode.getGraph().restoreState(createdState);
	}

	@Override
	public void undo() {
		this.parentNode.getGraph().restoreState(this.originalState);
	}

	@Override
	public String toString(){
		StringBuilder buf = new StringBuilder(this.getClass().getSimpleName());
		buf.append("(");
		buf.append("parentNodeIdx=");
		buf.append(parentNode.getIndex());
		buf.append(",bounds=");
		buf.append(this.figController.getEnvelope());
		buf.append(",objectType=");
		buf.append(objectType.getName());
		return buf.toString();
	}
}
