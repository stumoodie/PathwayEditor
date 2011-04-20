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
package org.pathwayeditor.visualeditor.feedback;

import java.util.Iterator;

import org.pathwayeditor.businessobjects.drawingprimitives.ILinkAttribute;
import org.pathwayeditor.businessobjects.typedefn.ILinkAttributeDefaults;
import org.pathwayeditor.businessobjects.typedefn.ILinkObjectType;
import org.pathwayeditor.businessobjects.typedefn.ILinkTerminusDefaults;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.visualeditor.geometry.ILinkPointDefinition;

public class FeedbackLinkBuilder implements IFeedbackLinkBuilder {
	private static int idCount = 0;

	private final FeedbackModel feedbackModel;
	
	public FeedbackLinkBuilder(FeedbackModel feedbackModel){
		this.feedbackModel = feedbackModel;
	}
	
	
	@Override
	public IFeedbackModel getFeedbackModel() {
		return this.feedbackModel;
	}

	private int nextCounter(){
		return idCount++;
	}
	
	@Override
	public IFeedbackLink createFromAttribute(ILinkAttribute linkAttribute) {
		IFeedbackLink retVal = new FeedbackNodelessLink(nextCounter(), linkAttribute.getSourceTerminus().getLocation(), linkAttribute.getTargetTerminus().getLocation());
		ILinkPointDefinition linkDefn = retVal.getLinkDefinition();
		Iterator<Point> bpIter = linkAttribute.getBendPointContainer().bendPointIterator();
		while(bpIter.hasNext()){
			Point bp = bpIter.next();
			linkDefn.addNewBendPoint(bp);
		}
		linkDefn.setLineColour(linkAttribute.getLineColour());
		linkDefn.setLineStyle(linkAttribute.getLineStyle());
		linkDefn.setLineWidth(linkAttribute.getLineWidth());
		linkDefn.getSourceTerminusDefinition().setEndDecoratorType(linkAttribute.getSourceTerminus().getEndDecoratorType());
		linkDefn.getSourceTerminusDefinition().setGap(linkAttribute.getSourceTerminus().getGap());
		linkDefn.getSourceTerminusDefinition().setEndSize(linkAttribute.getSourceTerminus().getEndSize());
		linkDefn.getTargetTerminusDefinition().setEndDecoratorType(linkAttribute.getTargetTerminus().getEndDecoratorType());
		linkDefn.getTargetTerminusDefinition().setGap(linkAttribute.getTargetTerminus().getGap());
		linkDefn.getTargetTerminusDefinition().setEndSize(linkAttribute.getTargetTerminus().getEndSize());
//		final ILinkDefinitionAnchorCalculator anchorCalc = new LinkDefinitionAnchorCalculator(linkDefn);
//		if(srcNode != null){
//			srcNode.addFeedbackNodeListener(new IFeedbackNodeListener(){
//				@Override
//				public void nodeTranslationEvent(IFeedbackNodeTranslationEvent e) {
//					anchorCalc.setSrcLocation(srcNode.getFigureController().getAnchorLocatorFactory().createAnchorLocator());
//					anchorCalc.recalculateBothAnchors();
//				}
//				@Override
//				public void nodeResizeEvent(IFeedbackNodeResizeEvent e) {
//					anchorCalc.setSrcLocation(srcNode.getFigureController().getAnchorLocatorFactory().createAnchorLocator());
//					anchorCalc.recalculateBothAnchors();
//				}
//			});
//		}
//		if(tgtNode != null){
//			tgtNode.addFeedbackNodeListener(new IFeedbackNodeListener(){
//				@Override
//				public void nodeTranslationEvent(IFeedbackNodeTranslationEvent e) {
//					anchorCalc.setTgtLocation(tgtNode.getFigureController().getAnchorLocatorFactory().createAnchorLocator());
//					anchorCalc.recalculateBothAnchors();
//				}
//				@Override
//				public void nodeResizeEvent(IFeedbackNodeResizeEvent e) {
//					anchorCalc.setTgtLocation(tgtNode.getFigureController().getAnchorLocatorFactory().createAnchorLocator());
//					anchorCalc.recalculateBothAnchors();
//				}
//			});
//		}
//		retVal.addFeedbackLinkListener(new IFeedbackLinkListener() {
//			@Override
//			public void linkChangeEvent(IFeedbackLinkChangeEvent e) {
//				if(srcNode != null){
//					anchorCalc.setSrcLocation(srcNode.getFigureController().getAnchorLocatorFactory().createAnchorLocator());
//				}
//				if(tgtNode != null){
//					anchorCalc.setTgtLocation(tgtNode.getFigureController().getAnchorLocatorFactory().createAnchorLocator());
//				}
//				anchorCalc.recalculateBothAnchors();
//			}
//		});
		this.feedbackModel.addEdge(retVal);
		return retVal;
	}


//	@Override
//	public IFeedbackLink createFromObjectType(final IFeedbackNode srcNode, final IFeedbackNode tgtNode, ILinkObjectType objectType) {
////		IAnchorLocator srcAnchorLocator = srcNode.getFigureController().getAnchorLocatorFactory().createAnchorLocator();
////		IAnchorLocator tgtAnchorLocator = tgtNode.getFigureController().getAnchorLocatorFactory().createAnchorLocator();
////		IFeedbackLink retVal = new FeedbackLink(srcNode,	tgtNode, nextCounter(),
////				srcNode.getConvexHull().getCentre(), srcAnchorLocator,
////				tgtNode.getConvexHull().getCentre(), tgtAnchorLocator);
//		IFeedbackLink retVal = new FeedbackNodelessLink(nextCounter(), srcNode.getConvexHull().getCentre(),
//				tgtNode.getConvexHull().getCentre());
//		final ILinkDefinitionAnchorCalculator anchorCalc = new LinkDefinitionAnchorCalculator(retVal.getLinkDefinition());
//		srcNode.addFeedbackNodeListener(new IFeedbackNodeListener(){
//			@Override
//			public void nodeTranslationEvent(IFeedbackNodeTranslationEvent e) {
//				anchorCalc.setSrcLocation(srcNode.getFigureController().getAnchorLocatorFactory().createAnchorLocator());
//				anchorCalc.recalculateBothAnchors();
//			}
//			@Override
//			public void nodeResizeEvent(IFeedbackNodeResizeEvent e) {
//				anchorCalc.setSrcLocation(srcNode.getFigureController().getAnchorLocatorFactory().createAnchorLocator());
//				anchorCalc.recalculateBothAnchors();
//			}
//		});
//		tgtNode.addFeedbackNodeListener(new IFeedbackNodeListener(){
//			@Override
//			public void nodeTranslationEvent(IFeedbackNodeTranslationEvent e) {
//				anchorCalc.setTgtLocation(tgtNode.getFigureController().getAnchorLocatorFactory().createAnchorLocator());
//				anchorCalc.recalculateBothAnchors();
//			}
//			@Override
//			public void nodeResizeEvent(IFeedbackNodeResizeEvent e) {
//				anchorCalc.setTgtLocation(tgtNode.getFigureController().getAnchorLocatorFactory().createAnchorLocator());
//				anchorCalc.recalculateBothAnchors();
//			}
//		});
//		retVal.addFeedbackLinkListener(new IFeedbackLinkListener() {
//			@Override
//			public void linkChangeEvent(IFeedbackLinkChangeEvent e) {
//				anchorCalc.setSrcLocation(srcNode.getFigureController().getAnchorLocatorFactory().createAnchorLocator());
//				anchorCalc.setTgtLocation(tgtNode.getFigureController().getAnchorLocatorFactory().createAnchorLocator());
//				anchorCalc.recalculateBothAnchors();
//			}
//		});
//		this.feedbackModel.addEdge(retVal);
//		return buildFromObjectType(retVal, objectType);
//	}


	@Override
	public IFeedbackLink createNodelessLinkFromObjectType(Point srcPosn, Point tgtPosn, ILinkObjectType linkObjectType) {
		IFeedbackLink retVal = buildFromObjectType(new FeedbackNodelessLink(nextCounter(), srcPosn, tgtPosn), linkObjectType);
		this.feedbackModel.addEdge(retVal);
		return retVal;
	}


	private IFeedbackLink buildFromObjectType(IFeedbackLink feedbackLink, ILinkObjectType objectType) {
		ILinkPointDefinition linkDefn = feedbackLink.getLinkDefinition();
		ILinkAttributeDefaults linkAttribute = objectType.getDefaultAttributes();
		linkDefn.setLineColour(linkAttribute.getLineColour());
		linkDefn.setLineStyle(linkAttribute.getLineStyle());
		linkDefn.setLineWidth(linkAttribute.getLineWidth());
		ILinkTerminusDefaults srcDefaults = objectType.getSourceTerminusDefinition().getDefaultAttributes();
		linkDefn.getSourceTerminusDefinition().setEndDecoratorType(srcDefaults.getEndDecoratorType());
		linkDefn.getSourceTerminusDefinition().setGap(srcDefaults.getGap());
		linkDefn.getSourceTerminusDefinition().setEndSize(srcDefaults.getEndSize());
		ILinkTerminusDefaults tgtDefaults = objectType.getTargetTerminusDefinition().getDefaultAttributes();
		linkDefn.getTargetTerminusDefinition().setEndDecoratorType(tgtDefaults.getEndDecoratorType());
		linkDefn.getTargetTerminusDefinition().setGap(tgtDefaults.getGap());
		linkDefn.getTargetTerminusDefinition().setEndSize(tgtDefaults.getEndSize());
		return feedbackLink;
	}

}
