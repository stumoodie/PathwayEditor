package org.pathwayeditor.graphicsengine;

import java.awt.Graphics2D;

import org.pathwayeditor.visualeditor.controller.IViewControllerStore;

public interface ICanvasDrawer {

	public abstract IViewControllerStore getViewControllerStore();

	public abstract void paint(Graphics2D g2d);

}