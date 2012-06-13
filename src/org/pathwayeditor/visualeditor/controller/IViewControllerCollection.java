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

import java.util.Iterator;

import org.pathwayeditor.businessobjects.drawingprimitives.IAnchorNodeAttribute;
import org.pathwayeditor.businessobjects.drawingprimitives.ICanvasElementAttribute;
import org.pathwayeditor.businessobjects.drawingprimitives.IDrawingElement;
import org.pathwayeditor.businessobjects.drawingprimitives.IDrawingNode;
import org.pathwayeditor.businessobjects.drawingprimitives.ILinkEdge;
import org.pathwayeditor.businessobjects.drawingprimitives.IShapeAttribute;
import org.pathwayeditor.businessobjects.drawingprimitives.ITypedDrawingNodeAttribute;

public interface IViewControllerCollection {

	Iterator<IDrawingElementController> drawingPrimitiveIterator();
	
	Iterator<IShapeController> shapeControllerIterator();
	
	Iterator<ILabelController> labelControllerIterator();
	
	Iterator<ILinkController> linkControllerIterator();

	Iterator<INodeController> nodeControllerIterator();

	IDrawingElementController getDrawingPrimitiveController(IDrawingElement testAttribute);
	
	IDrawingElementController findControllerByAttribute(ICanvasElementAttribute testAttribute);
	
	INodeController getNodeController(IDrawingNode testNode);
	
	ILinkController getLinkController(ILinkEdge attribute);

	IShapeController getShapeController(IShapeAttribute attribute);

	IAnchorNodeController getAnchorNodeController(IAnchorNodeAttribute attribute);
	
	IConnectingNodeController getConnectingNodeController(ITypedDrawingNodeAttribute att);

	boolean containsDrawingElement(IDrawingElement testPrimitive);
}
