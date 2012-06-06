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

import org.apache.log4j.Logger;
import org.pathwayeditor.businessobjects.drawingprimitives.ICanvasElementAttribute;
import org.pathwayeditor.businessobjects.drawingprimitives.ILinkEdge;
import org.pathwayeditor.businessobjects.drawingprimitives.IShapeAttribute;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.BendPointStructureChange;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.CanvasAttributePropertyChange;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.IBendPointContainerListener;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.IBendPointLocationChangeEvent;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.IBendPointStructureChangeEvent;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.ICanvasAttributeChangeListener;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.ICanvasAttributePropertyChangeEvent;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.ICanvasAttributeResizedEvent;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.ICanvasAttributeTranslationEvent;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.ILinkTerminusChangeListener;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.ILinkTerminusValueChangeEvent;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.LinkTerminusChangeType;
import org.pathwayeditor.businessobjects.impl.facades.ShapeNodeFacade;
import org.pathwayeditor.figure.geometry.Envelope;
import org.pathwayeditor.figure.geometry.IConvexHull;
import org.pathwayeditor.figure.geometry.LineSegment;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.visualeditor.editingview.IMiniCanvas;
import org.pathwayeditor.visualeditor.feedback.DomainLinkMiniCanvas;
import org.pathwayeditor.visualeditor.geometry.ILinkDefinitionAnchorCalculator;
import org.pathwayeditor.visualeditor.geometry.ILinkPointDefinition;
import org.pathwayeditor.visualeditor.geometry.LinkDefinitionAnchorCalculator;
import org.pathwayeditor.visualeditor.geometry.LinkPointDefinition;

public class LinkController extends DrawingElementController implements ILinkController {
//	private static final int MAX_NUM_ANCHOR_RECALCS = 5;
	private final Logger logger = Logger.getLogger(this.getClass());
	private final ICanvasElementAttribute parentAttribute;
	private final ILinkEdge linkAttribute;
	private final ILinkPointDefinition linkDefinition;
	private boolean isActive;
	private final ICanvasAttributeChangeListener srcNodeListener;
	private final ICanvasAttributeChangeListener tgtNodeListener;
	private final ILinkTerminusChangeListener srcTermChangeListener;
	private final ILinkTerminusChangeListener tgtTermChangeListener;
	private final IBendPointContainerListener bpChangeListener;
	private final ICanvasAttributeChangeListener parentDrawingElementPropertyChangeListener;
	private final ICanvasAttributeChangeListener linkAttributePropertyChangeListener;
	
	public LinkController(IViewControllerModel localViewControllerStore, ILinkEdge localLinkAttribute, int index){
		super(localViewControllerStore, index);
		this.linkAttribute = localLinkAttribute;
		this.parentAttribute = (ICanvasElementAttribute)this.linkAttribute.getGraphElement().getParent().getAttribute();
		this.linkDefinition = new LinkPointDefinition(linkAttribute.getAttribute());
		this.srcTermChangeListener = new ILinkTerminusChangeListener() {
			@Override
			public void valueChangeEvent(ILinkTerminusValueChangeEvent e) {
				if(e.getChangeType().equals(LinkTerminusChangeType.LOCATION)){
					Envelope originalDrawnBounds = getDrawnBounds();
					Point newLocation = (Point)e.getNewValue();
					linkDefinition.setSrcAnchorPosition(newLocation);
					notifyDrawnBoundsChanged(originalDrawnBounds, getDrawnBounds());
				}
			}
		};
		this.tgtTermChangeListener = new ILinkTerminusChangeListener() {
			@Override
			public void valueChangeEvent(ILinkTerminusValueChangeEvent e) {
				if(e.getChangeType().equals(LinkTerminusChangeType.LOCATION)){
					Envelope originalDrawnBounds = getDrawnBounds();
					Point newLocation = (Point)e.getNewValue();
					linkDefinition.setTgtAnchorPosition(newLocation);
					notifyDrawnBoundsChanged(originalDrawnBounds, getDrawnBounds());
				}
			}
		};
		this.parentDrawingElementPropertyChangeListener = new ICanvasAttributeChangeListener() {
			
			@Override
			public void propertyChange(ICanvasAttributePropertyChangeEvent e) {
			}
			
			@Override
			public void elementTranslated(ICanvasAttributeTranslationEvent e) {
				linkAttribute.getAttribute().translate(e.getTranslationDelta());
			}
			
			@Override
			public void nodeResized(ICanvasAttributeResizedEvent e) {
			}
		};
		this.bpChangeListener = new IBendPointContainerListener() {
			
			@Override
			public void locationChange(IBendPointLocationChangeEvent e) {
				Point bpPosn = e.getNewPosition();
				int idx = e.getBendPointIndex();
				linkDefinition.setBendPointPosition(idx, bpPosn);
				updateLinksToBendPoints(idx);
			}

			@Override
			public void structureChange(IBendPointStructureChangeEvent e) {
				Envelope originalDrawnBounds = getDrawnBounds();
				if(e.getChangeType().equals(BendPointStructureChange.BEND_POINT_ADDED)){
					int bpIdx = e.getNewIndexPos();
					Point bpPosn = e.getBendPoint();
					linkDefinition.addNewBendPoint(bpIdx, bpPosn);
					updateLinksToBendPoints(bpIdx);
				}
				else if(e.getChangeType().equals(BendPointStructureChange.BEND_POINT_REMOVED)){
					int bpIdx = e.getOldIndexPos();
					linkDefinition.removeBendPoint(bpIdx);
					if(bpIdx < linkDefinition.numBendPoints()){
						// recalculate anchor points on remaining bend-point(s)
//						Point bpPosn = linkAttribute.getAttribute().getBendPointContainer().getBendPoint(bpIdx);
						updateLinksToBendPoints(bpIdx);
					}
					else if(linkDefinition.numBendPoints() == 0){
						// no bend-points
						updateAnchorPoints();
					}
					else{
						// in this case the last bp was removed so we need to take the new last bp
						int lastBpIdx = linkDefinition.numBendPoints()-1;
//						Point bpPosn = linkAttribute.getAttribute().getBendPointContainer().getBendPoint(lastBpIdx);
						updateLinksToBendPoints(lastBpIdx);
					}
				}
				notifyDrawnBoundsChanged(originalDrawnBounds, getDrawnBounds());
			}
		};
		linkAttributePropertyChangeListener = new ICanvasAttributeChangeListener() {
			@Override
			public void propertyChange(ICanvasAttributePropertyChangeEvent e) {
			}
			@Override
			public void elementTranslated(ICanvasAttributeTranslationEvent e) {
				Envelope oldDrawnBounds = linkDefinition.getBounds();
				linkDefinition.translate(e.getTranslationDelta());
				Envelope newDrawnBounds = linkDefinition.getBounds();
				notifyDrawnBoundsChanged(oldDrawnBounds, newDrawnBounds);
			}
			@Override
			public void nodeResized(ICanvasAttributeResizedEvent e) {
			}
		};
		this.srcNodeListener = new ICanvasAttributeChangeListener(){

			@Override
			public void propertyChange(ICanvasAttributePropertyChangeEvent e) {
				if(e.getPropertyChange().equals(CanvasAttributePropertyChange.BOUNDS)){
					if(!e.getAttribute().equals(parentAttribute)){
						// only update the anchor points if this is not a child of the src node - in which case it will be dealt with
						// as a whole link translation
						updateAnchorPoints();
					}
				}
			}

			@Override
			public void elementTranslated(ICanvasAttributeTranslationEvent e) {
			}

			@Override
			public void nodeResized(ICanvasAttributeResizedEvent e) {
			}
			
		};
		this.tgtNodeListener = new ICanvasAttributeChangeListener(){

			@Override
			public void propertyChange(ICanvasAttributePropertyChangeEvent e) {
				if(e.getPropertyChange().equals(CanvasAttributePropertyChange.BOUNDS)){
					if(!e.getAttribute().equals(parentAttribute)){
						// only update the anchor points if this is not a child of the tgt node - in which case it will be dealt with
						// as a whole link translation
						updateAnchorPoints();
					}
				}
			}

			@Override
			public void elementTranslated(ICanvasAttributeTranslationEvent e) {
			}

			@Override
			public void nodeResized(ICanvasAttributeResizedEvent e) {
			}
			
		};
	}
	
//	private void recalculateSrcLinks(){
//		Iterator<ICompoundEdge> edgeIter = this.domainNode.sourceLinkIterator();
//		while(edgeIter.hasNext()){
//			ILinkEdge link = new LinkEdgeFacade(edgeIter.next());
//			ILinkController linkController = this.getViewModel().getLinkController(link);
//			IShapeController srcNode = (IShapeController)this.getViewModel().getNodeController(new ShapeNodeFacade(link.getSourceShape()));
//			IShapeController tgtNode = (IShapeController)this.getViewModel().getNodeController(new ShapeNodeFacade(link.getTargetShape()));
//			changeSourceAnchor(linkController, srcNode, tgtNode);
////			changeTargetAnchor(linkController, srcNode, tgtNode);
//		}
//	}
//
//	private void recalculateTgtLinks(){
//		Iterator<ICompoundEdge> edgeIter = this.domainNode.targetLinkIterator();
//		while(edgeIter.hasNext()){
//			ILinkEdge link = new LinkEdgeFacade(edgeIter.next());
//			ILinkController linkController = this.getViewModel().getLinkController(link);
//			IShapeController srcNode = (IShapeController)this.getViewModel().getNodeController(new ShapeNodeFacade(link.getSourceShape()));
//			IShapeController tgtNode = (IShapeController)this.getViewModel().getNodeController(new ShapeNodeFacade(link.getTargetShape()));
////			changeSourceAnchor(linkController, srcNode, tgtNode);
//			changeTargetAnchor(linkController, srcNode, tgtNode);
//		}
//	}
	
	private void updateAnchorPoints() {
		ILinkDefinitionAnchorCalculator anchorCalc = new LinkDefinitionAnchorCalculator(new LinkPointDefinition(this.linkAttribute.getAttribute()));
		IShapeController srcShapeController = getViewModel().getShapeController(new ShapeNodeFacade(linkAttribute.getSourceShape()));
		anchorCalc.setSrcLocation(srcShapeController.getFigureController().getAnchorLocatorFactory().createAnchorLocator());
		IShapeController tgtShapeController = getViewModel().getShapeController(new ShapeNodeFacade(linkAttribute.getTargetShape()));
		anchorCalc.setTgtLocation(tgtShapeController.getFigureController().getAnchorLocatorFactory().createAnchorLocator());
		anchorCalc.recalculateBothAnchors();
		linkAttribute.getAttribute().getSourceTerminus().setLocation(anchorCalc.getLinkDefinition().getSrcAnchorPosition());
		linkAttribute.getAttribute().getTargetTerminus().setLocation(anchorCalc.getLinkDefinition().getTgtAnchorPosition());
//		int cntr = MAX_NUM_ANCHOR_RECALCS;
//		boolean converged = false;
//		while(cntr-- > 0 && !converged){
//			Point oldSrcLocn = linkAttribute.getAttribute().getSourceTerminus().getLocation();
//			Point oldTgtLocn = linkAttribute.getAttribute().getTargetTerminus().getLocation();
//			updateSrcAnchor(linkAttribute.getAttribute().getTargetTerminus().getLocation());
//			updateTgtAnchor(linkAttribute.getAttribute().getSourceTerminus().getLocation());
//			Point newSrcLocn = linkAttribute.getAttribute().getSourceTerminus().getLocation();
//			Point newTgtLocn = linkAttribute.getAttribute().getTargetTerminus().getLocation();
//			converged = oldSrcLocn.equals(newSrcLocn) && oldTgtLocn.equals(newTgtLocn);
//		}
	}

//	private void updateSrcAnchor(Point otherEndPos){
//		
//		IShapeController shapeController = getViewModel().getShapeController(new ShapeNodeFacade(linkAttribute.getSourceShape()));
//		IAnchorLocator anchorCalc = shapeController.getFigureController().getAnchorLocatorFactory().createAnchorLocator();
//		anchorCalc.setOtherEndPoint(otherEndPos);
//		Point newSrcPosn = anchorCalc.calcAnchorPosition();
//		linkAttribute.getAttribute().getSourceTerminus().setLocation(newSrcPosn);
//	}
	
//	private void updateTgtAnchor(Point otherEndPos){
//		IShapeController shapeController = getViewModel().getShapeController(new ShapeNodeFacade(linkAttribute.getTargetShape()));
//		IAnchorLocator anchorCalc = shapeController.getFigureController().getAnchorLocatorFactory().createAnchorLocator();
//		anchorCalc.setOtherEndPoint(otherEndPos);
//		Point newSrcPosn = anchorCalc.calcAnchorPosition();
//		linkAttribute.getAttribute().getTargetTerminus().setLocation(newSrcPosn);
//	}
	
	private void updateLinksToBendPoints(int bpIdx){
		ILinkDefinitionAnchorCalculator anchorCalc = new LinkDefinitionAnchorCalculator(new LinkPointDefinition(this.linkAttribute.getAttribute()));
		IShapeController srcShapeController = getViewModel().getShapeController(new ShapeNodeFacade(linkAttribute.getSourceShape()));
		anchorCalc.setSrcLocation(srcShapeController.getFigureController().getAnchorLocatorFactory().createAnchorLocator());
		IShapeController tgtShapeController = getViewModel().getShapeController(new ShapeNodeFacade(linkAttribute.getTargetShape()));
		anchorCalc.setTgtLocation(tgtShapeController.getFigureController().getAnchorLocatorFactory().createAnchorLocator());
		// check if bp attached to anchor and recalc anchor if it is
		if(bpIdx == 0){
			anchorCalc.recalculateSrcAnchor();
			linkAttribute.getAttribute().getSourceTerminus().setLocation(anchorCalc.getLinkDefinition().getSrcAnchorPosition());
//			updateSrcAnchor(bpPosn);
		}
		if(bpIdx == linkAttribute.getAttribute().getBendPointContainer().numBendPoints()-1){
			anchorCalc.recalculateTgtAnchor();
			linkAttribute.getAttribute().getTargetTerminus().setLocation(anchorCalc.getLinkDefinition().getTgtAnchorPosition());
//			updateTgtAnchor(bpPosn);
		}
	}
	
	@Override
	public ILinkEdge getDrawingElement() {
		return this.linkAttribute;
	}

	@Override
	public ILinkPointDefinition getLinkDefinition() {
		return this.linkDefinition;
	}

	@Override
	public void activate() {
		this.parentAttribute.addChangeListener(parentDrawingElementPropertyChangeListener);
		this.linkAttribute.getAttribute().getSourceTerminus().addLinkTerminusChangeListener(srcTermChangeListener);
		this.linkAttribute.getAttribute().getTargetTerminus().addLinkTerminusChangeListener(tgtTermChangeListener);
		this.linkAttribute.getAttribute().getBendPointContainer().addChangeListener(this.bpChangeListener);
		this.linkAttribute.getAttribute().addChangeListener(linkAttributePropertyChangeListener);
		((IShapeAttribute)this.linkAttribute.getSourceShape().getAttribute()).addChangeListener(srcNodeListener);
		((IShapeAttribute)this.linkAttribute.getTargetShape().getAttribute()).addChangeListener(tgtNodeListener);
		this.isActive = true;
	}

	@Override
	public void inactivate() {
		this.linkAttribute.getAttribute().getSourceTerminus().removeLinkTerminusChangeListener(srcTermChangeListener);
		this.linkAttribute.getAttribute().getTargetTerminus().removeLinkTerminusChangeListener(tgtTermChangeListener);
		this.linkAttribute.getAttribute().getBendPointContainer().removeChangeListener(this.bpChangeListener);
		this.parentAttribute.removeChangeListener(parentDrawingElementPropertyChangeListener);
		this.linkAttribute.getAttribute().removeChangeListener(linkAttributePropertyChangeListener);
		((IShapeAttribute)this.linkAttribute.getSourceShape().getAttribute()).removeChangeListener(srcNodeListener);
		((IShapeAttribute)this.linkAttribute.getTargetShape().getAttribute()).removeChangeListener(tgtNodeListener);
		this.isActive = false;
	}

	@Override
	public boolean isActive() {
		return this.isActive;
	}

	@Override
	public Envelope getDrawnBounds() {
		return this.linkDefinition.getBounds();
	}

	@Override
	public boolean containsPoint(Point p) {
		boolean retVal = false;
		Envelope bounds = getDrawnBounds(); 
		if(bounds.containsPoint(p)){
//			final double halfLineHeight = this.linkAttribute.getLineWidth() + LINE_HIT_TOLERENCE;
			retVal = this.linkDefinition.containsPoint(p);//, halfLineHeight); 
			if(logger.isTraceEnabled() && retVal){
				logger.trace("Bounds contains point. bounds=" + bounds + ",point=" + p);
			}
		}
		return retVal;
	}

	@Override
	public boolean intersectsBounds(Envelope drawnBounds) {
		boolean retVal = false;
		return this.linkDefinition.intersectsBounds(drawnBounds);
	}

	@Override
	public IMiniCanvas getMiniCanvas() {
		return new DomainLinkMiniCanvas(linkDefinition);
	}

	@Override
	public boolean intersectsHull(IConvexHull queryHull) {
		return this.linkDefinition.intersectsHull(queryHull);
	}
}
