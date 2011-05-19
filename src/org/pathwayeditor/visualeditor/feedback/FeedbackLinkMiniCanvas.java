package org.pathwayeditor.visualeditor.feedback;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics2D;

import org.pathwayeditor.figure.geometry.Envelope;
import org.pathwayeditor.visualeditor.editingview.IMiniCanvas;
import org.pathwayeditor.visualeditor.editingview.LinkDrawer;
import org.pathwayeditor.visualeditor.geometry.ILinkPointDefinition;

public class FeedbackLinkMiniCanvas implements IMiniCanvas {
	private ILinkPointDefinition linkDefinition;

	public FeedbackLinkMiniCanvas(ILinkPointDefinition linkDefinition) {
		this.linkDefinition = linkDefinition;
	}

	@Override
	public void paint(Graphics2D g2d) {
		final Composite original = g2d.getComposite();
		final AlphaComposite alpha = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f);
		g2d.setComposite(alpha);
		LinkDrawer drawer = new LinkDrawer(linkDefinition); 
		drawer.paint(g2d);
		g2d.setComposite(original);
	}

	@Override
	public Envelope getBounds() {
		return this.linkDefinition.getBounds();
	}

}
