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
import org.pathwayeditor.figure.geometry.Dimension;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.visualeditor.behaviour.IEditingOperation;
import org.pathwayeditor.visualeditor.behaviour.IMouseBehaviourController;
import org.pathwayeditor.visualeditor.behaviour.IResizeOperation;
import org.pathwayeditor.visualeditor.behaviour.MouseBehaviourController;
import org.pathwayeditor.visualeditor.commands.CommandStack;
import org.pathwayeditor.visualeditor.commands.CompoundCommand;
import org.pathwayeditor.visualeditor.commands.ICommand;
import org.pathwayeditor.visualeditor.commands.ICommandStack;
import org.pathwayeditor.visualeditor.commands.ICompoundCommand;
import org.pathwayeditor.visualeditor.commands.MoveNodeCommand;
import org.pathwayeditor.visualeditor.commands.ResizeNodeCommand;
import org.pathwayeditor.visualeditor.controller.INodeController;
import org.pathwayeditor.visualeditor.controller.IViewControllerStore;
import org.pathwayeditor.visualeditor.controller.ViewControllerStore;
import org.pathwayeditor.visualeditor.selection.ISelection;
import org.pathwayeditor.visualeditor.selection.ISelectionChangeEvent;
import org.pathwayeditor.visualeditor.selection.ISelectionChangeListener;
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
	private IViewControllerStore viewModel;
	private ISelectionRecord selectionRecord;
	private ICommandStack commandStack;
	private IMouseBehaviourController editBehaviourController;

	private ISelectionChangeListener selectionChangeListener;

//	private ICompoundCommand currentCommand;
	
	public VisualEditor(ICanvas boCanvas){
		this.selectionRecord = new SelectionRecord();
        this.commandStack = new CommandStack();
		frame = new JFrame("NotationInspector");
		viewModel = new ViewControllerStore(boCanvas.getModel());
		this.shapePane = new ShapePane(viewModel, this.selectionRecord);
		this.shapePane.setSize(new java.awt.Dimension((int)Math.round(boCanvas.getCanvasSize().getWidth()),
								(int)Math.round(boCanvas.getCanvasSize().getHeight())));
		frame.getContentPane().add(this.shapePane);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        IEditingOperation editOperation = new IEditingOperation(){

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

        };
        IResizeOperation resizeOperation = new IResizeOperation() {
			
			@Override
			public void resizeStarted() {
				logger.trace("Resize started");
			}
			
			@Override
			public void resizeFinished(Point originDelta, Dimension resizeDelta) {
				if(logger.isTraceEnabled()){
					logger.trace("Resize finished. originDelta=" + originDelta + ", dimDelta=" + resizeDelta);
				}
				createResizeCommand(originDelta, resizeDelta);
//				commandStack.execute(currentCommand);
				shapePane.repaint();
			}
			
			@Override
			public void resizeContinuing(Point originDelta, Dimension resizeDelta) {
				resizeSelection(originDelta, resizeDelta);
				shapePane.repaint();
			}
		};
        this.editBehaviourController = new MouseBehaviourController(shapePane, editOperation, resizeOperation);
        this.selectionChangeListener = new ISelectionChangeListener() {
			
			@Override
			public void selectionChanged(ISelectionChangeEvent event) {
				shapePane.repaint();
			}
		}; 
        frame.pack();
        frame.setVisible(true);
	}
	
	private void resizeSelection(Point originDelta, Dimension resizeDelta) {
		Iterator<ISelection> moveNodeIterator = this.selectionRecord.selectionIterator();
		while(moveNodeIterator.hasNext()){
			INodeController nodePrimitive = (INodeController)moveNodeIterator.next().getPrimitiveController();
			nodePrimitive.resizePrimitive(originDelta, resizeDelta);
			if(logger.isTraceEnabled()){
				logger.trace("Resizing shape to bounds: " + nodePrimitive.getBounds());
			}
		}
	}

	private void createResizeCommand(Point originDelta, Dimension resizeDelta) {
		Iterator<ISelection> moveNodeIterator = this.selectionRecord.selectionIterator();
		ICompoundCommand cmpCommand = new CompoundCommand();
		while(moveNodeIterator.hasNext()){
			INodeController nodePrimitive = (INodeController)moveNodeIterator.next().getPrimitiveController();
			nodePrimitive.resizePrimitive(originDelta, resizeDelta);
			ICommand cmd = new ResizeNodeCommand(nodePrimitive.getDrawingElement(), originDelta, resizeDelta);
			cmpCommand.addCommand(cmd);
			logger.trace("Dragged shape to location: " + nodePrimitive.getBounds().getOrigin());
		}
		this.commandStack.execute(cmpCommand);
	}

	private void createMoveCommand(Point delta){
		Iterator<ISelection> moveNodeIterator = this.selectionRecord.getTopNodeSelection();
		ICompoundCommand cmpCommand = new CompoundCommand();
		while(moveNodeIterator.hasNext()){
			INodeController nodePrimitive = (INodeController)moveNodeIterator.next().getPrimitiveController();
			nodePrimitive.translatePrimitive(delta);
			ICommand cmd = new MoveNodeCommand(nodePrimitive.getDrawingElement(), delta);
			cmpCommand.addCommand(cmd);
			logger.trace("Dragged shape to location: " + nodePrimitive.getBounds().getOrigin());
		}
		this.commandStack.execute(cmpCommand);
	}
	
	private void moveSelection(Point delta) {
		Iterator<ISelection> moveNodeIterator = this.selectionRecord.getTopNodeSelection();
		while(moveNodeIterator.hasNext()){
			INodeController nodePrimitive = (INodeController)moveNodeIterator.next().getPrimitiveController();
			nodePrimitive.translatePrimitive(delta);
			logger.trace("Dragged shape to location: " + nodePrimitive.getBounds().getOrigin());
		}
//		this.currentCommand = cmpCommand;
	}
	
	public void initialise(){
		this.editBehaviourController.initialise();
		this.viewModel.activate();
		this.selectionRecord.addSelectionChangeListener(selectionChangeListener);
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
