package org.pathwayeditor.visualeditor.editingview;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JPanel;

import org.apache.log4j.Logger;
import org.pathwayeditor.figure.geometry.Envelope;

public class ShapePane extends JPanel implements IShapePane {
	private final Logger logger = Logger.getLogger(this.getClass());
	private static final long serialVersionUID = -7580080598416351849L;

	private final double PANE_BORDER = 20.0;
	private AffineTransform lastTransform;
	private Envelope canvasBounds;
	private final List<IShapePaneLayer> layers;

	public ShapePane(){
		super();
		this.layers = new LinkedList<IShapePaneLayer>();
		this.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent e) {
			}
			
			@Override
			public void mousePressed(MouseEvent e) {
			}
			
			@Override
			public void mouseExited(MouseEvent e) {
			}
			
			@Override
			public void mouseEntered(MouseEvent e) {
			}
			
			@Override
			public void mouseClicked(MouseEvent e) {
				requestFocusInWindow();
			}
		});
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
		g2d.setColor(Color.ORANGE);
		Rectangle r = g2d.getClipBounds();
		r.setRect(r.getX()+1.0, r.getY()+1.0, r.getWidth()-2.0, r.getHeight()-2.0);
		g2d.draw(r);
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
//		for(IShapePaneLayer layer : this.layers){
//			layer.setAllObjectsToUpdate();
//		}
		repaint();
		if(logger.isTraceEnabled()){
			logger.trace("Pane is focusable? = " + this.isFocusable());
			logger.trace("Pane has focus? = " + this.hasFocus());
		}
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

	public Rectangle getAdjustedBounds(Envelope updateBounds){
		AffineTransform paneTransform = this.getLastUsedTransform();
		Rectangle bounds = new Rectangle();
		if(paneTransform == null){
			bounds.setRect(updateBounds.getOrigin().getX(), updateBounds.getOrigin().getY(), updateBounds.getDimension().getWidth(), updateBounds.getDimension().getHeight());
		}
		else{
			double originalMouseX = updateBounds.getOrigin().getX();
			double originalMouseY = updateBounds.getOrigin().getY();
			bounds.setRect((originalMouseX*paneTransform.getScaleX()) + paneTransform.getTranslateX(), (originalMouseY*paneTransform.getScaleY()) + paneTransform.getTranslateY(),
					updateBounds.getDimension().getWidth()*paneTransform.getScaleX(), updateBounds.getDimension().getHeight()*paneTransform.getScaleY());
		}
		return bounds;  
	}

	@Override
	public void updateView(Envelope updateBounds) {
		Dimension prefSize = new Dimension();
		prefSize.setSize(canvasBounds.getDimension().getWidth()+2*PANE_BORDER, canvasBounds.getDimension().getHeight() + 2*PANE_BORDER);
		this.setPreferredSize(prefSize);
//		for(IShapePaneLayer layer : this.layers){
//			layer.setObjectsToUpdate(updateBounds);
////			layer.setAllObjectsToUpdate();
//		}
		Rectangle bounds = getAdjustedBounds(updateBounds);
		if(logger.isTraceEnabled()){
			logger.trace("Update requested. Model bounds=" + updateBounds + ", screen bounds=" + bounds);
		}
		repaint(bounds);
		if(logger.isTraceEnabled()){
			logger.trace("Pane is focusable? = " + this.isFocusable());
			logger.trace("Pane has focus? = " + this.hasFocus());
		}
	}
}
