package org.pathwayeditor.visualeditor.editingview;

import java.awt.Graphics2D;

import org.pathwayeditor.figure.geometry.Envelope;

public interface IMiniCanvas {

	void paint(Graphics2D g);
	
	Envelope getBounds();
	
}
