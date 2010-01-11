package org.pathwayeditor.visualeditor;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import javax.swing.JFrame;

import org.apache.log4j.Logger;
import org.pathwayeditor.businessobjects.drawingprimitives.ICanvas;
import org.pathwayeditor.businessobjects.exchange.FileXmlCanvasPersistenceManager;
import org.pathwayeditor.businessobjects.exchange.IXmlPersistenceManager;
import org.pathwayeditor.businessobjects.management.INotationSubsystemPool;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.graphicsengine.ShapePane;
import org.pathwayeditor.visualeditor.commands.CommandStack;
import org.pathwayeditor.visualeditor.commands.CompoundCommand;
import org.pathwayeditor.visualeditor.commands.ICommand;
import org.pathwayeditor.visualeditor.commands.ICommandStack;
import org.pathwayeditor.visualeditor.commands.ICompoundCommand;
import org.pathwayeditor.visualeditor.commands.MoveNodeCommand;
import org.pathwayeditor.visualeditor.controller.IEditingOperation;
import org.pathwayeditor.visualeditor.controller.IMouseBehaviourController;
import org.pathwayeditor.visualeditor.controller.INodePrimitive;
import org.pathwayeditor.visualeditor.controller.IViewModel;
import org.pathwayeditor.visualeditor.controller.MouseBehaviourController;
import org.pathwayeditor.visualeditor.controller.ViewModel;
import org.pathwayeditor.visualeditor.selection.ISelectionRecord;
import org.pathwayeditor.visualeditor.selection.SelectionRecord;

public class VisualEditor {
	private final Logger logger = Logger.getLogger(this.getClass());
	
//	private static final double X_START = 10.0;
//	private static final double Y_START = 10.0;
//	private static final double SEP_WIDTH = 10.0;
//	private static final double MAX_WIDTH = 400.0;
//	private static final double SEP_HEIGHT = 40.0;
//	private static final double MIN_WIDTH = 100.0;

	private static final String TEST_FILE = "test/org/pathwayeditor/graphicsengine/za.pwe";
	private final JFrame frame;
	private final ShapePane shapePane;
	private IViewModel viewModel;
	private ISelectionRecord selectionRecord;
	private ICommandStack commandStack;
	private IMouseBehaviourController editBehaviourController;

//	private ICompoundCommand currentCommand;
	
	public VisualEditor(ICanvas boCanvas){
		this.selectionRecord = new SelectionRecord();
        this.commandStack = new CommandStack();
		frame = new JFrame("NotationInspector");
		viewModel = new ViewModel(boCanvas.getModel());
		this.shapePane = new ShapePane(viewModel, this.selectionRecord);
		this.shapePane.setSize(new java.awt.Dimension((int)Math.round(boCanvas.getCanvasSize().getWidth()),
								(int)Math.round(boCanvas.getCanvasSize().getHeight())));
		frame.getContentPane().add(this.shapePane);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.editBehaviourController = new MouseBehaviourController(shapePane, new IEditingOperation(){

			@Override
			public void addSecondarySelection(INodePrimitive nodeController) {
				selectionRecord.addSecondarySelection(nodeController);
				shapePane.repaint();
			}

			@Override
			public void clearSelection() {
				selectionRecord.clear();
				shapePane.repaint();
			}

			@Override
			public boolean isNodeSelected(INodePrimitive nodeController) {
				return selectionRecord.isNodeSelected(nodeController);
			}

			@Override
			public void moveFinished(Point delta) {
				if(logger.isTraceEnabled()){
					logger.trace("Move finished. Delta=" + delta);
				}
				createMoveCommand(delta);
//				commandStack.execute(currentCommand);
				shapePane.repaint();
			}

			@Override
			public void moveOngoing(Point delta) {
				if(logger.isTraceEnabled()){
					logger.trace("Ongoning move. Delta=" + delta);
				}
				moveSelection(delta);
				shapePane.repaint();
			}

			@Override
			public void moveStarted() {
				logger.trace("Move started.");
			}

			@Override
			public void nodePrimarySelection(INodePrimitive nodeController) {
				selectionRecord.setPrimarySelection(nodeController);
				shapePane.repaint();
			}
        	
        });		
        frame.pack();
        frame.setVisible(true);
	}
	
	private void createMoveCommand(Point delta){
//		ISelectionFactory selectionFactory = this.selectionRecord.getPrimarySelection().getDrawingElement().getModel().newSelectionFactory();
//		Iterator<INodePrimitive> iter = this.selectionRecord.selectedNodesIterator();
//		while(iter.hasNext()){
//			INodePrimitive nodePrimitive = iter.next();
//			selectionFactory.addDrawingNode(nodePrimitive.getDrawingElement());
//		}
//		IDrawingElementSelection selection = selectionFactory.createEdgeExcludedSelection();
		Iterator<INodePrimitive> moveNodeIterator = this.selectionRecord.getTopNodeSelection();
		ICompoundCommand cmpCommand = new CompoundCommand();
		while(moveNodeIterator.hasNext()){
			INodePrimitive nodePrimitive = moveNodeIterator.next();
			nodePrimitive.translatePrimitive(delta);
			ICommand cmd = new MoveNodeCommand(nodePrimitive.getDrawingElement(), nodePrimitive.getBounds().getOrigin());
			cmpCommand.addCommand(cmd);
			logger.trace("Dragged shape to location: " + nodePrimitive.getBounds().getOrigin());
		}
		this.commandStack.execute(cmpCommand);
	}
	
	private void moveSelection(Point delta) {
//		ISelectionFactory selectionFactory = this.selectionRecord.getPrimarySelection().getDrawingElement().getModel().newSelectionFactory();
//		Iterator<INodePrimitive> iter = this.selectionRecord.selectedNodesIterator();
//		while(iter.hasNext()){
//			INodePrimitive nodePrimitive = iter.next();
//			selectionFactory.addDrawingNode(nodePrimitive.getDrawingElement());
//		}
//		IDrawingElementSelection selection = selectionFactory.createEdgeExcludedSelection();
		Iterator<INodePrimitive> moveNodeIterator = this.selectionRecord.getTopNodeSelection();
//		ICompoundCommand cmpCommand = new CompoundCommand();
		while(moveNodeIterator.hasNext()){
			INodePrimitive nodePrimitive = moveNodeIterator.next();
			nodePrimitive.translatePrimitive(delta);
//			ICommand cmd = new MoveNodeCommand(draggedNode, nodePrimitive.getBounds().getOrigin());
//			cmpCommand.addCommand(cmd);
			logger.trace("Dragged shape to location: " + nodePrimitive.getBounds().getOrigin());
		}
//		this.currentCommand = cmpCommand;
	}
	
	public void initialise(){
		this.editBehaviourController.initialise();
		this.viewModel.activate();
		this.shapePane.repaint();
	}
	
	public static final void main(String argv[]){
		try{
			INotationSubsystemPool subsystemPool = new NotationSubsystemPool();
			IXmlPersistenceManager canvasPersistenceManager = new FileXmlCanvasPersistenceManager(subsystemPool);
			InputStream in = new FileInputStream(TEST_FILE);
			canvasPersistenceManager.readCanvasFromStream(in);
			in.close();
			final VisualEditor insp = new VisualEditor(canvasPersistenceManager.getCurrentCanvas());
			javax.swing.SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					insp.initialise();
				}
			});
		}
		catch(IOException ex){
			System.err.println("Error opening file!");
			System.err.println();
		}
	}
}
