package org.pathwayeditor.visualeditor.behaviour;

import java.awt.event.MouseListener;

public interface IPopupMenuListener extends MouseListener {

	void activate();

	void deactivate();
	
	boolean isActive();
	
}
