package org.pathwayeditor.visualeditor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import org.apache.log4j.Logger;
import org.pathwayeditor.businessobjects.drawingprimitives.IRootAttribute;
import org.pathwayeditor.businessobjects.notationsubsystem.INotationSubsystem;
import org.pathwayeditor.figure.geometry.Envelope;
import org.pathwayeditor.visualeditor.behaviour.IMouseBehaviourController;
import org.pathwayeditor.visualeditor.behaviour.MouseBehaviourController;
import org.pathwayeditor.visualeditor.commands.CommandStack;
import org.pathwayeditor.visualeditor.commands.ICommandStack;
import org.pathwayeditor.visualeditor.controller.IDrawingElementController;
import org.pathwayeditor.visualeditor.controller.IViewControllerModel;
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

import uk.ac.ed.inf.graph.compound.ICompoundEdge;
import uk.ac.ed.inf.graph.compound.ICompoundGraph;

public class PathwayEditor extends JPanel {
	private static final double REFRESH_EXPANSION_Y = 20.0;

	private static final double REFRESH_EXPANSION_X = REFRESH_EXPANSION_Y;

	private static final long serialVersionUID = 1L;

	private final Logger logger = Logger.getLogger(this.getClass());
	private IShapePane shapePane;
	private JScrollPane scrollPane;
	private PalettePanel palettePane;
	private IViewControllerModel viewModel;
	private ISelectionRecord selectionRecord;
	private ICommandStack commandStack;
	private IMouseBehaviourController editBehaviourController;
	private ISelectionChangeListener selectionChangeListener;
	private IFeedbackModel feedbackModel;
	private boolean isOpen = false;

	private ILayoutCalculator layoutCalculator;

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
	
	private void setUpEditorViews(ICompoundGraph canvas){
		this.selectionRecord = new SelectionRecord(viewModel);
		this.feedbackModel = new FeedbackModel(selectionRecord);
		this.shapePane = new ShapePane();
		this.shapePane.addLayer(new DomainModelLayer(viewModel));
		this.shapePane.addLayer(new SelectionLayer(selectionRecord));
		this.shapePane.addLayer(new FeedbackLayer(feedbackModel));
		Envelope canvasBounds = this.viewModel.getCanvasBounds();
		this.shapePane.setPaneBounds(canvasBounds);
		scrollPane = new JScrollPane((ShapePane)this.shapePane, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);
		scrollPane.setFocusable(true);
		scrollPane.setWheelScrollingEnabled(true);
        this.editBehaviourController = new MouseBehaviourController(shapePane, new OperationFactory(this.shapePane, this.feedbackModel, this.selectionRecord, viewModel, this.commandStack));
        IRootAttribute rootAtt = (IRootAttribute)canvas.getRoot().getAttribute();
        INotationSubsystem notationSubsystem = rootAtt.getObjectType().getSyntaxService().getNotationSubsystem(); 
		this.palettePane = new PalettePanel(notationSubsystem, editBehaviourController);
		this.add(palettePane, BorderLayout.LINE_START);
		this.add(scrollPane, BorderLayout.CENTER);
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
		this.revalidate();
		this.initialise();
	}
	
	/**
	 * Sets the layout calculator to be used for auto-layout of the canvas.
	 * @param layoutCalculator the layout calculator 
	 */
	public void setLayoutCalculator(ILayoutCalculator layoutCalculator){
		this.layoutCalculator = layoutCalculator;
		
	}
	
	/**
	 * Carry out auto-layout on the canvas. Using the layout calculator set in {@link setLayoutCalculator}.
	 * @throws IllegalStateException if the layout calculator is not set
	 */
	public void layoutCanvas(){
		if(this.layoutCalculator != null){
			this.layoutCalculator.calculateLayout();
		}
		else{
			throw new IllegalStateException("No layout calculator was set");
		}
	}
	
	public void loadCanvas(ICompoundGraph canvas){
		if(isOpen){
			reset();
		}
        this.commandStack = new CommandStack();
		this.viewModel = new ViewControllerStore(canvas);
		setUpEditorViews(canvas);
		((ShapePane)this.shapePane).setPreferredSize(new Dimension(1800, 1800));
		((ShapePane)this.shapePane).revalidate();
	}

	
	private void initialise(){
		this.editBehaviourController.activate();
		this.viewModel.activate();
		this.selectionRecord.addSelectionChangeListener(selectionChangeListener);
		this.shapePane.updateView();
		this.isOpen = true;
	}

	public void selectAndFocusOnElement(ICompoundEdge linkEdge) {
		selectionRecord.clear();
		IDrawingElementController linkController = viewModel.getLinkController(linkEdge);
		selectionRecord.setPrimarySelection(linkController);
	}
}
