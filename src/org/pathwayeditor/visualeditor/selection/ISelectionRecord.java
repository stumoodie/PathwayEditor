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
package org.pathwayeditor.visualeditor.selection;

import java.util.Iterator;
import java.util.List;

import org.pathwayeditor.figure.geometry.Envelope;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.visualeditor.controller.IDrawingElementController;
import org.pathwayeditor.visualeditor.controller.ILinkController;
import org.pathwayeditor.visualeditor.controller.INodeController;

/**
 * ISelectionRecord is an interface that defines how selections are managed in the graphical editor.
 * The selection will always have a primary selection. If the primary selection if not a shape or label node
 * or link then it is the root node. When a selection record is cleared then the root node becomes the primary
 * selection.   
 * 
 * @author smoodie
 *
 */
public interface ISelectionRecord {

	ISelectionHandle findSelectionModelAt(Point point);
	
	void setPrimarySelection(IDrawingElementController drawingElement);
	
	void addSecondarySelection(IDrawingElementController drawingElement);
	
	ISelection getPrimarySelection();
	
	Iterator<ISelection> secondarySelectionIterator();
	
	Iterator<ISelection> selectionIterator();
	
	Iterator<INodeSelection> selectedNodeIterator();
	
	Iterator<ILinkSelection> selectedLinkIterator();
	
	int numSelected();
	
	void clear();

	void addSelectionChangeListener(ISelectionChangeListener listener);
	
	void removeSelectionChangeListener(ISelectionChangeListener listener);
	
	List<ISelectionChangeListener> getSelectionChangeListeners();

	boolean isNodeSelected(IDrawingElementController testElement);

	ISubgraphSelection getSubgraphSelection();

	ISubgraphSelection getEdgeIncludedSelection();

	ILinkSelection getUniqueLinkSelection();

	ILinkSelection getLinkSelection(ILinkController next);

	INodeSelection getNodeSelection(INodeController next);

	ISelection getSelection(IDrawingElementController next);

	boolean containsSelection(IDrawingElementController controller);

	void restoreSelection();

	Envelope getTotalSelectionBounds();

	void selectAll();
}
