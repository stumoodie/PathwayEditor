package org.pathwayeditor.visualeditor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.apache.log4j.Logger;
import org.pathwayeditor.businessobjects.drawingprimitives.ICanvas;
import org.pathwayeditor.businessobjects.drawingprimitives.ILinkEdge;
import org.pathwayeditor.figure.geometry.Envelope;
import org.pathwayeditor.visualeditor.behaviour.IMouseBehaviourController;
import org.pathwayeditor.visualeditor.behaviour.MouseBehaviourController;
import org.pathwayeditor.visualeditor.commands.CommandStack;
import org.pathwayeditor.visualeditor.commands.ICommandStack;
import org.pathwayeditor.visualeditor.controller.IDrawingPrimitiveController;
import org.pathwayeditor.visualeditor.controller.IViewControllerStore;
import org.pathwayeditor.visualeditor.controller.ViewControllerStore;
import org.pathwayeditor.visualeditor.editingview.DomainModelLayer;
import org.pathwayeditor.visualeditor.editingview.FeedbackLayer;
import org.pathwayeditor.visualeditor.editingview.IShapePane;
import org.pathwayeditor.visualeditor.editingview.SelectionLayer;
import org.pathwayeditor.visualeditor.editingview.ShapePane;
import org.pathwayeditor.visualeditor.feedback.FeedbackModel;
import org.pathwayeditor.visualeditor.feedback.IFeedbackModel;
import org.pathwayeditor.visualeditor.geometry.EnvelopeBuilder;
import org.pathwayeditor.visualeditor.operations.OperationFactory;
import org.pathwayeditor.visualeditor.selection.ISelection;
import org.pathwayeditor.visualeditor.selection.ISelectionChangeEvent;
import org.pathwayeditor.visualeditor.selection.ISelectionChangeListener;
import org.pathwayeditor.visualeditor.selection.ISelectionRecord;
import org.pathwayeditor.visualeditor.selection.SelectionRecord;

public class PathwayEditor extends JPanel {
	private static final double REFRESH_EXPANSION_Y = 20.0;

	private static final double REFRESH_EXPANSION_X = REFRESH_EXPANSION_Y;

	private static final long serialVersionUID = 1L;

	private final Logger logger = Logger.getLogger(this.getClass());
	private IShapePane shapePane;
	private JScrollPane scrollPane;
	private PalettePanel palettePane;
	private IViewControllerStore viewModel;
	private ISelectionRecord selectionRecord;
	private ICommandStack commandStack;
	private IMouseBehaviourController editBehaviourController;
	private ISelectionChangeListener selectionChangeListener;
	private IFeedbackModel feedbackModel;
	private boolean isOpen = false;
	
	public PathwayEditor(){
		super();
		this.setLayout(new BorderLayout());
		this.setBorder(BorderFactory.createLineBorder(Color.BLACK));
	}
	
	private void reset(){
		this.commandStack = null;
		this.remove(this.scrollPane);
		this.scrollPane.remove((JComponent)this.shapePane);
		this.selectionRecord.removeSelectionChangeListener(selectionChangeListener);
		this.viewModel.deactivate();
		this.editBehaviourController.deactivate();
		this.shapePane = null;
		this.scrollPane = null;
		this.selectionRecord = null;
		this.viewModel = null;
		this.editBehaviourController = null;
		this.selectionChangeListener = null;
		this.feedbackModel = null;
	}
	
	public void close(){
		if(isOpen){
			reset();
		}
		isOpen = false;
	}
	
	private void setUpEditorViews(ICanvas canvas){
		this.selectionRecord = new SelectionRecord(viewModel);
		this.feedbackModel = new FeedbackModel(selectionRecord);
		this.shapePane = new ShapePane();
		this.shapePane.addLayer(new DomainModelLayer(viewModel));
		this.shapePane.addLayer(new SelectionLayer(selectionRecord));
		this.shapePane.addLayer(new FeedbackLayer(feedbackModel));
		this.shapePane.setPaneBounds(new Envelope(0, 0, 400.0, 400.0));
		scrollPane = new JScrollPane((ShapePane)this.shapePane, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setPreferredSize(this.getPreferredSize());
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);
		scrollPane.setFocusable(true);
		this.add(scrollPane, BorderLayout.CENTER);
		Envelope canvasBounds = viewModel.getCanvasBounds();
		((ShapePane)this.shapePane).setSize((int)Math.ceil(canvasBounds.getDimension().getWidth()), (int)Math.ceil(canvasBounds.getDimension().getHeight()));
        this.editBehaviourController = new MouseBehaviourController(shapePane, new OperationFactory(this.shapePane, this.feedbackModel, this.selectionRecord, viewModel, this.commandStack));
		this.palettePane = new PalettePanel(canvas.getNotationSubsystem(), editBehaviourController);
		this.add(palettePane, BorderLayout.LINE_START);
        this.selectionChangeListener = new ISelectionChangeListener() {
			
			@Override
			public void selectionChanged(ISelectionChangeEvent event) {
				EnvelopeBuilder builder = null;
				Iterator<ISelection> oldIter = event.oldSelectionIter();
				while(oldIter.hasNext()){
					ISelection seln = oldIter.next();
					if(logger.isTraceEnabled()){
						logger.trace("Union old selection drawnBounds=" + seln.getPrimitiveController().getDrawnBounds());
					}
					Envelope bounds = seln.getPrimitiveController().getDrawnBounds();
					if(builder == null){
						builder = new EnvelopeBuilder(bounds);
					}
					else{
						builder.union(bounds);
					}
				}
				if(builder != null){
					builder.expand(REFRESH_EXPANSION_X, REFRESH_EXPANSION_Y);
					Envelope refreshBounds = builder.getEnvelope();
					if(logger.isTraceEnabled()){
						logger.trace("Unselection refresh bounds: " + refreshBounds);
					}
					shapePane.updateView(refreshBounds);
				}
				else{
					logger.debug("No old selection bounds identified");
				}
				builder = null;
				Iterator<ISelection> newIter = event.newSelectionIter();
				while(newIter.hasNext()){
					ISelection seln = newIter.next();
					Envelope bounds = seln.getPrimitiveController().getDrawnBounds();
					if(logger.isTraceEnabled()){
						logger.trace("Union new selection drawnBounds=" + seln.getPrimitiveController().getDrawnBounds());
					}
					if(builder == null){
						builder = new EnvelopeBuilder(bounds);
					}
					else{
						builder.union(bounds);
					}
				}
				if(builder != null){
					builder.expand(REFRESH_EXPANSION_X, REFRESH_EXPANSION_Y);
					Envelope refreshBounds = builder.getEnvelope();
					if(logger.isTraceEnabled()){
						logger.trace("Selection refresh bounds: " + refreshBounds);
					}
					shapePane.updateView(refreshBounds);
				}
				else{
					logger.debug("No new selection bounds identified");
				}
			}
		};
		this.initialise();
		this.revalidate();
	}
	
	public void loadCanvas(ICanvas canvas){
		if(isOpen){
			reset();
		}
        this.commandStack = new CommandStack();
		this.viewModel = new ViewControllerStore(canvas.getModel());
		setUpEditorViews(canvas);
		
	}

	
	private void initialise(){
		this.editBehaviourController.activate();
		this.viewModel.activate();
		this.selectionRecord.addSelectionChangeListener(selectionChangeListener);
		this.shapePane.updateView();
		this.isOpen = true;
	}

	public void selectAndFocusOnElement(ILinkEdge linkEdge) {
		selectionRecord.clear();
		IDrawingPrimitiveController linkController = viewModel.getLinkController(linkEdge.getAttribute());
		selectionRecord.setPrimarySelection(linkController);
	}
}
