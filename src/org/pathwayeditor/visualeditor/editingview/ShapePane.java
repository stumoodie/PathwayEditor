package org.pathwayeditor.visualeditor.editingview;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JPanel;

import org.pathwayeditor.figure.geometry.Envelope;

public class ShapePane extends JPanel implements IShapePane {
	private static final long serialVersionUID = -7580080598416351849L;

	private final double PANE_BORDER = 20.0;
	private AffineTransform lastTransform;
	private Envelope canvasBounds;
	private final List<IShapePaneLayer> layers;

	public ShapePane(){
		super();
		this.layers = new LinkedList<IShapePaneLayer>();
	}
	
	@Override
	public void paintComponent(Graphics g){
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D)g;
		AffineTransform originalTransform = g2d.getTransform();
		g2d.translate(-canvasBounds.getOrigin().getX()+PANE_BORDER, -canvasBounds.getOrigin().getY()+PANE_BORDER);
		this.lastTransform = g2d.getTransform();
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		for(IShapePaneLayer layer : this.layers){
			layer.paint(g2d);
		}
		g2d.setTransform(originalTransform);
	}

	@Override
	public AffineTransform getLastUsedTransform(){
		return this.lastTransform;
	}

	@Override
	public void updateView() {
		Dimension prefSize = new Dimension();
		prefSize.setSize(canvasBounds.getDimension().getWidth()+2*PANE_BORDER, canvasBounds.getDimension().getHeight() + 2*PANE_BORDER);
		this.setPreferredSize(prefSize);
		revalidate();
		repaint();
	}

	@Override
	public void addLayer(IShapePaneLayer layer) {
		this.layers.add(layer);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends IShapePaneLayer> T getLayer(LayerType layerType) {
		T retVal = null;
		for(IShapePaneLayer layer : this.layers){
			if(layer.getLayerType() == layerType){
				retVal = (T)layer;
				break;
			}
		}
		return retVal; 
	}

	@Override
	public Envelope getPaneBounds() {
		return this.canvasBounds;
	}

	@Override
	public Iterator<IShapePaneLayer> layerIterator() {
		return this.layers.iterator();
	}

	@Override
	public void removeLayer(IShapePaneLayer layer) {
		this.layers.remove(layer);
	}

	@Override
	public void setPaneBounds(Envelope paneBounds) {
		this.canvasBounds = paneBounds;
	}
}
