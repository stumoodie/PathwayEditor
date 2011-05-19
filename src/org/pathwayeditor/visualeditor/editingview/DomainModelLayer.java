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

import org.apache.log4j.Logger;
import org.pathwayeditor.visualeditor.controller.IDrawingElementController;
import org.pathwayeditor.visualeditor.controller.IViewControllerModel;

public class DomainModelLayer implements IDomainModelLayer {
	private final IViewControllerModel viewControllerStore;
	private final Logger logger = Logger.getLogger(this.getClass());
	
	
	public DomainModelLayer(IViewControllerModel viewControllerStore) {
		this.viewControllerStore = viewControllerStore;
	}

	/* (non-Javadoc)
	 * @see org.pathwayeditor.graphicsengine.ICanvasDrawer#getVierwControllerStore()
	 */
	@Override
	public IViewControllerModel getViewControllerStore(){
		return this.viewControllerStore;
	}
	
	/* (non-Javadoc)
	 * @see org.pathwayeditor.graphicsengine.ICanvasDrawer#paint(java.awt.Graphics2D)
	 */
	@Override
	public void paint(Graphics2D g2d){
		Iterator<IDrawingElementController> contIter = this.viewControllerStore.drawingPrimitiveIterator();
		while(contIter.hasNext()){
			IDrawingElementController controller = contIter.next();
			IMiniCanvas miniCanvas = controller.getMiniCanvas();
			if(logger.isTraceEnabled()){
				logger.trace("Painting node=" + controller.getDrawingElement() + " at bounds=" + miniCanvas.getBounds());
			}
			miniCanvas.paint(g2d);
		}
	}

	@Override
	public LayerType getLayerType() {
		return LayerType.DOMAIN;
	}

}
