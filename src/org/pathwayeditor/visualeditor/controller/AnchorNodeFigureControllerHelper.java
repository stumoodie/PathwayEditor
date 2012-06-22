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

import org.pathwayeditor.businessobjects.drawingprimitives.IAnchorNodeAttribute;
import org.pathwayeditor.figure.rendering.FigureRenderingController;
import org.pathwayeditor.figure.rendering.IFigureRenderingController;
import org.pathwayeditor.visualeditor.feedback.FigureCompilationCache;

public class AnchorNodeFigureControllerHelper implements IFigureControllerHelper {
//	private final Logger logger = Logger.getLogger(this.getClass());
	private final IAnchorNodeAttribute attribute;
	private IFigureRenderingController figureRenderingController; 

	public AnchorNodeFigureControllerHelper(IAnchorNodeAttribute nodeAttribute){
		this.attribute = nodeAttribute; 
	}

	
	/* (non-Javadoc)
	 * @see org.pathwayeditor.visualeditor.controller.IFigureControllerFactory#createFigureController(org.pathwayeditor.businessobjects.drawingprimitives.IShapeAttribute)
	 */
	@Override
	public void createFigureController(){
		figureRenderingController = new FigureRenderingController(FigureCompilationCache.getInstance().lookup(attribute.getShapeDefinition()));
		figureRenderingController.setEnvelope(attribute.getBounds());
		figureRenderingController.setFillColour(attribute.getFillColour());
		figureRenderingController.setLineColour(attribute.getLineColour());
		figureRenderingController.setLineStyle(attribute.getLineStyle());
		figureRenderingController.setLineWidth(attribute.getLineWidth());
		refreshBoundProperties();
		figureRenderingController.generateFigureDefinition();
	}
	
	@Override
	public IFigureRenderingController getFigureController(){
		return this.figureRenderingController;
	}
	
	@Override
	public void refreshBoundProperties() {
	}
	
	


	@Override
	public void refreshGraphicalAttributes() {
		this.figureRenderingController.generateFigureDefinition();
	}


	@Override
	public void refreshAll() {
		this.refreshBoundProperties();
	}
}
