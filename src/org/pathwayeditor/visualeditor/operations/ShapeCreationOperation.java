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
package org.pathwayeditor.visualeditor.operations;

import java.util.SortedSet;

import org.apache.log4j.Logger;
import org.pathwayeditor.businessobjects.drawingprimitives.ICanvasElementAttribute;
import org.pathwayeditor.businessobjects.drawingprimitives.attributes.Colour;
import org.pathwayeditor.businessobjects.drawingprimitives.attributes.LineStyle;
import org.pathwayeditor.businessobjects.typedefn.IShapeObjectType;
import org.pathwayeditor.figure.geometry.Dimension;
import org.pathwayeditor.figure.geometry.Envelope;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.figure.geometry.Scale;
import org.pathwayeditor.visualeditor.behaviour.operation.IShapeCreationOperation;
import org.pathwayeditor.visualeditor.commands.ICommand;
import org.pathwayeditor.visualeditor.commands.ICommandStack;
import org.pathwayeditor.visualeditor.commands.ShapeCreationCommand;
import org.pathwayeditor.visualeditor.controller.IDrawingElementController;
import org.pathwayeditor.visualeditor.controller.IViewControllerModel;
import org.pathwayeditor.visualeditor.editingview.IShapePane;
import org.pathwayeditor.visualeditor.feedback.IFeedbackModel;
import org.pathwayeditor.visualeditor.feedback.IFeedbackNode;
import org.pathwayeditor.visualeditor.geometry.IIntersectionCalcnFilter;
import org.pathwayeditor.visualeditor.layout.ILabelPositionCalculator;

public class ShapeCreationOperation implements IShapeCreationOperation<IShapeObjectType> {
	private static final Point NO_SIZE_DELTA = new Point(0.0, 0.0);
	private final Logger logger = Logger.getLogger(this.getClass());
	private final IShapePane shapePane;
//	private final IModel domainModel;
	private final IFeedbackModel feedbackModel;
	private final ICommandStack commandStack;
	private IShapeObjectType shapeObjectType;
	private Point originDelta;
	private Dimension sizeDelta;
//	private Point startLocation;
	private boolean canCreationSucceed = false;
	private final IViewControllerModel viewModel;
	private ILabelPositionCalculator labelPositionCalculator;

	public ShapeCreationOperation(IShapePane shapePane, IFeedbackModel feedbackModel,
			IViewControllerModel viewModel, ICommandStack commandStack, ILabelPositionCalculator labelPositionCalculator) {
		this.shapePane = shapePane;
		this.viewModel = viewModel;
		this.feedbackModel = feedbackModel;
		this.commandStack = commandStack;
		this.labelPositionCalculator = labelPositionCalculator;
	}

	@Override
	public IShapeObjectType getShapeObjectType() {
		return this.shapeObjectType;
	}

	@Override
	public void setShapeObjectType(IShapeObjectType shapeType) {
		this.shapeObjectType = shapeType;
	}

	private void calculateDefaultBounds(){
		Dimension delta = this.shapeObjectType.getDefaultAttributes().getSize().scale(new Scale(0.5, 0.5)).negate();
		double originX = delta.getWidth();
		double originY = delta.getHeight();
		this.originDelta = new Point(originX, originY);
		this.sizeDelta = this.shapeObjectType.getDefaultAttributes().getSize();
	}
	
	@Override
	public void finishCreationDrag(Point delta) {
		if(delta.equals(NO_SIZE_DELTA)){
			calculateDefaultBounds();
		}
		else{
			calculateBounds(delta);
		}
		IFeedbackNode node = this.feedbackModel.uniqueFeedbackNode();
		node.resizePrimitive(originDelta, sizeDelta);
		IDrawingElementController potentialParent = getParentElement(node);
		ICanvasElementAttribute drawingElementAtt = potentialParent.getAssociatedAttribute();
		if(drawingElementAtt.getObjectType().getParentingRules().isValidChild(getShapeObjectType())){
			ICommand cmd = new ShapeCreationCommand(potentialParent.getAssociatedAttribute().getCurrentElement(), this.shapeObjectType,
					node.getFigureController(), labelPositionCalculator);
			this.commandStack.execute(cmd);
			if(logger.isDebugEnabled()){
				logger.debug("Create a new shape at: " + cmd);
			}
		}
		
//		this.shapePane.updateView();
		this.feedbackModel.clear();
		this.shapePane.updateView();
	}
	
	
//	private void createShapeLabels(IShapeController shapeHull){
//		currentCmd = new CompoundCommand();
//		INotationSyntaxService syntaxService = this.domainModel.getNotationSubsystem().getSyntaxService();
//		Iterator<IAnnotationProperty> defnIter = shapeHull.getAssociatedAttribute().propertyIterator();
//		while(defnIter.hasNext()){
//			IAnnotationProperty defn = defnIter.next();
//			if(syntaxService.isVisualisableProperty(defn.getDefinition())){
//				ILabelObjectType labelObjectType = syntaxService.getLabelObjectTypeByProperty(defn.getDefinition());
//				if(labelObjectType.isAlwaysDisplayed()){
//					String defaultText = getDisplayedLabelText(labelObjectType, defn);
//					Envelope labelBounds = this.labelPositionCalculator.calculateLabelPosition(shapeHull, labelObjectType, defaultText);
//					currentCmd.addCommand(new LabelCreationCommand(shapeHull.getDrawingElement(), defn, labelBounds));
//					if(logger.isTraceEnabled()){
//						logger.trace("Create label at: " + labelBounds);
//					}
//				}
//			}
//		}
////		this.commandStack.execute(currentCmd);
//	}
//	
//	private String getDisplayedLabelText(ILabelObjectType labelObjectType, IAnnotationProperty defn) {
//		Format displayFormat = labelObjectType.getDefaultAttributes().getDisplayFormat();
//		String retVal = null;
//		if(displayFormat != null){
//			retVal = displayFormat.format(defn.getValue());
//		}
//		else{
//			retVal = defn.getValue().toString();
//		}
//		return retVal;
//	}


	@Override
	public void ongoingCreationDrag(Point delta) {
		calculateBounds(delta);
		IFeedbackNode node = this.feedbackModel.uniqueFeedbackNode();
		node.resizePrimitive(originDelta, sizeDelta);
		this.shapePane.updateView();
		IDrawingElementController potentialParent = getParentElement(node);
		ICanvasElementAttribute drawingElementAtt = potentialParent.getAssociatedAttribute();
		this.canCreationSucceed = drawingElementAtt.getObjectType().getParentingRules().isValidChild(getShapeObjectType());
		if(logger.isTraceEnabled()){
			logger.trace("PotParent=" + potentialParent + "Ongoing drag. newLocation=" + originDelta + ", sizeDelta=" + sizeDelta + ",creationSucceed=" + canCreationSucceed);
		}
	}
	
	private IDrawingElementController getParentElement(IFeedbackNode node){
		viewModel.getIntersectionCalculator().setFilter(new IIntersectionCalcnFilter() {
			@Override
			public boolean accept(IDrawingElementController node) {
				return true;
			}
		});
		SortedSet<IDrawingElementController> intersections = viewModel.getIntersectionCalculator().findIntersectingElements(node.getConvexHull());
		viewModel.getIntersectionCalculator().setFilter(null);
		IDrawingElementController potentialParent = intersections.first();
		if(logger.isTraceEnabled()){
			int i = 1;
			for(IDrawingElementController intn : intersections){
				logger.trace("Parent Hit=" + intn + "hit#=" + i++);
			}
		}
		return potentialParent;
	}

	@Override
	public void startCreationDrag(Point origin) {
		this.canCreationSucceed = false;
//		this.startLocation = origin;
		calculateBounds(origin);
		feedbackModel.clear();
		if(logger.isTraceEnabled()){
			logger.trace("Starting create shape drag. origin=" + origin);
		}
		IFeedbackNode node = feedbackModel.getFeedbackNodeBuilder().createFromDrawingNodeObjectType(this.shapeObjectType, new Envelope(origin, new Dimension(0.1, 0.1)));
//		node.setFillColour(Colour.BLUE);
		node.setLineColour(Colour.RED);
		node.setLineStyle(LineStyle.SOLID);
		node.setLineWidth(1.0);
		this.shapePane.updateView();
	}

	private void calculateBounds(Point delta){
		double originX = 0.0;
		double originY = 0.0;
		double sizeX = delta.getX();
		double sizeY = delta.getY();
		if(delta.getX() < 0.0){
			originX = delta.getX();
			sizeX = -delta.getX();
		}
		if(delta.getY() < 0.0){
			originY = delta.getY();
			sizeY = -delta.getY();
		}
		this.originDelta = new Point(originX, originY);
		this.sizeDelta = new Dimension(sizeX, sizeY);
	}

	@Override
	public boolean canCreationSucceed() {
		return this.canCreationSucceed ;
	}
	
//	protected final Point calculateLocationDelta(Point newLocation){
//		return this.startLocation.difference(newLocation);
//	}
	
}
