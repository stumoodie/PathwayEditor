package org.pathwayeditor.visualeditor.editingview;

import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.util.Iterator;

import org.pathwayeditor.figure.geometry.Envelope;

public interface IShapePane {

	void updateView();
	
	void addLayer(IShapePaneLayer layer);
	
	void removeLayer(IShapePaneLayer layer);
	
	Iterator<IShapePaneLayer> layerIterator();

	<T extends IShapePaneLayer> T getLayer(LayerType layerType);

	void setPaneBounds(Envelope paneBounds);
	
	Envelope getPaneBounds();
	
//	IViewControllerStore getViewModel();
//	
//	ISelectionRecord getSelectionRecord();
//	
//	IFeedbackModel getFeedbackModel(); 

	void addKeyListener(KeyListener keyListener);

	void addMouseListener(MouseListener mouseSelectionListener);

	void addMouseMotionListener(MouseMotionListener mouseMotionListener);

	AffineTransform getLastUsedTransform();

	void removeMouseMotionListener(MouseMotionListener mouseMotionListener);

	void removeKeyListener(KeyListener keyListener);

	void removeMouseListener(MouseListener mouseSelectionListener);
	
}