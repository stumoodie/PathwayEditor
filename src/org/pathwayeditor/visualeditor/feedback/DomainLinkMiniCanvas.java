package org.pathwayeditor.visualeditor.feedback;

import java.awt.Graphics2D;

import org.pathwayeditor.figure.geometry.Envelope;
import org.pathwayeditor.visualeditor.editingview.IMiniCanvas;
import org.pathwayeditor.visualeditor.editingview.LinkDrawer;
import org.pathwayeditor.visualeditor.geometry.ILinkPointDefinition;

public class DomainLinkMiniCanvas implements IMiniCanvas {
	private ILinkPointDefinition linkDefinition;

	public DomainLinkMiniCanvas(ILinkPointDefinition linkDefinition) {
		this.linkDefinition = linkDefinition;
	}

	@Override
	public void paint(Graphics2D g2d) {
		LinkDrawer drawer = new LinkDrawer(linkDefinition); 
		drawer.paint(g2d);
	}

	@Override
	public Envelope getBounds() {
		return this.linkDefinition.getBounds();
	}

}
