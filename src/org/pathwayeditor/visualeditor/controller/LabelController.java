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

import org.pathwayeditor.businessobjects.drawingprimitives.ICanvasElementAttribute;
import org.pathwayeditor.businessobjects.drawingprimitives.ILabelNode;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.ICanvasAttributeChangeListener;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.ICanvasAttributePropertyChangeEvent;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.ICanvasAttributeResizedEvent;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.ICanvasAttributeTranslationEvent;

public class LabelController extends CommonLabelController implements ILabelController {
	private final ICanvasElementAttribute parentAttribute;
//	private final ICanvasAttributeChangeListener drawingNodePropertyChangeListener;
	private final ICanvasAttributeChangeListener parentDrawingNodePropertyChangeListener;
	
	public LabelController(IViewControllerModel viewModel, final ILabelNode node, int index) {
		super(viewModel, node, index);
		this.parentAttribute = (ICanvasElementAttribute)node.getGraphElement().getParent().getAttribute();
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
	}


	@Override
	public void inactivateOverride() {
		parentAttribute.removeChangeListener(parentDrawingNodePropertyChangeListener);
	}


	@Override
	public void activateOverride() {
		parentAttribute.addChangeListener(parentDrawingNodePropertyChangeListener);
	}

}
