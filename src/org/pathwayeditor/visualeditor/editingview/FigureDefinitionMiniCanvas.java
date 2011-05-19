package org.pathwayeditor.visualeditor.editingview;

import java.awt.Graphics2D;

import org.pathwayeditor.figure.geometry.Envelope;
import org.pathwayeditor.figure.rendering.FigureRenderer;
import org.pathwayeditor.figure.rendering.GraphicsInstructionList;
import org.pathwayeditor.figure.rendering.IGraphicsEngine;
import org.pathwayeditor.graphicsengine.Java2DGraphicsEngine;

public class FigureDefinitionMiniCanvas implements IMiniCanvas {
	private final GraphicsInstructionList graphicsDefn;
	private final Envelope bounds;
	
	public FigureDefinitionMiniCanvas(GraphicsInstructionList graphicsDefn, Envelope bounds){
		this.graphicsDefn = graphicsDefn;
		this.bounds = bounds;
	}
	
	
	@Override
	public void paint(Graphics2D g2d) {
		IGraphicsEngine graphicsEngine = new Java2DGraphicsEngine(g2d);
		FigureRenderer drawer = new FigureRenderer(this.graphicsDefn);
		drawer.drawFigure(graphicsEngine);
	}

	@Override
	public Envelope getBounds() {
		return this.bounds;
	}

}
