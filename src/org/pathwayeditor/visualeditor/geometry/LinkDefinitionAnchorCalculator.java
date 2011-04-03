package org.pathwayeditor.visualeditor.geometry;

import org.apache.log4j.Logger;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.figure.rendering.IAnchorLocator;

public class LinkDefinitionAnchorCalculator implements ILinkDefinitionAnchorCalculator {
	private final Logger logger = Logger.getLogger(this.getClass());
	private static final int MAX_NUM_ANCHOR_RECALCS = 5;
	private ILinkPointDefinition linkDefn;
	private IAnchorLocator srcAnchorLocator;
	private IAnchorLocator tgtAnchorLocator;

	public LinkDefinitionAnchorCalculator(ILinkPointDefinition linkDefn){
		this.linkDefn = linkDefn;
	}
	
	@Override
	public ILinkPointDefinition getLinkDefinition() {
		return this.linkDefn;
	}

	@Override
	public IAnchorLocator getSrcLocator() {
		return this.srcAnchorLocator;
	}

	@Override
	public void setSrcLocation(IAnchorLocator anchorLocator) {
		this.srcAnchorLocator = anchorLocator;
	}

	@Override
	public IAnchorLocator getTgtLocator() {
		return this.tgtAnchorLocator;
	}

	@Override
	public void setTgtLocation(IAnchorLocator anchorLocator) {
		this.tgtAnchorLocator = anchorLocator;
	}
	
	private static Point recalculateAnchor(IAnchorLocator anchorLocator, Point refPoint){
		anchorLocator.setOtherEndPoint(refPoint);
		Point newAnchorPosn = anchorLocator.calcAnchorPosition();
		return newAnchorPosn;
	}

	@Override
	public void recalculateSrcAnchor() {
		if(this.srcAnchorLocator != null){
			Point newSrcPosn = recalculateAnchor(this.srcAnchorLocator, this.linkDefn.getSourceLineSegment().getTerminus());
			this.linkDefn.setSrcAnchorPosition(newSrcPosn);
			if(logger.isTraceEnabled()){
				logger.trace("Src anchor recalculated. Posn=" + newSrcPosn);
			}
		}
	}

	@Override
	public void recalculateTgtAnchor() {
		if(this.tgtAnchorLocator != null){
			Point newTgtPosn = recalculateAnchor(this.tgtAnchorLocator, this.linkDefn.getTargetLineSegment().getTerminus());
			this.linkDefn.setTgtAnchorPosition(newTgtPosn);
			if(logger.isTraceEnabled()){
				logger.trace("Tgt anchor recalculated. Posn=" + newTgtPosn);
			}
		}
	}

	@Override
	public void recalculateBothAnchors() {
		int cntr = MAX_NUM_ANCHOR_RECALCS;
		boolean converged = false;
		while(cntr-- > 0 && !converged){
			Point oldSrcLocn = this.linkDefn.getSrcAnchorPosition();
			Point oldTgtLocn = this.linkDefn.getTgtAnchorPosition();
			recalculateSrcAnchor();
			recalculateTgtAnchor();
			Point newSrcLocn = this.linkDefn.getSrcAnchorPosition();
			Point newTgtLocn = this.linkDefn.getTgtAnchorPosition();
			converged = oldSrcLocn.equals(newSrcLocn) && oldTgtLocn.equals(newTgtLocn);
			if(logger.isTraceEnabled()){
				logger.trace("Recalculating both anchors. Src: oldPosn=" + oldSrcLocn + ",newPosn=" + newSrcLocn +
						" - Tgt: oldPosn=" + oldTgtLocn + ",newPosn=" + newTgtLocn);
			}
		}
	}

}
