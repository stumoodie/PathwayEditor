package org.pathwayeditor.visualeditor;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.JFrame;

import org.pathwayeditor.businessobjects.drawingprimitives.ICanvas;
import org.pathwayeditor.businessobjects.exchange.FileXmlCanvasPersistenceManager;
import org.pathwayeditor.businessobjects.exchange.IXmlPersistenceManager;
import org.pathwayeditor.businessobjects.management.INotationSubsystemPool;
import org.pathwayeditor.graphicsengine.ShapePane;
import org.pathwayeditor.visualeditor.controller.CanvasController;
import org.pathwayeditor.visualeditor.controller.ICanvasController;
import org.pathwayeditor.visualeditor.controller.IViewModel;
import org.pathwayeditor.visualeditor.controller.ViewModel;
import org.pathwayeditor.visualeditor.selection.ISelectionRecord;
import org.pathwayeditor.visualeditor.selection.SelectionRecord;

public class VisualEditor {
//	private final Logger logger = Logger.getLogger(this.getClass());
	
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
	private ICanvasController canvasController;
	
	public VisualEditor(ICanvas boCanvas){
		this.selectionRecord = new SelectionRecord();
		frame = new JFrame("NotationInspector");
		viewModel = new ViewModel(boCanvas.getModel());
		this.shapePane = new ShapePane(viewModel, this.selectionRecord);
		this.shapePane.setSize(new java.awt.Dimension((int)Math.round(boCanvas.getCanvasSize().getWidth()),
								(int)Math.round(boCanvas.getCanvasSize().getHeight())));
		frame.getContentPane().add(this.shapePane);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.canvasController = new CanvasController(this.shapePane);
        initialise();
		
        frame.pack();
        frame.setVisible(true);
	}
	
	private void initialise(){
//		initialiseViewToModelBindings();
		this.canvasController.initialise();
	}
	
	
//	private void initialiseViewToModelBindings() {
//        Iterator<IDrawingNode> drawingNodeIter = this.boCanvas.getModel().drawingNodeIterator();
//        while(drawingNodeIter.hasNext()){
//        	IDrawingNode node = drawingNodeIter.next();
//        	node.getAttribute().addChangeListener(new ICanvasAttributePropertyChangeListener(){
//				public void propertyChange(ICanvasAttributePropertyChangeEvent e) {
//					shapePane.repaint();
//				}
//        	});
//        }
//        this.selectionRecord.addSelectionChangeListener(new ISelectionChangeListener(){
//
//			public void selectionChanged(ISelectionChangeEvent event) {
//				shapePane.repaint();
//			}
//        	
//        });
//	}

	
	public void draw(){
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
					insp.draw();
				}
			});
		}
		catch(IOException ex){
			System.err.println("Error opening file!");
			System.err.println();
		}
	}
}
