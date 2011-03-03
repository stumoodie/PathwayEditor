package org.pathwayeditor.visualeditor.feedback;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.figure.rendering.IAnchorLocator;
import org.pathwayeditor.visualeditor.geometry.ILinkPointDefinition;
import org.pathwayeditor.visualeditor.geometry.LinkPointDefinition;

public class FeedbackLink implements IFeedbackLink {
	private final ILinkPointDefinition linkDefinition;
	private IAnchorLocator srcAnchorCalc;
	private IAnchorLocator tgtAnchorCalc;
	private Point lastDelta = null;
	private final int elementIdentifier;
	private final IFeedbackNodeListener srcFeedbackNodeListener;
	private final IFeedbackNodeListener tgtFeedbackNodeListener;
	private final List<IFeedbackLinkListener> listeners;
	
	public FeedbackLink(FeedbackNode srcNode, FeedbackNode tgtNode, int elementIdentifier, Point srcAnchor, IAnchorLocator srcAnchorLocator, Point tgtAnchor, IAnchorLocator tgtAnchorLocator) {
		this.listeners = new LinkedList<IFeedbackLinkListener>();
		this.elementIdentifier = elementIdentifier;
		this.linkDefinition = new LinkPointDefinition(srcAnchor, tgtAnchor);
		this.srcAnchorCalc = srcAnchorLocator;
		this.tgtAnchorCalc = tgtAnchorLocator;
		this.srcFeedbackNodeListener = new IFeedbackNodeListener() {
			
			@Override
			public void nodeTranslationEvent(IFeedbackNodeTranslationEvent e) {
				IFeedbackNode srcNode = e.getNode();
				srcAnchorCalc = srcNode.getFigureController().getAnchorLocatorFactory().createAnchorLocator();
				ILinkPointDefinition origDefn = linkDefinition.getCopy();;
				updateSrcAnchor(linkDefinition.getSourceLineSegment().getTerminus());
				updateTgtAnchor(linkDefinition.getTargetLineSegment().getTerminus());
				notifyLinkChange(origDefn, linkDefinition);
			}
			
			@Override
			public void nodeResizeEvent(IFeedbackNodeResizeEvent e) {
				IFeedbackNode srcNode = e.getNode();
				srcAnchorCalc = srcNode.getFigureController().getAnchorLocatorFactory().createAnchorLocator();
				ILinkPointDefinition origDefn = linkDefinition.getCopy();;
				updateSrcAnchor(linkDefinition.getSourceLineSegment().getTerminus());
				updateTgtAnchor(linkDefinition.getTargetLineSegment().getTerminus());
				notifyLinkChange(origDefn, linkDefinition);
			}
		};
		this.tgtFeedbackNodeListener = new IFeedbackNodeListener() {
			
			@Override
			public void nodeTranslationEvent(IFeedbackNodeTranslationEvent e) {
				IFeedbackNode tgtNode = e.getNode();
				tgtAnchorCalc = tgtNode.getFigureController().getAnchorLocatorFactory().createAnchorLocator();
				ILinkPointDefinition origDefn = linkDefinition.getCopy();;
				updateTgtAnchor(linkDefinition.getTargetLineSegment().getTerminus());
				updateSrcAnchor(linkDefinition.getSourceLineSegment().getTerminus());
				notifyLinkChange(origDefn, linkDefinition);
			}
			
			@Override
			public void nodeResizeEvent(IFeedbackNodeResizeEvent e) {
				IFeedbackNode tgtNode = e.getNode();
				tgtAnchorCalc = tgtNode.getFigureController().getAnchorLocatorFactory().createAnchorLocator();
				ILinkPointDefinition origDefn = linkDefinition.getCopy();;
				updateTgtAnchor(linkDefinition.getTargetLineSegment().getTerminus());
				updateSrcAnchor(linkDefinition.getSourceLineSegment().getTerminus());
				notifyLinkChange(origDefn, linkDefinition);
			}
		};
		if(srcNode != null){
			srcNode.addFeedbackNodeListener(this.srcFeedbackNodeListener);
		}
		if(tgtNode != null){
			tgtNode.addFeedbackNodeListener(this.tgtFeedbackNodeListener);
		}
	}

	@Override
	public int getElementIdentifier() {
		return elementIdentifier;
	}

	@Override
	public ILinkPointDefinition getLinkDefinition() {
		return this.linkDefinition;
	}

	@Override
	public void translatePrimitive(Point translation) {
		Point delta = translation;
		if(this.lastDelta != null){
			delta = lastDelta.difference(translation);
		}
		ILinkPointDefinition origDefn = this.linkDefinition.getCopy();;
		this.linkDefinition.translate(delta); 
		lastDelta = translation;
		notifyLinkChange(origDefn, this.linkDefinition);
	}

	@Override
	public void moveBendPoint(int bpIdx, Point translation) {
		Point delta = translation;
		if(this.lastDelta != null){
			delta = lastDelta.difference(translation);
		}
		ILinkPointDefinition origDefn = this.linkDefinition.getCopy();
		Point newLocation = this.linkDefinition.getBendPointPosition(bpIdx).translate(delta);
		this.linkDefinition.setBendPointPosition(bpIdx, newLocation);
		lastDelta = translation;
		updateLinksToBendPoints(bpIdx, newLocation);
		notifyLinkChange(origDefn, this.linkDefinition);
	}

	private void updateLinksToBendPoints(int bpIdx, Point bpPosn){
		// check if bp attached to anchor and recalc anchor if it is
		if(bpIdx == 0){
			updateSrcAnchor(bpPosn);
		}
		if(bpIdx == this.linkDefinition.numBendPoints()-1){
			updateTgtAnchor(bpPosn);
		}
	}
	
	private void updateSrcAnchor(Point otherEndPos){
		srcAnchorCalc.setOtherEndPoint(otherEndPos);
		Point newSrcPosn = srcAnchorCalc.calcAnchorPosition();
		this.linkDefinition.setSrcAnchorPosition(newSrcPosn);
	}
	
	private void updateTgtAnchor(Point otherEndPos){
		tgtAnchorCalc.setOtherEndPoint(otherEndPos);
		Point newSrcPosn = tgtAnchorCalc.calcAnchorPosition();
		this.linkDefinition.setTgtAnchorPosition(newSrcPosn);
	}

	@Override
	public void newBendPoint(int newBpIdx, Point initialLocation) {
		ILinkPointDefinition origDefn = this.linkDefinition.getCopy();;
		this.linkDefinition.addNewBendPoint(newBpIdx, initialLocation);
		updateLinksToBendPoints(newBpIdx, initialLocation);
		notifyLinkChange(origDefn, this.linkDefinition);
	}

	@Override
	public void translateBendPoint(int bpIdx, Point translation) {
		Point delta = translation;
		if(this.lastDelta != null){
			delta = lastDelta.difference(translation);
		}
		ILinkPointDefinition origDefn = this.linkDefinition.getCopy();;
		this.linkDefinition.translate(delta); 
		lastDelta = translation;
		notifyLinkChange(origDefn, this.linkDefinition);
	}
	
	private void notifyLinkChange(final ILinkPointDefinition origDefn, final ILinkPointDefinition newDefn){
		IFeedbackLinkChangeEvent e = new IFeedbackLinkChangeEvent(){

			@Override
			public ILinkPointDefinition getNewLinkDefintion() {
				return newDefn;
			}

			@Override
			public ILinkPointDefinition getOriginalLinkDefinition() {
				return origDefn;
			}
			
		};
		for(IFeedbackLinkListener l : this.listeners){
			l.linkChangeEvent(e);
		}
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

	@Override
	public void addFeedbackLinkListener(IFeedbackLinkListener l) {
		this.listeners.add(l);
	}

	@Override
	public List<IFeedbackLinkListener> getFeedbackLinkListeners() {
		return new ArrayList<IFeedbackLinkListener>(this.listeners);
	}

	@Override
	public void removeFeedbackLinkListener(IFeedbackLinkListener l) {
		this.listeners.remove(l);
	}
}
