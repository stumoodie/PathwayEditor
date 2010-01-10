package org.pathwayeditor.visualeditor.controller;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Iterator;
import java.util.SortedSet;

import org.apache.log4j.Logger;
import org.pathwayeditor.businessobjects.drawingprimitives.IDrawingElementSelection;
import org.pathwayeditor.businessobjects.drawingprimitives.IDrawingNode;
import org.pathwayeditor.businessobjects.drawingprimitives.ISelectionFactory;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.graphicsengine.ShapePane;
import org.pathwayeditor.visualeditor.commands.CommandStack;
import org.pathwayeditor.visualeditor.commands.CompoundCommand;
import org.pathwayeditor.visualeditor.commands.ICommand;
import org.pathwayeditor.visualeditor.commands.ICommandStack;
import org.pathwayeditor.visualeditor.commands.ICompoundCommand;
import org.pathwayeditor.visualeditor.commands.MoveNodeCommand;
import org.pathwayeditor.visualeditor.geometry.INodeIntersectionCalculator;
import org.pathwayeditor.visualeditor.geometry.ShapeIntersectionCalculator;
import org.pathwayeditor.visualeditor.selection.ISelectionRecord;

public class CanvasController implements ICanvasController {
	private enum DragStatus { STARTED, FINISHED };

	private final Logger logger = Logger.getLogger(this.getClass());
	private final ShapePane shapePane;
	private final ISelectionRecord selectionManager;
	private DragStatus dragStatus = DragStatus.FINISHED;
	private DragStatus keyDragStatus = DragStatus.FINISHED;
	private final INodeIntersectionCalculator intCalc;
	private ICommand currentCommand;
	private ICommandStack commandStack;
	private final IViewModel viewModel;
	private final MouseListener mouseSelectionListener;
	private final MouseMotionListener mouseMotionListener;
	private final KeyListener keyListener;

	public CanvasController(ShapePane pane){
		this.selectionManager = pane.getSelectionRecord();
		this.shapePane = pane;
        intCalc = new ShapeIntersectionCalculator(pane.getViewModel());
        this.viewModel = pane.getViewModel();
        this.commandStack = new CommandStack();
        this.mouseSelectionListener = new MouseListener(){

			public void mouseClicked(MouseEvent e) {
				if(e.getButton() == MouseEvent.BUTTON1){
					if(!e.isShiftDown() && !e.isAltDown()){
						Point location = new Point(e.getPoint().getX(), e.getPoint().getY());
						INodePrimitive nodeController = findDrawingNodeAt(location);
						if(nodeController != null){
							selectionManager.setPrimarySelection(nodeController);
						}
						else{
							selectionManager.clear();
						}
						shapePane.repaint();
					}
					else if(e.isShiftDown() && !e.isAltDown()){
						Point location = new Point(e.getPoint().getX(), e.getPoint().getY());
						INodePrimitive nodeController = findDrawingNodeAt(location);
						if(nodeController != null){
							selectionManager.addSecondarySelection(nodeController);
						}
						shapePane.repaint();
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
					if(currentCommand != null){
						commandStack.execute(currentCommand);
					}
					viewModel.synchroniseWithDomainModel();
					shapePane.repaint();
				}
			}
        	
        };
        this.mouseMotionListener = new MouseMotionListener(){
        	private Point startPosition;

			public void mouseDragged(MouseEvent e) {
				if(e.getButton() == MouseEvent.BUTTON1){
					Point location = new Point(e.getPoint().getX(), e.getPoint().getY());
					if(dragStatus == DragStatus.FINISHED){
						INodePrimitive nodeController = findDrawingNodeAt(location);
						if(selectionManager.isNodeSelected(nodeController)){
							dragStatus = DragStatus.STARTED;
							logger.info("Starting dragging on: " + nodeController);
							startPosition = location;
						}
					}
					else {
						Point delta = startPosition.difference(location);
						moveSelection(delta);
						shapePane.repaint();
					}
				}
			}

			public void mouseMoved(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
        	
        };
        this.keyListener = new KeyListener(){
        	private Point lastDelta = new Point(0,0);

			@Override
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_RIGHT){
					if(keyDragStatus.equals(DragStatus.FINISHED)){
						keyDragStatus = DragStatus.STARTED;
						lastDelta = new Point(0,0);
						Point delta = lastDelta.translate(1, 0);
						moveSelection(delta);
						lastDelta = delta;
						shapePane.repaint();
					}
					else{
						Point delta = lastDelta.translate(1, 0);
						moveSelection(delta);
						lastDelta = delta;
						shapePane.repaint();
					}
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_RIGHT){
					if(dragStatus == DragStatus.STARTED){
						dragStatus = DragStatus.FINISHED;
						if(currentCommand != null){
							commandStack.execute(currentCommand);
						}
						viewModel.synchroniseWithDomainModel();
						shapePane.repaint();
					}
				}
			}

			@Override
			public void keyTyped(KeyEvent e) {
				// TODO Auto-generated method stub
				
			}
        	
        };
	}
	
	private INodePrimitive findDrawingNodeAt(Point location) {
		SortedSet<INodePrimitive> hits = intCalc.findNodesAt(new Point(location.getX(), location.getY()));
		INodePrimitive retVal = null;
		if(!hits.isEmpty()){
			retVal = hits.first();
			logger.info("Found hit at: " + retVal);
		}
		return retVal;
	}

	public void initialise(){
		this.shapePane.addKeyListener(this.keyListener);
        this.shapePane.addMouseListener(this.mouseSelectionListener);
        this.shapePane.addMouseMotionListener(this.mouseMotionListener);
        // now activate all the drawing primitives
        Iterator<IDrawingPrimitive> drawingPrimitiveIterator = this.viewModel.drawingPrimitiveIterator();
        while(drawingPrimitiveIterator.hasNext()){
        	drawingPrimitiveIterator.next().activate();
        }
	}
	
	private void moveSelection(Point delta) {
		ISelectionFactory selectionFactory = this.selectionManager.getPrimarySelection().getDrawingElement().getModel().newSelectionFactory();
		Iterator<INodePrimitive> iter = this.selectionManager.selectedNodesIterator();
		while(iter.hasNext()){
			INodePrimitive nodePrimitive = iter.next();
			selectionFactory.addDrawingNode(nodePrimitive.getDrawingElement());
//			nodePrimitive.translatePrimitive(delta);
		}
		IDrawingElementSelection selection = selectionFactory.createEdgeExcludedSelection();
		Iterator<IDrawingNode> moveNodeIterator = selection.topDrawingNodeIterator();
		ICompoundCommand cmpCommand = new CompoundCommand();
		while(moveNodeIterator.hasNext()){
			IDrawingNode draggedNode = moveNodeIterator.next();
			INodePrimitive nodePrimitive = this.viewModel.getNodePrimitive(draggedNode);
			nodePrimitive.translatePrimitive(delta);
			ICommand cmd = new MoveNodeCommand(draggedNode, nodePrimitive.getBounds().getOrigin());
			cmpCommand.addCommand(cmd);
			logger.trace("Dragged shape to location: " + nodePrimitive.getBounds().getOrigin());
		}
		this.currentCommand = cmpCommand;
	}

}
