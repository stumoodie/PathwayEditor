package org.pathwayeditor.visualeditor.editingview;

import java.awt.Graphics2D;

public interface IShapePaneLayer {

	void paint(Graphics2D g2d);

	LayerType getLayerType();

//	void setObjectsToUpdate(Envelope updateBound);
//
//	void setAllObjectsToUpdate();
	
}
