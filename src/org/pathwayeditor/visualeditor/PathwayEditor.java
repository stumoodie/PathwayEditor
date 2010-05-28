package org.pathwayeditor.visualeditor;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

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
import org.pathwayeditor.visualeditor.operations.OperationFactory;
import org.pathwayeditor.visualeditor.selection.ISelectionChangeEvent;
import org.pathwayeditor.visualeditor.selection.ISelectionChangeListener;
import org.pathwayeditor.visualeditor.selection.ISelectionRecord;
import org.pathwayeditor.visualeditor.selection.SelectionRecord;

public class PathwayEditor extends JPanel {
	private static final long serialVersionUID = 1L;

//	private final Logger logger = Logger.getLogger(this.getClass());
	private IShapePane shapePane;
	private JScrollPane scrollPane;
	private PalettePanel palettePane;
//	private JPanel linkPalette;
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
	
//	private void setupPaletteToolbar(INotationSubsystem notationSubsystem){
//		JPanel shapeButtonPanel = new JPanel();
//		this.palettePane = new JScrollPane(shapeButtonPanel);
//		this.palettePane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
//		shapeButtonPanel.setLayout(new BoxLayout(shapeButtonPanel, BoxLayout.PAGE_AXIS));
////		this.linkPalette = new JPanel();
//		JButton selectionButton = new JButton("Selection");
//		selectionButton.addActionListener(new ActionListener(){
//
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				editBehaviourController.setSelectionMode();
//			}
//			
//		});
//		shapeButtonPanel.add(selectionButton);
//		ShapeIconGenerator iconGenerator = new ShapeIconGenerator();
//		iconGenerator.setBounds(new Envelope(0, 0, 40, 30));
//		Iterator<IShapeObjectType> shapeTypeIterator = notationSubsystem.getSyntaxService().shapeTypeIterator();
//		while(shapeTypeIterator.hasNext()){
//			final IShapeObjectType shapeType = shapeTypeIterator.next();
//			iconGenerator.setObjectType(shapeType);
//			iconGenerator.generateImage();
//			iconGenerator.generateIcon();
//			JButton shapeButton = new JButton(iconGenerator.getIcon());
//			shapeButton.setText(shapeType.getName());
//			shapeButton.setToolTipText(shapeType.getDescription());
//			shapeButtonPanel.add(shapeButton);
//			shapeButton.addActionListener(new ActionListener(){
//
//				@Override
//				public void actionPerformed(ActionEvent e) {
//					editBehaviourController.setShapeCreationMode(shapeType);
//				}
//				
//			});
//		}
////		shapeButtonPaneladdSeparator();
////		Iterator<ILinkObjectType> linkTypeIterator = notationSubsystem.getSyntaxService().linkTypeIterator();
////		while(linkTypeIterator.hasNext()){
////			final ILinkObjectType linkType = linkTypeIterator.next();
////			JButton shapeButton = new JButton(linkType.getName());
////			this.linkPalette.add(shapeButton);
////			shapeButton.addActionListener(new ActionListener(){
////
////				@Override
////				public void actionPerformed(ActionEvent e) {
////					editBehaviourController.setLinkCreationMode(linkType);
////				}
////				
////			});
////		}
//		JPanel panel = new JPanel();
////		panel.setPreferredSize(new Dimension(200, 800));
//		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
//		panel.add(this.palettePane);
////		panel.add(this.linkPalette);
//		this.add(panel, BorderLayout.LINE_START);
////		this.add(this.palettePane, BorderLayout.LINE_START);
////		this.add(this.linkPalette);
//	}
	

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
				shapePane.updateView();
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
//		setupPaletteToolbar(canvas.getNotationSubsystem());
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
