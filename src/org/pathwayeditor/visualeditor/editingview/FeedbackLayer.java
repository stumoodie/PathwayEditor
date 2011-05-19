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
package org.pathwayeditor.visualeditor.editingview;

import java.awt.Graphics2D;
import java.util.Iterator;

import org.pathwayeditor.visualeditor.feedback.IFeedbackLink;
import org.pathwayeditor.visualeditor.feedback.IFeedbackModel;
import org.pathwayeditor.visualeditor.feedback.IFeedbackNode;

public class FeedbackLayer implements IFeedbackLayer {
//	private final Logger logger = Logger.getLogger(this.getClass());
	private final IFeedbackModel feedbackModel;
	
	
	public FeedbackLayer(IFeedbackModel viewControllerStore) {
		this.feedbackModel = viewControllerStore;
	}

	/* (non-Javadoc)
	 * @see org.pathwayeditor.graphicsengine.ICanvasDrawer#paint(java.awt.Graphics2D)
	 */
	@Override
	public void paint(Graphics2D g2d){
//		Rectangle rectangleBounds = g2d.getClipBounds();
//		if(logger.isDebugEnabled()){
//			logger.debug("Refreshing the clipped bounds=" + rectangleBounds);
//		}
//		Envelope updateBound =  new Envelope(rectangleBounds.getX(),rectangleBounds.getY(), rectangleBounds.getWidth(), rectangleBounds.getHeight());;
		paintShapes(g2d);
		paintLinks(g2d);
	}

	private void paintShapes(Graphics2D g2d){
		Iterator<IFeedbackNode> shapeIter = this.feedbackModel.nodeIterator();
		while(shapeIter.hasNext()){
			IFeedbackNode shapeNode = shapeIter.next();
			shapeNode.getMiniCanvas().paint(g2d);
		}
	}
	
	private void paintLinks(Graphics2D g2d){
		Iterator<IFeedbackLink> linkIter = this.feedbackModel.linkIterator();
		while(linkIter.hasNext()){
			IFeedbackLink feedbackLink = linkIter.next();
			feedbackLink.getMiniCanvas().paint(g2d);
		}
	}
	
	@Override
	public IFeedbackModel getFeedbackModel() {
		return this.feedbackModel;
	}

	@Override
	public LayerType getLayerType() {
		return LayerType.FEEDBACK;
	}
}
