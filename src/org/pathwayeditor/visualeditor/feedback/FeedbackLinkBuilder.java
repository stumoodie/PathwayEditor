package org.pathwayeditor.visualeditor.feedback;

import java.util.Iterator;

import org.pathwayeditor.businessobjects.drawingprimitives.ILinkAttribute;
import org.pathwayeditor.businessobjects.typedefn.ILinkAttributeDefaults;
import org.pathwayeditor.businessobjects.typedefn.ILinkObjectType;
import org.pathwayeditor.businessobjects.typedefn.ILinkTerminusDefaults;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.figure.rendering.IAnchorLocator;
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
	public IFeedbackLink createFromAttribute(IFeedbackNode srcNode, IFeedbackNode tgtNode, ILinkAttribute linkAttribute,
			Point srcAnchorLocn, IAnchorLocator srcAnchorLocator, Point tgtAnchorLocation, IAnchorLocator tgtAnchorLocator) {
		FeedbackLink retVal = new FeedbackLink(srcNode,	tgtNode, nextCounter(), srcAnchorLocn, srcAnchorLocator, tgtAnchorLocation, tgtAnchorLocator);
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
		this.feedbackModel.addEdge(retVal);
		return retVal;
	}


	@Override
	public IFeedbackLink createFromObjectType(IFeedbackNode srcNode, IFeedbackNode tgtNode, ILinkObjectType objectType) {
		IAnchorLocator srcAnchorLocator = srcNode.getFigureController().getAnchorLocatorFactory().createAnchorLocator();
		IAnchorLocator tgtAnchorLocator = tgtNode.getFigureController().getAnchorLocatorFactory().createAnchorLocator();
		FeedbackLink retVal = new FeedbackLink(srcNode,	tgtNode, nextCounter(),
				srcNode.getConvexHull().getCentre(), srcAnchorLocator,
				tgtNode.getConvexHull().getCentre(), tgtAnchorLocator);
		this.feedbackModel.addEdge(retVal);
		return buildFromObjectType(retVal, objectType);
	}


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
