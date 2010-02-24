package org.pathwayeditor.visualeditor.feedback;

import org.pathwayeditor.figure.figuredefn.IAnchorLocator;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.visualeditor.geometry.ILinkPointDefinition;
import org.pathwayeditor.visualeditor.geometry.LinkPointDefinition;

public class FeedbackLink implements IFeedbackLink {
	private final ILinkPointDefinition originalLinkDefinition;
	private IAnchorLocator srcAnchorCalc;
	private IAnchorLocator tgtAnchorCalc;
	private Point lastDelta = null;
	private final int elementIdentifier;
	private final IFeedbackNodeListener srcFeedbackNodeListener;
	private final IFeedbackNodeListener tgtFeedbackNodeListener;
	
	public FeedbackLink(FeedbackNode srcNode, FeedbackNode tgtNode, int elementIdentifier, Point srcAnchor, IAnchorLocator srcAnchorLocator, Point tgtAnchor, IAnchorLocator tgtAnchorLocator) {
		this.elementIdentifier = elementIdentifier;
		this.originalLinkDefinition = new LinkPointDefinition(srcAnchor, tgtAnchor);
		this.srcAnchorCalc = srcAnchorLocator;
		this.tgtAnchorCalc = tgtAnchorLocator;
		this.srcFeedbackNodeListener = new IFeedbackNodeListener() {
			
			@Override
			public void nodeTranslationEvent(IFeedbackNodeTranslationEvent e) {
				IFeedbackNode srcNode = e.getNode();
				srcAnchorCalc = srcNode.getFigureController().getAnchorLocatorFactory().createAnchorLocator();
				updateSrcAnchor(originalLinkDefinition.getSourceLineSegment().getTerminus());
				updateTgtAnchor(originalLinkDefinition.getTargetLineSegment().getTerminus());
			}
			
			@Override
			public void nodeResizeEvent(IFeedbackNodeResizeEvent e) {
				// TODO Auto-generated method stub
				
			}
		};
		this.tgtFeedbackNodeListener = new IFeedbackNodeListener() {
			
			@Override
			public void nodeTranslationEvent(IFeedbackNodeTranslationEvent e) {
				IFeedbackNode tgtNode = e.getNode();
				tgtAnchorCalc = tgtNode.getFigureController().getAnchorLocatorFactory().createAnchorLocator();
				updateTgtAnchor(originalLinkDefinition.getTargetLineSegment().getTerminus());
				updateSrcAnchor(originalLinkDefinition.getSourceLineSegment().getTerminus());
			}
			
			@Override
			public void nodeResizeEvent(IFeedbackNodeResizeEvent e) {
				// TODO Auto-generated method stub
				
			}
		};
		if(srcNode != null){
			srcNode.addFeedbackNodeListener(this.srcFeedbackNodeListener);
		}
		if(tgtNode != null){
			tgtNode.addFeedbackNodeListener(this.tgtFeedbackNodeListener);
		}
	}

	public int getElementIdentifier() {
		return elementIdentifier;
	}

	@Override
	public ILinkPointDefinition getLinkDefinition() {
		return this.originalLinkDefinition;
	}

	@Override
	public void translatePrimitive(Point translation) {
		Point delta = translation;
		if(this.lastDelta != null){
			delta = lastDelta.difference(translation);
		}
		this.originalLinkDefinition.translate(delta); 
		lastDelta = translation;
	}

	@Override
	public void moveBendPoint(int bpIdx, Point newLocation) {
		this.originalLinkDefinition.setBendPointPosition(bpIdx, newLocation);
		updateLinksToBendPoints(bpIdx, newLocation);
	}

	private void updateLinksToBendPoints(int bpIdx, Point bpPosn){
		// check if bp attached to anchor and recalc anchor if it is
		if(bpIdx == 0){
			updateSrcAnchor(bpPosn);
		}
		if(bpIdx == this.originalLinkDefinition.numBendPoints()-1){
			updateTgtAnchor(bpPosn);
		}
	}
	
	private void updateSrcAnchor(Point otherEndPos){
		srcAnchorCalc.setOtherEndPoint(otherEndPos);
		Point newSrcPosn = srcAnchorCalc.calcAnchorPosition();
		this.originalLinkDefinition.setSrcAnchorPosition(newSrcPosn);
	}
	
	private void updateTgtAnchor(Point otherEndPos){
		tgtAnchorCalc.setOtherEndPoint(otherEndPos);
		Point newSrcPosn = tgtAnchorCalc.calcAnchorPosition();
		this.originalLinkDefinition.setTgtAnchorPosition(newSrcPosn);
	}

	@Override
	public void newBendPoint(int newBpIdx, Point initialLocation) {
		this.originalLinkDefinition.addNewBendPoint(newBpIdx, initialLocation);
		updateLinksToBendPoints(newBpIdx, initialLocation);
	}

	@Override
	public void translateBendPoint(int bpIdx, Point translation) {
		Point delta = translation;
		if(this.lastDelta != null){
			delta = lastDelta.difference(translation);
		}
		this.originalLinkDefinition.translate(delta); 
		lastDelta = translation;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + elementIdentifier;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof FeedbackLink))
			return false;
		FeedbackLink other = (FeedbackLink) obj;
		if (elementIdentifier != other.elementIdentifier)
			return false;
		return true;
	}
}
