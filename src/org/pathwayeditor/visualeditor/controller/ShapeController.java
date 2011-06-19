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
package org.pathwayeditor.visualeditor.controller;

import java.util.Iterator;
import java.util.SortedSet;

import org.apache.log4j.Logger;
import org.pathwayeditor.businessobjects.drawingprimitives.IDrawingElement;
import org.pathwayeditor.businessobjects.drawingprimitives.IShapeAttribute;
import org.pathwayeditor.businessobjects.drawingprimitives.IShapeNode;
import org.pathwayeditor.businessobjects.drawingprimitives.attributes.Colour;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.CanvasAttributePropertyChange;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.IAnnotationPropertyChangeEvent;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.IAnnotationPropertyChangeListener;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.ICanvasAttributeChangeListener;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.ICanvasAttributePropertyChangeEvent;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.ICanvasAttributeResizedEvent;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.ICanvasAttributeTranslationEvent;
import org.pathwayeditor.businessobjects.drawingprimitives.properties.IAnnotationProperty;
import org.pathwayeditor.businessobjects.impl.facades.DrawingElementFacade;
import org.pathwayeditor.businessobjects.impl.facades.DrawingNodeFacade;
import org.pathwayeditor.businessobjects.impl.facades.SubModelFacade;
import org.pathwayeditor.figure.geometry.Dimension;
import org.pathwayeditor.figure.geometry.Envelope;
import org.pathwayeditor.figure.geometry.IConvexHull;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.figure.geometry.RectangleHull;
import org.pathwayeditor.figure.rendering.IFigureRenderingController;
import org.pathwayeditor.visualeditor.editingview.FigureDefinitionMiniCanvas;
import org.pathwayeditor.visualeditor.editingview.IMiniCanvas;
import org.pathwayeditor.visualeditor.geometry.IIntersectionCalcnFilter;
import org.pathwayeditor.visualeditor.geometry.IIntersectionCalculator;

import uk.ac.ed.inf.graph.compound.ICompoundNode;

public class ShapeController extends NodeController implements IShapeController {
	private final Logger logger = Logger.getLogger(this.getClass());
	private final IShapeNode domainNode;
	private final IDrawingElement parentAttribute;
	private final ICanvasAttributeChangeListener shapePropertyChangeListener;
	private final IAnnotationPropertyChangeListener annotPropChangeListener;
	private final IFigureControllerHelper figureController;
	private final ICanvasAttributeChangeListener parentDrawingNodePropertyChangeListener;
	private boolean isActive;
	
	public ShapeController(IViewControllerModel viewModel, IShapeNode node, int index) {
		super(viewModel, index);
		
		this.domainNode = node;
		this.parentAttribute = new DrawingElementFacade(this.domainNode.getGraphElement().getParent());
		this.figureController = new ShapeFigureControllerHelper(domainNode.getAttribute());
		figureController.createFigureController();
		shapePropertyChangeListener = new ICanvasAttributeChangeListener() {
			@Override
			public void propertyChange(ICanvasAttributePropertyChangeEvent e) {
				if(e.getPropertyChange().equals(CanvasAttributePropertyChange.LINE_COLOUR)){
					figureController.getFigureController().setLineColour((Colour)e.getNewValue());
					figureController.refreshGraphicalAttributes();
				}
				else if(e.getPropertyChange().equals(CanvasAttributePropertyChange.FILL_COLOUR)){
					figureController.getFigureController().setFillColour((Colour)e.getNewValue());
					figureController.refreshGraphicalAttributes();
				}
				else if(e.getPropertyChange().equals(CanvasAttributePropertyChange.LINE_WIDTH)){
					Double newLineWidth = (Double)e.getNewValue();
					figureController.getFigureController().setLineWidth(newLineWidth);
					figureController.refreshGraphicalAttributes();
				}
				else if(e.getPropertyChange().equals(CanvasAttributePropertyChange.BOUNDS)){
					IShapeAttribute attribute = (IShapeAttribute)e.getAttribute();
					Envelope oldDrawnBounds = figureController.getFigureController().getConvexHull().getEnvelope();
					figureController.getFigureController().setEnvelope(attribute.getBounds());
					figureController.refreshGraphicalAttributes();
//					recalculateSrcLinks();
//					recalculateTgtLinks();
					notifyDrawnBoundsChanged(oldDrawnBounds, figureController.getFigureController().getConvexHull().getEnvelope());
				}
				else if(e.getPropertyChange().equals(CanvasAttributePropertyChange.LINE_STYLE)){
					IShapeAttribute attribute = (IShapeAttribute)e.getAttribute();
					figureController.getFigureController().setLineStyle(attribute.getLineStyle());
					figureController.refreshGraphicalAttributes();
				}
			}

			@Override
			public void elementTranslated(ICanvasAttributeTranslationEvent e) {
			}

			@Override
			public void nodeResized(ICanvasAttributeResizedEvent e) {
			}
		};
		annotPropChangeListener = new IAnnotationPropertyChangeListener() {
			@Override
			public void propertyChange(IAnnotationPropertyChangeEvent e) {
				figureController.refreshBoundProperties();
//				assignBindVariablesToProperties(domainNode.getAttribute(), figureController);
//				figureController.generateFigureDefinition();
			}	
		};
		parentDrawingNodePropertyChangeListener = new ICanvasAttributeChangeListener() {
			@Override
			public void propertyChange(ICanvasAttributePropertyChangeEvent e) {
			}
			@Override
			public void elementTranslated(ICanvasAttributeTranslationEvent e) {
				domainNode.getAttribute().translate(e.getTranslationDelta());
			}
			@Override
			public void nodeResized(ICanvasAttributeResizedEvent e) {
			}
		};
	}

	@Override
	public void activate(){
		addListeners(this.domainNode);
		this.isActive = true;
	}
	
	
	@Override
	public IMiniCanvas getMiniCanvas(){
		IFigureRenderingController renderingController = this.figureController.getFigureController();
		return new FigureDefinitionMiniCanvas(renderingController.getFigureDefinition(), renderingController.getEnvelope());
	}

//	public void changeSourceAnchor(ILinkController linkController, IShapeController srcNode, IShapeController tgtNode){
//		if(linkController.getLinkDefinition().numBendPoints() > 0){
//			// there are bend-points so we use the bp as the reference position
//			Point refPoint = linkController.getLinkDefinition().getSourceLineSegment().getTerminus();
//			this.calculateSourceAnchor(linkController, srcNode, refPoint);
//		}
//		else{
//			// otherwise we just use the centre positions of the shapes
//			// as they will be after the move is completed
//			this.calculateSourceAnchor(linkController, srcNode, tgtNode.getConvexHull().getCentre());
//			this.calculateTargetAnchor(linkController, tgtNode, srcNode.getConvexHull().getCentre());
//		}
//	}
//
//	public void changeTargetAnchor(ILinkController linkController, IShapeController srcNode, IShapeController tgtNode){
//		if(linkController.getLinkDefinition().numBendPoints() > 0){
//			// there are bend-points so we use the bp as the reference position
//			Point refPoint = linkController.getLinkDefinition().getTargetLineSegment().getTerminus();
//			this.calculateTargetAnchor(linkController, tgtNode, refPoint);
//		}
//		else{
//			// otherwise we just use the centre positions of the shapes
//			// as they will be after the move is completed
//			this.calculateTargetAnchor(linkController, tgtNode, srcNode.getConvexHull().getCentre());
//			this.calculateSourceAnchor(linkController, srcNode, tgtNode.getConvexHull().getCentre());
//		}
//	}
	
//	private void calculateSourceAnchor(ILinkController linkController, IShapeController srcNode, Point refPosn){
//		Point newAnchorLocn = getRevisedAnchorPosition(srcNode, refPosn);
//		linkController.getDrawingElement().getAttribute().getSourceTerminus().setLocation(newAnchorLocn);
//		if(logger.isTraceEnabled()){
//			logger.trace("Recalculating src anchor. Reference bp = " + refPosn + ",anchor=" + newAnchorLocn);
//		}
//	}
//	
//	private void calculateTargetAnchor(ILinkController linkController, IShapeController tgtNode, Point refPosn){
//		Point newAnchorLocn = getRevisedAnchorPosition(tgtNode, refPosn);
//		linkController.getDrawingElement().getAttribute().getTargetTerminus().setLocation(newAnchorLocn);
//		if(logger.isTraceEnabled()){
//			logger.trace("Recalculating tgt anchor. Reference bp = " + refPosn + ",anchor=" + newAnchorLocn);
//		}
//	}
//	
//	private Point getRevisedAnchorPosition(IShapeController nodeController, Point refPoint){
//		IFigureRenderingController controller = nodeController.getFigureController();
//		IAnchorLocator calc = controller.getAnchorLocatorFactory().createAnchorLocator();
//		calc.setOtherEndPoint(refPoint);
//		return calc.calcAnchorPosition();
//	}
	
	
	private void addListeners(IShapeNode attribute) {
		attribute.getAttribute().addChangeListener(shapePropertyChangeListener);
		Iterator<IAnnotationProperty> iter = attribute.getAttribute().propertyIterator();
		while(iter.hasNext()){
			IAnnotationProperty prop = iter.next();
			prop.addChangeListener(annotPropChangeListener);
		}
		this.parentAttribute.getAttribute().addChangeListener(parentDrawingNodePropertyChangeListener);
	}
	
	private void removeListeners() {
		final IShapeAttribute attribute = this.domainNode.getAttribute();
		attribute.removeChangeListener(shapePropertyChangeListener);
		Iterator<IAnnotationProperty> iter = attribute.propertyIterator();
		while(iter.hasNext()){
			IAnnotationProperty prop = iter.next();
			prop.removeChangeListener(annotPropChangeListener);
		}
		parentAttribute.getAttribute().removeChangeListener(parentDrawingNodePropertyChangeListener);
	}
		
	@Override
	public IShapeNode getDrawingElement() {
		return this.domainNode;
	}

	@Override
	public IFigureRenderingController getFigureController() {
		return this.figureController.getFigureController();
	}

	@Override
	public Envelope getBounds() {
		return this.figureController.getFigureController().getEnvelope();
	}

	@Override
	public IConvexHull getConvexHull() {
		return this.figureController.getFigureController().getConvexHull();
	}

	@Override
	public boolean canResize(Point originDelta, Dimension resizeDelta) {
		boolean retVal = false;
		// algorithm is to find the intersecting shapes and then check if the
		// children and parents are in the intersection list
		Envelope newBounds = this.domainNode.getAttribute().getBounds().resize(originDelta, resizeDelta);
		if(logger.isTraceEnabled()){
			logger.trace("In can resize. New bounds = " + newBounds + ",originDelta=" + originDelta + ",resizeDelta=" + resizeDelta);
		}
		if(newBounds.getDimension().getWidth() > 0.0 && newBounds.getDimension().getHeight() > 0.0){
			INodeController parentNode = this.getViewModel().getNodeController(new DrawingNodeFacade((ICompoundNode)this.domainNode.getGraphElement().getParent()));
			IIntersectionCalculator intCal = this.getViewModel().getIntersectionCalculator();
			intCal.setFilter(new IIntersectionCalcnFilter() {
				@Override
				public boolean accept(IDrawingElementController node) {
					return !(node instanceof ILabelController);
				}
			});
			SortedSet<IDrawingElementController> intersectingNodes = intCal.findIntersectingNodes(this.getConvexHull().changeEnvelope(newBounds), this);
			boolean parentIntersects = intersectingNodes.contains(parentNode);
			if(logger.isTraceEnabled()){
				logger.trace("CanResize: intersects with parent" + parentIntersects);
			}
			boolean childrenIntersect = childrenIntersect(this, intersectingNodes);
			if(logger.isTraceEnabled()){
				logger.trace("CanResize: intersects with children" + childrenIntersect);
			}
			retVal = parentIntersects && childrenIntersect;
		}
		return retVal;
	}

	private boolean childrenIntersect(IShapeController parentNode, SortedSet<IDrawingElementController> intersectingNodes){
		Iterator<ICompoundNode> iter = new SubModelFacade(parentNode.getDrawingElement().getGraphElement().getChildCompoundGraph()).shapeNodeIterator();
		boolean retVal = true;
		while(iter.hasNext() && retVal){
			INodeController child = this.getViewModel().getNodeController(new DrawingNodeFacade(iter.next()));
			retVal = intersectingNodes.contains(child);
		}
		return retVal;
	}
	
	@Override
	public void inactivate() {
		removeListeners();
		this.isActive = false;
	}

	@Override
	public boolean isActive() {
		return this.isActive;
	}

	@Override
	public Envelope getDrawnBounds() {
		return this.figureController.getFigureController().getEnvelope();
	}

	@Override
	public boolean containsPoint(Point p) {
		IConvexHull attributeHull = this.getConvexHull();
		boolean retVal = attributeHull.containsPoint(p); 
		if(logger.isTraceEnabled()){
			logger.trace("Testing contains node:" + this + ",retVal=" + retVal + ", hull=" + attributeHull + ", point=" + p);
		}
		return retVal;
	}

	@Override
	public boolean intersectsHull(IConvexHull queryHull) {
		return this.figureController.getFigureController().getConvexHull().hullsIntersect(queryHull);
	}

	@Override
	public boolean intersectsBounds(Envelope otherBounds) {
		IConvexHull otherHull = new RectangleHull(otherBounds);
		return intersectsHull(otherHull);
	}
}
