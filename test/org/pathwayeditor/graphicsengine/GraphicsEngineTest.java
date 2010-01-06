package org.pathwayeditor.graphicsengine;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.SortedSet;

import javax.swing.JFrame;

import org.apache.log4j.Logger;
import org.pathwayeditor.businessobjects.drawingprimitives.ICanvas;
import org.pathwayeditor.businessobjects.drawingprimitives.IDrawingNode;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.ICanvasAttributePropertyChangeEvent;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.ICanvasAttributePropertyChangeListener;
import org.pathwayeditor.businessobjects.exchange.FileXmlCanvasPersistenceManager;
import org.pathwayeditor.businessobjects.exchange.IXmlPersistenceManager;
import org.pathwayeditor.businessobjects.management.INotationSubsystemPool;
import org.pathwayeditor.businessobjects.notationsubsystem.INotationSubsystem;
import org.pathwayeditor.figure.figuredefn.FigureGeometryFactory;
import org.pathwayeditor.figure.figuredefn.IFigureGeometryFactory;
import org.pathwayeditor.figure.geometry.Envelope;
import org.pathwayeditor.figure.geometry.INodeIntersectionCalculator;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.figure.geometry.ShapeIntersectionCalculator;

public class GraphicsEngineTest {
	private enum DragStatus { STARTED, FINISHED };

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
	private INotationSubsystem notationSubsystem; 
	private final ICanvas boCanvas;
	private IFigureGeometryFactory geomFact;
	private DragStatus dragStatus = DragStatus.FINISHED;
	
	public GraphicsEngineTest(ICanvas boCanvas){
		frame = new JFrame("NotationInspector");
		this.boCanvas = boCanvas;
		this.notationSubsystem = boCanvas.getNotationSubsystem();
		geomFact = new FigureGeometryFactory(boCanvas.getModel());
		this.shapePane = new ShapePane(geomFact, boCanvas);
		this.shapePane.setSize(new java.awt.Dimension((int)Math.round(boCanvas.getCanvasSize().getWidth()),
								(int)Math.round(boCanvas.getCanvasSize().getHeight())));
		frame.getContentPane().add(this.shapePane);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        final INodeIntersectionCalculator intCalc = new ShapeIntersectionCalculator(boCanvas.getModel());
        this.shapePane.addMouseListener(new MouseListener(){

			public void mouseClicked(MouseEvent e) {
				if(e.getButton() == MouseEvent.BUTTON1){
					java.awt.Point location = e.getPoint();
					SortedSet<IDrawingNode> hits = intCalc.findNodesAt(new Point(location.getX(), location.getY()));
					logger.info("Found hit at: " + hits.first());
					if(hits.size() > 1){
						shapePane.selectDrawingNode(hits.first());
					}
					else{
						shapePane.clearSelection();
					}
					shapePane.repaint();
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
        	private IDrawingNode draggedNode = null;

			public void mouseDragged(MouseEvent e) {
				if(e.getButton() == MouseEvent.BUTTON1){
					Point location = new Point(e.getPoint().getX(), e.getPoint().getY());
					if(dragStatus == DragStatus.FINISHED){
						SortedSet<IDrawingNode> hits = intCalc.findNodesAt(location);
						dragStatus = DragStatus.STARTED;
						if(hits.size() > 1){
							logger.info("Starting dragging on: " + hits.first());
							startPosition = new Point(location.getX(), location.getY());
							draggedNode = hits.first();
							initialShapePosition = draggedNode.getAttribute().getBounds();
						}
						else{
							draggedNode = null;
						}
					}
					else if(draggedNode != null){
						Point delta = startPosition.difference(location);
						Point newLocation = initialShapePosition.getOrigin().translate(delta);
						draggedNode.getAttribute().setBounds(initialShapePosition.changeOrigin(newLocation));
						logger.info("Dragged shape to location: " + draggedNode.getAttribute().getBounds().getOrigin());
					}
				}
			}

			public void mouseMoved(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
        	
        });
        
        Iterator<IDrawingNode> drawingNodeIter = this.boCanvas.getModel().drawingNodeIterator();
        while(drawingNodeIter.hasNext()){
        	IDrawingNode node = drawingNodeIter.next();
        	node.getAttribute().addChangeListener(new ICanvasAttributePropertyChangeListener(){
				public void propertyChange(ICanvasAttributePropertyChangeEvent e) {
					shapePane.repaint();
				}
        	});
        }
        //Display the window.
        frame.pack();
        frame.setVisible(true);
	}
	
//	public void writeShapes() {
//		INotationSyntaxService syntaxService = this.notationSubsystem.getSyntaxService();
//		Iterator<IShapeObjectType> iter = syntaxService.shapeTypeIterator();
//		IShapeNodeFactory shapeNodeFact = this.boCanvas.getModel().getRootNode().getSubModel().shapeNodeFactory();
//		double x = X_START;
//		double y = Y_START;
//		double maxY = 0.0;
//		while(iter.hasNext()){
//			IShapeObjectType objectType = iter.next();
//			logger.debug("Drawing object type: " + objectType.getName());
//			shapeNodeFact.setObjectType(objectType);
//			IShapeNode newNode = shapeNodeFact.createShapeNode();
//			Dimension shapeSize = objectType.getDefaultAttributes().getSize();
//			IShapeAttribute controller = newNode.getAttribute();
//			Envelope bounds = new Envelope(new Point(x, y), shapeSize);
//			controller.setBounds(bounds);
//			logger.debug("Drawing at position: " + bounds);
//			controller.setFillColour(RGB.GREEN);
//			controller.setLineWidth(1);
//			x += Math.max(MIN_WIDTH, shapeSize.getWidth()) + SEP_WIDTH;
//			maxY = Math.max(maxY, shapeSize.getHeight());
//			if(x > MAX_WIDTH){
//				x = X_START;
//				y += maxY + SEP_HEIGHT;
//				maxY = 0.0;
//			}
//		}
//	}
//	
//	public void writeLinks(){
//		IShapeAttribute node1 = this.boCanvas.getShapeAttribute(2);
//		IShapeAttribute node2 = this.boCanvas.getShapeAttribute(3);
//		IShapeAttribute node3 = this.boCanvas.getShapeAttribute(4);
//		IFigureController node1Controller = this.geomFact.getFigureController(node1.getCurrentDrawingElement());
//		IFigureController node2Controller = this.geomFact.getFigureController(node2.getCurrentDrawingElement());;
//		IFigureController node3Controller = this.geomFact.getFigureController(node3.getCurrentDrawingElement());;
//		
//		IAnchorLocator node1AnchorLocator = node1Controller.getAnchorLocatorFactory().createAnchorLocator();
//		node1AnchorLocator.setOtherEndPoint(node2Controller.getConvexHull().getCentre());
//		Point link1SrcPosn = node1AnchorLocator.calcAnchorPosition();
//		
//		IAnchorLocator node2AnchorLocator = node2Controller.getAnchorLocatorFactory().createAnchorLocator();
//		node2AnchorLocator.setOtherEndPoint(node1Controller.getConvexHull().getCentre());
//		Point link1TgtPosn = node2AnchorLocator.calcAnchorPosition();
//
//		ILinkEdgeFactory linkEdgeFactory = this.boCanvas.getModel().linkEdgeFactory();
//		ILinkObjectType linkObjectType = this.notationSubsystem.getSyntaxService().getLinkObjectType(StubLinkAObjectType.UNIQUE_ID);  
//		linkEdgeFactory.setObjectType(linkObjectType);
//		linkEdgeFactory.setShapeNodePair(node1.getCurrentDrawingElement(), node2.getCurrentDrawingElement());
//		ILinkEdge newEdge = linkEdgeFactory.createLinkEdge();
//		newEdge.getAttribute().getSourceTerminus().setLocation(link1SrcPosn);
//		newEdge.getAttribute().getTargetTerminus().setLocation(link1TgtPosn);
//
//		
//		linkEdgeFactory.setShapeNodePair(node1.getCurrentDrawingElement(), node2.getCurrentDrawingElement());
//		ILinkEdge edge2 = linkEdgeFactory.createLinkEdge();
//		IBendPoint link2Bp = edge2.getAttribute().createNewBendPoint(new Point(180, 80));
//		node2AnchorLocator.setOtherEndPoint(link2Bp.getLocation());
//		Point link2SrcPosn = node2AnchorLocator.calcAnchorPosition();
//		IAnchorLocator node3AnchorLocator = node3Controller.getAnchorLocatorFactory().createAnchorLocator();
//		node3AnchorLocator.setOtherEndPoint(link2Bp.getLocation());
//		Point link2TgtPosn = node3AnchorLocator.calcAnchorPosition();
//		edge2.getAttribute().getSourceTerminus().setLocation(link2SrcPosn);
//		edge2.getAttribute().getTargetTerminus().setLocation(link2TgtPosn);
//	}
	
	
	public void draw(){
		this.shapePane.repaint();
	}
	
	public static final void main(String argv[]){
		try{
			//		NonPersistentCanvasFactory.getInstance().setNotationSubsystem(new StubNotationSubSystem());
			//		NonPersistentCanvasFactory.getInstance().setCanvasName("Test Canvas");
			//		ICanvas boCanvas = NonPersistentCanvasFactory.getInstance().createNewCanvas();
			INotationSubsystemPool subsystemPool = new NotationSubsystemPool();
			IXmlPersistenceManager canvasPersistenceManager = new FileXmlCanvasPersistenceManager(subsystemPool);
		InputStream in = new FileInputStream(TEST_FILE);
		canvasPersistenceManager.readCanvasFromStream(in);
		in.close();
		final GraphicsEngineTest insp = new GraphicsEngineTest(canvasPersistenceManager.getCurrentCanvas());
//		insp.writeShapes();
//		insp.writeLinks();
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
