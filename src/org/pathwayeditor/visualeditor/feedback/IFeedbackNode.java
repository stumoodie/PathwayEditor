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

import java.util.List;

import org.pathwayeditor.businessobjects.drawingprimitives.attributes.LineStyle;
import org.pathwayeditor.businessobjects.drawingprimitives.attributes.RGB;
import org.pathwayeditor.figure.geometry.Dimension;
import org.pathwayeditor.figure.geometry.Envelope;
import org.pathwayeditor.figure.geometry.IConvexHull;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.figure.rendering.IFigureRenderingController;

public interface IFeedbackNode extends IFeedbackElement {

	/**
	 * Returns the bounds of the drawn node, which may be different from that of the underlying model.
	 * @return the envelope of the drawn node.
	 */
	Envelope getBounds();
	
	/**
	 * Returns the hull of the drawn node primitive. 
	 * @return the hull of the drawn node primitive.
	 */
	IConvexHull getConvexHull();
	
	/**
	 * Get the figure controller for this figure
	 * @return
	 */
	IFigureRenderingController getFigureController();

	void resizePrimitive(Point originDelta, Dimension sizeDelta);

	void addFeedbackNodeListener(IFeedbackNodeListener feedbackNodeListener);

	void removeFeedbackNodeListener(IFeedbackNodeListener feedbackNodeListener);

	List<IFeedbackNodeListener> getFeedbackNodeListeners();

	void setFillColour(RGB blue);

	void setLineColour(RGB red);

	void setLineStyle(LineStyle solid);

	void setLineWidth(double d);

	void setLocation(Point newPosition);
}
