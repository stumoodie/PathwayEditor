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

import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Iterator;

import javax.swing.JPopupMenu;

import org.pathwayeditor.figure.geometry.Envelope;

public interface IShapePane {

	void updateView(Envelope updateBounds);
	
	void updateView();
	
	void addLayer(IShapePaneLayer layer);
	
	void removeLayer(IShapePaneLayer layer);
	
	Iterator<IShapePaneLayer> layerIterator();

	<T extends IShapePaneLayer> T getLayer(LayerType layerType);

	void setPaneBounds(Envelope paneBounds);
	
	Envelope getPaneBounds();
	
	void addKeyListener(KeyListener keyListener);

	void addMouseListener(MouseListener mouseSelectionListener);

	void addMouseMotionListener(MouseMotionListener mouseMotionListener);

//	AffineTransform getLastUsedTransform();

	void removeMouseMotionListener(MouseMotionListener mouseMotionListener);

	void removeKeyListener(KeyListener keyListener);

	void removeMouseListener(MouseListener mouseSelectionListener);

	void showPopup(JPopupMenu popup, double x, double y);
	
}