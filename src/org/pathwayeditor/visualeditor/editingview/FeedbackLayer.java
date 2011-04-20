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

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.pathwayeditor.figure.geometry.Envelope;
import org.pathwayeditor.figure.rendering.FigureRenderer;
import org.pathwayeditor.figure.rendering.IFigureRenderingController;
import org.pathwayeditor.figure.rendering.IGraphicsEngine;
import org.pathwayeditor.graphicsengine.Java2DGraphicsEngine;
import org.pathwayeditor.visualeditor.feedback.IFeedbackLink;
import org.pathwayeditor.visualeditor.feedback.IFeedbackModel;
import org.pathwayeditor.visualeditor.feedback.IFeedbackNode;

public class FeedbackLayer implements IFeedbackLayer {
	private final Logger logger = Logger.getLogger(this.getClass());
	private final IFeedbackModel feedbackModel;
	
	
	public FeedbackLayer(IFeedbackModel viewControllerStore) {
		this.feedbackModel = viewControllerStore;
	}

	/* (non-Javadoc)
	 * @see org.pathwayeditor.graphicsengine.ICanvasDrawer#paint(java.awt.Graphics2D)
	 */
	@Override
	public void paint(Graphics2D g2d){
		Rectangle rectangleBounds = g2d.getClipBounds();
		if(logger.isDebugEnabled()){
			logger.debug("Refreshing the clipped bounds=" + rectangleBounds);
		}
		Envelope updateBound =  new Envelope(rectangleBounds.getX(),rectangleBounds.getY(), rectangleBounds.getWidth(), rectangleBounds.getHeight());;
		paintShapes(g2d, updateBound);
		paintLinks(g2d, updateBound);
	}

	private void paintShapes(Graphics2D g2d, Envelope updateBound){
		IGraphicsEngine graphicsEngine = new Java2DGraphicsEngine(g2d);
		Iterator<IFeedbackNode> shapeIter = this.feedbackModel.nodeIterator();
		final Composite original = g2d.getComposite();
		final AlphaComposite alpha = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f);
		g2d.setComposite(alpha);
		while(shapeIter.hasNext()){
			IFeedbackNode shapeNode = shapeIter.next();
			if(shapeNode.getBounds().intersects(updateBound)){
				IFigureRenderingController controller = shapeNode.getFigureController();
				FigureRenderer drawer = new FigureRenderer(controller.getFigureDefinition());
				drawer.drawFigure(graphicsEngine);
			}
		}
		g2d.setComposite(original);
	}
	
	private void paintLinks(Graphics2D g2d, Envelope updateBound){
		Iterator<IFeedbackLink> linkIter = this.feedbackModel.linkIterator();
		final Composite original = g2d.getComposite();
		final AlphaComposite alpha = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f);
		g2d.setComposite(alpha);
		while(linkIter.hasNext()){
			IFeedbackLink feedbackLink = linkIter.next();
			if(feedbackLink.getLinkDefinition().getBounds().intersects(updateBound)){
				LinkDrawer drawer = new LinkDrawer(feedbackLink.getLinkDefinition()); 
				drawer.paint(g2d);
			}
		}
		g2d.setComposite(original);
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
