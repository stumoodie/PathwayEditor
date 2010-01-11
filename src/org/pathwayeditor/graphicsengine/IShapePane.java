package org.pathwayeditor.graphicsengine;

import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import org.pathwayeditor.visualeditor.controller.IViewControllerStore;
import org.pathwayeditor.visualeditor.selection.ISelectionRecord;

public interface IShapePane {

	void repaint();
	
	IViewControllerStore getViewModel();
	
	ISelectionRecord getSelectionRecord();

	void addKeyListener(KeyListener keyListener);

	void addMouseListener(MouseListener mouseSelectionListener);

	void addMouseMotionListener(MouseMotionListener mouseMotionListener);
	
}