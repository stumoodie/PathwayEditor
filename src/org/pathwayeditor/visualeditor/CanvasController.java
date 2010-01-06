package org.pathwayeditor.visualeditor;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Iterator;
import java.util.SortedSet;

import org.apache.log4j.Logger;
import org.pathwayeditor.businessobjects.drawingprimitives.ICanvas;
import org.pathwayeditor.businessobjects.drawingprimitives.IDrawingElement;
import org.pathwayeditor.businessobjects.drawingprimitives.IDrawingElementSelection;
import org.pathwayeditor.businessobjects.drawingprimitives.IDrawingNode;
import org.pathwayeditor.businessobjects.drawingprimitives.ISelectionFactory;
import org.pathwayeditor.figure.geometry.Envelope;
import org.pathwayeditor.figure.geometry.INodeIntersectionCalculator;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.figure.geometry.ShapeIntersectionCalculator;
import org.pathwayeditor.graphicsengine.ShapePane;
import org.pathwayeditor.visualeditor.selection.ISelectionRecord;

public class CanvasController implements ICanvasController {
	private enum DragStatus { STARTED, FINISHED };

	private final Logger logger = Logger.getLogger(this.getClass());
	private final ShapePane shapePane;
	private final ISelectionRecord selectionManager;
	private DragStatus dragStatus = DragStatus.FINISHED;
	private final INodeIntersectionCalculator intCalc;

	public CanvasController(ShapePane pane, ICanvas canvas, ISelectionRecord selectionRecord){
		this.selectionManager = selectionRecord;
		this.shapePane = pane;
        intCalc = new ShapeIntersectionCalculator(canvas.getModel());
	}
	
	private IDrawingNode findDrawingNodeAt(Point location) {
		SortedSet<IDrawingNode> hits = intCalc.findNodesAt(new Point(location.getX(), location.getY()));
		logger.info("Found hit at: " + hits.first());
		return hits.first();
	}

	public void initialise(){
        this.shapePane.addMouseListener(new MouseListener(){

			public void mouseClicked(MouseEvent e) {
				if(e.getButton() == MouseEvent.BUTTON1){
					if(!e.isShiftDown() && !e.isAltDown()){
						Point location = new Point(e.getPoint().getX(), e.getPoint().getY());
						IDrawingNode nodeController = findDrawingNodeAt(location);
						if(nodeController != null){
							selectionManager.setPrimarySelection(nodeController);
						}
						else{
							selectionManager.clear();
						}
					}
					else if(e.isShiftDown() && !e.isAltDown()){
						Point location = new Point(e.getPoint().getX(), e.getPoint().getY());
						IDrawingNode nodeController = findDrawingNodeAt(location);
						if(nodeController != null){
							selectionManager.addSecondarySelection(nodeController);
						}
					}
				}
			}

			public void mouseEntered(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}

			public void mouseExited(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}

			public void mousePressed(MouseEvent e) {
			}

			public void mouseReleased(MouseEvent e) {
				if(dragStatus == DragStatus.STARTED){
					dragStatus = DragStatus.FINISHED;
				}
			}
        	
        });
        this.shapePane.addMouseMotionListener(new MouseMotionListener(){
        	private Point startPosition;
        	private Envelope initialShapePosition;

			public void mouseDragged(MouseEvent e) {
				if(e.getButton() == MouseEvent.BUTTON1){
					Point location = new Point(e.getPoint().getX(), e.getPoint().getY());
					if(dragStatus == DragStatus.FINISHED){
						IDrawingNode nodeController = findDrawingNodeAt(location);
						if(selectionManager.isNodeSelected(nodeController)){
							dragStatus = DragStatus.STARTED;
							logger.info("Starting dragging on: " + nodeController);
							startPosition = location;
							initialShapePosition = ((IDrawingNode)selectionManager.getPrimarySelection()).getAttribute().getBounds();
						}
					}
					else {
						Point delta = startPosition.difference(location);
						moveSelection(initialShapePosition, delta);
//							Point newLocation = initialShapePosition.getOrigin().translate(delta);
//							draggedNode.getAttribute().setBounds(initialShapePosition.changeOrigin(newLocation));
//							logger.info("Dragged shape to location: " + draggedNode.getAttribute().getBounds().getOrigin());
					}
				}
			}

			public void mouseMoved(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
        	
        });
	}
	
	private void moveSelection(Envelope initialShapePosition, Point delta) {
		ISelectionFactory selectionFactory = this.selectionManager.getPrimarySelection().getModel().newSelectionFactory();
		Iterator<IDrawingElement> iter = this.selectionManager.selectionIterator();
		while(iter.hasNext()){
			selectionFactory.addDrawingNode((IDrawingNode)iter.next());
		}
		IDrawingElementSelection selection = selectionFactory.createEdgeExcludedSelection();
		Iterator<IDrawingNode> moveNodeIterator = selection.drawingNodeIterator();
		while(moveNodeIterator.hasNext()){
			IDrawingNode draggedNode = moveNodeIterator.next();
			Point newLocation = initialShapePosition.getOrigin().translate(delta);
			draggedNode.getAttribute().setBounds(initialShapePosition.changeOrigin(newLocation));
			logger.trace("Dragged shape to location: " + draggedNode.getAttribute().getBounds().getOrigin());
		}
	}

}
