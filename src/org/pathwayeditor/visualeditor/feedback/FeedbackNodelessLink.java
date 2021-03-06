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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.visualeditor.editingview.IMiniCanvas;
import org.pathwayeditor.visualeditor.geometry.ILinkPointDefinition;
import org.pathwayeditor.visualeditor.geometry.LinkPointDefinition;

public class FeedbackNodelessLink implements IFeedbackLink {
	private final ILinkPointDefinition linkDefinition;
	private Point lastDelta = null;
	private final int elementIdentifier;
	private final List<IFeedbackLinkListener> listeners;
	
	public FeedbackNodelessLink(int elementIdentifier, Point srcAnchor, Point tgtAnchor) {
		this.listeners = new LinkedList<IFeedbackLinkListener>();
		this.elementIdentifier = elementIdentifier;
		this.linkDefinition = new LinkPointDefinition(srcAnchor, tgtAnchor);
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
//		updateLinksToBendPoints(bpIdx, newLocation);
		notifyLinkChange(origDefn, this.linkDefinition);
	}

//	private void updateLinksToBendPoints(int bpIdx, Point bpPosn){
//		// check if bp attached to anchor and recalc anchor if it is
//		if(bpIdx == 0){
//			updateSrcAnchor(bpPosn);
//		}
//		if(bpIdx == this.linkDefinition.numBendPoints()-1){
//			updateTgtAnchor(bpPosn);
//		}
//	}
	
//	private void updateSrcAnchor(Point otherEndPos){
//		this.linkDefinition.setSrcAnchorPosition(otherEndPos);
//	}
	
//	private void updateTgtAnchor(Point otherEndPos){
//		this.linkDefinition.setTgtAnchorPosition(otherEndPos);
//	}

	@Override
	public void newBendPoint(int newBpIdx, Point initialLocation) {
		ILinkPointDefinition origDefn = this.linkDefinition.getCopy();;
		this.linkDefinition.addNewBendPoint(newBpIdx, initialLocation);
//		updateLinksToBendPoints(newBpIdx, initialLocation);
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
		if (!(obj instanceof FeedbackNodelessLink))
			return false;
		FeedbackNodelessLink other = (FeedbackNodelessLink) obj;
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
	
	@Override
	public String toString(){
		StringBuilder buf = new StringBuilder(this.getClass().getSimpleName());
		buf.append("(");
		buf.append("elementIdentifier=");
		buf.append(this.elementIdentifier);
		buf.append(",srcPosn=");
		buf.append(this.linkDefinition.getSrcAnchorPosition());
		buf.append(",tgtPosn=");
		buf.append(this.linkDefinition.getTgtAnchorPosition());
		buf.append(")");
		return buf.toString();
	}

	@Override
	public IMiniCanvas getMiniCanvas() {
		return new FeedbackLinkMiniCanvas(linkDefinition);
	}
}
