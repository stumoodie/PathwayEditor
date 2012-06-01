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

import org.pathwayeditor.businessobjects.drawingprimitives.ILabelNode;
import org.pathwayeditor.businessobjects.drawingprimitives.ILinkAttribute;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.IBendPointContainerListener;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.IBendPointLocationChangeEvent;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.IBendPointStructureChangeEvent;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.ICanvasAttributeChangeListener;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.ICanvasAttributePropertyChangeEvent;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.ICanvasAttributeResizedEvent;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.ICanvasAttributeTranslationEvent;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.ICurveSegmentLocationChangeEvent;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.ILinkTerminusChangeListener;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.ILinkTerminusValueChangeEvent;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.LinkTerminusChangeType;
import org.pathwayeditor.figure.geometry.LineSegment;
import org.pathwayeditor.figure.geometry.Point;

public class LinkLabelController extends CommonLabelController implements ILabelController {
	private final ILinkAttribute parentAttribute;
	private final ICanvasAttributeChangeListener parentDrawingNodePropertyChangeListener;
	private final IBendPointContainerListener parentLinkBendpointChangeListener;
	private final ILinkTerminusChangeListener parentSourceLinkterminusChangeListener;
	private final ILinkTerminusChangeListener parentTargetLinkterminusChangeListener;
	
	public LinkLabelController(IViewControllerModel viewModel, final ILabelNode node, int index) {
		super(viewModel, node, index);
		this.parentAttribute = (ILinkAttribute)node.getGraphElement().getParent().getAttribute();
		parentDrawingNodePropertyChangeListener = new ICanvasAttributeChangeListener() {
			
			@Override
			public void propertyChange(ICanvasAttributePropertyChangeEvent e) {
			}
			
			@Override
			public void elementTranslated(ICanvasAttributeTranslationEvent e) {
				node.getAttribute().translate(e.getTranslationDelta());
			}
			
			@Override
			public void nodeResized(ICanvasAttributeResizedEvent e) {
			}
		};
		this.parentLinkBendpointChangeListener = new IBendPointContainerListener() {
			
			
			@Override
			public void locationChange(IBendPointLocationChangeEvent e) {
				// don't recalculate label posn at moment.
			}

			@Override
			public void structureChange(IBendPointStructureChangeEvent e) {
				
			}
		};
		this.parentSourceLinkterminusChangeListener = new ILinkTerminusChangeListener() {
			
			@Override
			public void valueChangeEvent(ILinkTerminusValueChangeEvent e) {
				if(e.getChangeType().equals(LinkTerminusChangeType.LOCATION)){
					recalculateLabelPosition();
				}
			}
			
		};
		this.parentTargetLinkterminusChangeListener = new ILinkTerminusChangeListener() {
			
			@Override
			public void valueChangeEvent(ILinkTerminusValueChangeEvent e) {
				if(e.getChangeType().equals(LinkTerminusChangeType.LOCATION)){
					recalculateLabelPosition();
				}
			}
			
		};
	}

	private void recalculateLabelPosition(){
		LineSegment originalLink = new LineSegment(this.parentAttribute.getSourceTerminus().getLocation(),
				this.parentAttribute.getTargetTerminus().getLocation());
		Point linkMidPoint = originalLink.getMidPoint();
		Point originalCentrePosn = getDrawingElement().getAttribute().getBounds().getCentre();
		Point labelTranslation = originalCentrePosn.difference(linkMidPoint);
		getDrawingElement().getAttribute().translate(labelTranslation);
	}
	
	@Override
	public void inactivateOverride() {
		parentAttribute.removeChangeListener(parentDrawingNodePropertyChangeListener);
		this.parentAttribute.getBendPointContainer().removeChangeListener(parentLinkBendpointChangeListener);
		this.parentAttribute.getSourceTerminus().removeLinkTerminusChangeListener(parentSourceLinkterminusChangeListener);
		this.parentAttribute.getTargetTerminus().removeLinkTerminusChangeListener(parentTargetLinkterminusChangeListener);
	}


	@Override
	public void activateOverride() {
		parentAttribute.addChangeListener(parentDrawingNodePropertyChangeListener);
		this.parentAttribute.getBendPointContainer().addChangeListener(parentLinkBendpointChangeListener);
		this.parentAttribute.getSourceTerminus().addLinkTerminusChangeListener(parentSourceLinkterminusChangeListener);
		this.parentAttribute.getTargetTerminus().addLinkTerminusChangeListener(parentTargetLinkterminusChangeListener);
	}
}
