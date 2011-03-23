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

	void showPopup(JPopupMenu popup, int x, int y);
	
}