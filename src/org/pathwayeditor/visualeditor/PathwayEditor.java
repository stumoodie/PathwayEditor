package org.pathwayeditor.visualeditor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.apache.log4j.Logger;
import org.pathwayeditor.businessobjects.drawingprimitives.IBendPoint;
import org.pathwayeditor.businessobjects.drawingprimitives.ICanvas;
import org.pathwayeditor.businessobjects.drawingprimitives.ICanvasAttribute;
import org.pathwayeditor.businessobjects.drawingprimitives.IDrawingElementSelection;
import org.pathwayeditor.businessobjects.drawingprimitives.ILinkAttribute;
import org.pathwayeditor.businessobjects.drawingprimitives.ILinkEdge;
import org.pathwayeditor.businessobjects.drawingprimitives.ISelectionFactory;
import org.pathwayeditor.businessobjects.drawingprimitives.attributes.LineStyle;
import org.pathwayeditor.businessobjects.drawingprimitives.attributes.RGB;
import org.pathwayeditor.figure.geometry.Dimension;
import org.pathwayeditor.figure.geometry.Envelope;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.visualeditor.behaviour.IDefaultPopupActions;
import org.pathwayeditor.visualeditor.behaviour.IEditingOperation;
import org.pathwayeditor.visualeditor.behaviour.ILinkBendPointPopupActions;
import org.pathwayeditor.visualeditor.behaviour.ILinkOperation;
import org.pathwayeditor.visualeditor.behaviour.ILinkPopupActions;
import org.pathwayeditor.visualeditor.behaviour.IMarqueeOperation;
import org.pathwayeditor.visualeditor.behaviour.IMouseBehaviourController;
import org.pathwayeditor.visualeditor.behaviour.IOperationFactory;
import org.pathwayeditor.visualeditor.behaviour.IResizeOperation;
import org.pathwayeditor.visualeditor.behaviour.IShapePopupActions;
import org.pathwayeditor.visualeditor.behaviour.MouseBehaviourController;
import org.pathwayeditor.visualeditor.commands.CommandStack;
import org.pathwayeditor.visualeditor.commands.CompoundCommand;
import org.pathwayeditor.visualeditor.commands.CreateBendPointCommand;
import org.pathwayeditor.visualeditor.commands.DeleteBendPointCommand;
import org.pathwayeditor.visualeditor.commands.ICommand;
import org.pathwayeditor.visualeditor.commands.ICommandStack;
import org.pathwayeditor.visualeditor.commands.ICompoundCommand;
import org.pathwayeditor.visualeditor.commands.MoveBendPointCommand;
import org.pathwayeditor.visualeditor.commands.MoveNodeCommand;
import org.pathwayeditor.visualeditor.commands.ReparentSelectionCommand;
import org.pathwayeditor.visualeditor.commands.ResizeNodeCommand;
import org.pathwayeditor.visualeditor.controller.IDrawingPrimitiveController;
import org.pathwayeditor.visualeditor.controller.INodeController;
import org.pathwayeditor.visualeditor.controller.IRootController;
import org.pathwayeditor.visualeditor.controller.IViewControllerStore;
import org.pathwayeditor.visualeditor.controller.ViewControllerStore;
import org.pathwayeditor.visualeditor.editingview.DomainModelLayer;
import org.pathwayeditor.visualeditor.editingview.FeedbackLayer;
import org.pathwayeditor.visualeditor.editingview.IShapePane;
import org.pathwayeditor.visualeditor.editingview.SelectionLayer;
import org.pathwayeditor.visualeditor.editingview.ShapePane;
import org.pathwayeditor.visualeditor.feedback.FeedbackModel;
import org.pathwayeditor.visualeditor.feedback.IFeedbackElement;
import org.pathwayeditor.visualeditor.feedback.IFeedbackLink;
import org.pathwayeditor.visualeditor.feedback.IFeedbackModel;
import org.pathwayeditor.visualeditor.feedback.IFeedbackNode;
import org.pathwayeditor.visualeditor.geometry.CommonParentCalculator;
import org.pathwayeditor.visualeditor.geometry.IIntersectionCalculator;
import org.pathwayeditor.visualeditor.selection.ILinkSelection;
import org.pathwayeditor.visualeditor.selection.INodeSelection;
import org.pathwayeditor.visualeditor.selection.ISelection;
import org.pathwayeditor.visualeditor.selection.ISelectionChangeEvent;
import org.pathwayeditor.visualeditor.selection.ISelectionChangeListener;
import org.pathwayeditor.visualeditor.selection.ISelectionHandle;
import org.pathwayeditor.visualeditor.selection.ISelectionRecord;
import org.pathwayeditor.visualeditor.selection.ISubgraphSelection;
import org.pathwayeditor.visualeditor.selection.SelectionRecord;
import org.pathwayeditor.visualeditor.selection.ISelection.SelectionType;

public class PathwayEditor extends JPanel {
	private static final long serialVersionUID = 1L;

	private final Logger logger = Logger.getLogger(this.getClass());
	private IShapePane shapePane;
	private JScrollPane scrollPane;
	private IViewControllerStore viewModel;
	private ISelectionRecord selectionRecord;
	private ICommandStack commandStack;
	private IMouseBehaviourController editBehaviourController;
	private ISelectionChangeListener selectionChangeListener;
	private IFeedbackModel feedbackModel;
	private boolean isOpen = false;
	private CommonParentCalculator newParentCalc;
	private IShapePopupActions shapePopupMenuResponse;
	private ILinkPopupActions linkPopupMenuResponse;
	private IDefaultPopupActions defaultPopupMenuResponse;
	private ILinkBendPointPopupActions linkBendpointPopupResponse;
	
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
	
	public void loadCanvas(ICanvas canvas){
		if(isOpen){
			reset();
		}
        this.commandStack = new CommandStack();
		viewModel = new ViewControllerStore(canvas.getModel());
		newParentCalc = new CommonParentCalculator(viewModel.getIntersectionCalculator());
		this.selectionRecord = new SelectionRecord(viewModel);
		this.feedbackModel = new FeedbackModel(this.selectionRecord);
//		this.shapePane = new ShapePane(viewModel, this.selectionRecord, this.feedbackModel);
		this.shapePane = new ShapePane();
		this.shapePane.addLayer(new DomainModelLayer(viewModel));
		this.shapePane.addLayer(new SelectionLayer(this.selectionRecord));
		this.shapePane.addLayer(new FeedbackLayer(this.feedbackModel));
		this.shapePane.setPaneBounds(new Envelope(0, 0, 400.0, 400.0));
		scrollPane = new JScrollPane((ShapePane)this.shapePane, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setPreferredSize(this.getPreferredSize());
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);
		this.add(scrollPane, BorderLayout.CENTER);
		
		Envelope canvasBounds = viewModel.getCanvasBounds();
		((ShapePane)this.shapePane).setSize((int)Math.ceil(canvasBounds.getDimension().getWidth()), (int)Math.ceil(canvasBounds.getDimension().getHeight()));
        final IEditingOperation editOperation = new IEditingOperation(){

			@Override
			public void moveFinished(Point delta, ReparentingStateType reparentingState) {
				if(logger.isTraceEnabled()){
					logger.trace("Move finished. Delta=" + delta);
				}
				if(reparentingState.equals(ReparentingStateType.CAN_REPARENT)){
					createMoveCommand(delta, true);
					restoreSelection();
				}
				else if(reparentingState.equals(ReparentingStateType.CAN_MOVE)){
					createMoveCommand(delta, false);
					restoreSelection();
				}
				feedbackModel.clear();
				shapePane.updateView();
				
			}

			@Override
			public void moveOngoing(Point delta) {
				if(logger.isTraceEnabled()){
					logger.trace("Ongoning move. Delta=" + delta);
				}
				moveSelection(delta);
				shapePane.updateView();
			}

			@Override
			public void moveStarted() {
				logger.trace("Move started.");
				feedbackModel.rebuildIncludingHierarchy();
			}

			@Override
			public ReparentingStateType getReparentingState(Point delta) {
				ReparentingStateType retVal = ReparentingStateType.FORBIDDEN;
//				newParentCalc = new CommonParentCalculator(viewModel);
				newParentCalc.findCommonParent(selectionRecord.getSubgraphSelection(), delta);
		        if(newParentCalc.hasFoundCommonParent()) {
		        	if(logger.isTraceEnabled()){
		        		logger.trace("Common parent found. Node=" + newParentCalc.getCommonParent());
		        	}
		        	// parent is consistent - now we need to check if any node already has this parent
		        	// if all do then we move, in one or more doesn't then we fail reparenting
		        	if(newParentCalc.canReparentSelection()){
		        		retVal = ReparentingStateType.CAN_REPARENT;
		        	}
		        	else if(newParentCalc.canMoveSelection()){
		        		retVal = ReparentingStateType.CAN_MOVE;
		        	}
		        }
		        else{
		        	logger.trace("No common parent found.");
		        }
		    	if(logger.isTraceEnabled()){
		    		logger.trace("Reparent state=" + retVal);
		    	}
		        return retVal;
			}

        };
        final IResizeOperation resizeOperation = new IResizeOperation() {
			
			@Override
			public void resizeStarted() {
				logger.trace("Resize started");
				feedbackModel.rebuildWithStrictSelection();
			}
			
			@Override
			public void resizeFinished(Point originDelta, Dimension resizeDelta) {
				if(logger.isTraceEnabled()){
					logger.trace("Resize finished. originDelta=" + originDelta + ", dimDelta=" + resizeDelta);
				}
				createResizeCommand(originDelta, resizeDelta);
				feedbackModel.clear();
				restoreSelection();
				shapePane.updateView();
			}
			
			@Override
			public void resizeContinuing(Point originDelta, Dimension resizeDelta) {
				resizeSelection(originDelta, resizeDelta);
				shapePane.updateView();
			}

			@Override
			public boolean canResize(Point originDelta, Dimension resizeDelta) {
				return canContinueToResize(originDelta, resizeDelta);
			}
		};
		final ILinkOperation linkOperation = new ILinkOperation(){
			@Override
			public void moveBendPointFinished(ISelectionHandle bendPointHandle, Point position) {
				if(logger.isTraceEnabled()){
					logger.trace("Move bendpoint finished. bpIdx=" + bendPointHandle.getHandleIndex() + ",position=" + position);
				}
				createMoveBendPointCommand(bendPointHandle.getHandleIndex(), position);
				feedbackModel.clear();
				restoreSelection();
				shapePane.updateView();
			}

			@Override
			public void moveBendPointOngoing(ISelectionHandle handle, Point position) {
				if(logger.isTraceEnabled()){
					logger.trace("Move bendpoint ongoing. bpIdx=" + handle.getHandleIndex() + ",position=" + position);
				}
				moveBendPoint(handle.getHandleIndex(), position);
				shapePane.updateView();
			}

			@Override
			public void moveBendPointStated(ISelectionHandle selectionHandle) {
				logger.trace("Move bendpoint started");
				feedbackModel.rebuildOnLinkSelection((ILinkSelection)selectionHandle.getSelection());
			}

			@Override
			public void newBendPointFinished(ISelectionHandle selectionHandle, Point position) {
				if(logger.isTraceEnabled()){
					logger.trace("New bendpoint finished. lineSeg=" + selectionHandle.getHandleIndex() + ",position=" + position);
				}
				createNewBendPointCommand(selectionHandle.getHandleIndex(), position);
				feedbackModel.clear();
				restoreSelection();
				shapePane.updateView();
			}

			@Override
			public void newBendPointOngoing(ISelectionHandle handle, Point position) {
				if(logger.isTraceEnabled()){
					logger.trace("Moving new bendpoint ongoing. bpIdx=" + handle.getHandleIndex() + ",position=" + position);
				}
				moveBendPoint(handle.getHandleIndex(), position);
				shapePane.updateView();
			}

			@Override
			public void newBendPointStarted(ISelectionHandle selectionHandle) {
				logger.trace("New bendpoint started");
				feedbackModel.rebuildOnLinkSelection((ILinkSelection)selectionHandle.getSelection());
				IFeedbackLink feedbackLink = feedbackModel.uniqueFeedbackLink();
				Point handleOrigin = selectionHandle.getBounds().getOrigin();
				Point handleCentre = handleOrigin.translate(selectionHandle.getBounds().getDimension().getWidth()/2.0,
						selectionHandle.getBounds().getDimension().getHeight()/2.0);
				feedbackLink.newBendPoint(selectionHandle.getHandleIndex(), handleCentre);
			}
			
		};
		final IMarqueeOperation marqueeOperation = new IMarqueeOperation() {
			@Override
			public void selectionStarted(Point initialPosn) {
				feedbackModel.clear();
				selectionRecord.clear();
				IFeedbackNode node = feedbackModel.createSingleNode(new Envelope(initialPosn, new Dimension(0.1, 0.1)));
				node.setFillColour(RGB.BLUE);
				node.setLineColour(RGB.RED);
				node.setLineStyle(LineStyle.SOLID);
				node.setLineWidth(1.0);
			}

			@Override
			public void selectionFinished(Point originDelta, Dimension sizeDelta) {
				IFeedbackNode node = feedbackModel.uniqueFeedbackNode();
				node.resizePrimitive(originDelta, sizeDelta);
				makeSelectionFromMarquee(node.getBounds());
				feedbackModel.clear();
				shapePane.updateView();
			}
			
			@Override
			public void selectionContinuing(Point originDelta, Dimension sizeDelta) {
				IFeedbackNode node = feedbackModel.uniqueFeedbackNode();
				node.resizePrimitive(originDelta, sizeDelta);
				shapePane.updateView();
			}
		};
		this.shapePopupMenuResponse = new IShapePopupActions() {
			
			@Override
			public void delete() {
				deleteSelection();
				selectionRecord.clear();
				shapePane.updateView();
			}
		};
		this.linkPopupMenuResponse = new ILinkPopupActions() {
			
			@Override
			public void delete() {
				deleteSelection();
				selectionRecord.clear();
				shapePane.updateView();
			}
		};
		this.linkBendpointPopupResponse = new ILinkBendPointPopupActions() {
			
			@Override
			public void deleteBendPoint(int bpIdx) {
				deleteBendpoint(bpIdx);
				restoreSelection();
				shapePane.updateView();
			}
			
			@Override
			public void delete() {
				deleteSelection();
				selectionRecord.clear();
				shapePane.updateView();
			}

		};
		this.defaultPopupMenuResponse = new IDefaultPopupActions() {
			
			@Override
			public void selectAll() {
				selectAllElements();
				shapePane.updateView();
			}

			@Override
			public void delete() {
				deleteSelection();
				selectionRecord.clear();
				shapePane.updateView();
			}

			@Override
			public boolean isDeleteActionValid() {
				return selectionRecord.numSelected() > 0;
			}
		};
        this.editBehaviourController = new MouseBehaviourController(shapePane, new IOperationFactory() {
			
			@Override
			public IShapePopupActions getShapePopupMenuResponse() {
				return shapePopupMenuResponse;
			}
			
			@Override
			public IResizeOperation getResizeOperation() {
				return resizeOperation;
			}
			
			@Override
			public IEditingOperation getMoveOperation() {
				return editOperation;
			}
			
			@Override
			public IMarqueeOperation getMarqueeOperation() {
				return marqueeOperation;
			}
			
			@Override
			public ILinkPopupActions getLinkPopupMenuResponse() {
				return linkPopupMenuResponse;
			}
			
			@Override
			public ILinkOperation getLinkOperation() {
				return linkOperation;
			}
			
			@Override
			public IDefaultPopupActions getDefaultPopupMenuResponse() {
				return defaultPopupMenuResponse;
			}

			@Override
			public ILinkBendPointPopupActions getLinkBendpointPopupMenuResponse() {
				return linkBendpointPopupResponse;
			}
		});
        this.selectionChangeListener = new ISelectionChangeListener() {
			
			@Override
			public void selectionChanged(ISelectionChangeEvent event) {
				shapePane.updateView();
			}
		};
		this.initialise();
	}

	protected void deleteBendpoint(int bpIdx) {
		ILinkSelection linkSelection = this.selectionRecord.getUniqueLinkSelection(); 
		ICommand cmd = new DeleteBendPointCommand(linkSelection.getPrimitiveController().getDrawingElement(), bpIdx);
		this.commandStack.execute(cmd);
	}

	protected void selectAllElements() {
		Iterator<IDrawingPrimitiveController> primIter = this.viewModel.drawingPrimitiveIterator();
		boolean firstTime = true;
		while(primIter.hasNext()){
			IDrawingPrimitiveController controller = primIter.next();
			if(!(controller instanceof IRootController)){
				if(firstTime){
					selectionRecord.setPrimarySelection(controller);
					firstTime = false;
				}
				else{
					selectionRecord.addSecondarySelection(controller);
				}
			}
		}
	}

	protected void deleteSelection() {
		Iterator<INodeSelection> nodeSelectionIter = selectionRecord.selectedNodesIterator();
		ISelectionFactory selectionFact = null;
		while(nodeSelectionIter.hasNext()){
			INodeSelection selectedNode = nodeSelectionIter.next();
			if(selectionFact == null){
				selectionFact = selectedNode.getPrimitiveController().getViewModel().getDomainModel().newSelectionFactory();
			}
			selectionFact.addDrawingNode(selectedNode.getPrimitiveController().getDrawingElement().getCurrentDrawingElement());
		}
		Iterator<ILinkSelection> linkSelectionIter = selectionRecord.selectedLinksIterator();
		while(linkSelectionIter.hasNext()){
			ILinkSelection selectedLink = linkSelectionIter.next();
			if(selectionFact == null){
				selectionFact = selectedLink.getPrimitiveController().getViewModel().getDomainModel().newSelectionFactory();
			}
			selectionFact.addLink(selectedLink.getPrimitiveController().getDrawingElement().getCurrentDrawingElement());
		}
		if(selectionFact != null){
			IDrawingElementSelection seln = selectionFact.createGeneralSelection();
			seln.getModel().removeSubgraph(seln);
		}
	}

	protected void makeSelectionFromMarquee(Envelope bounds) {
		IIntersectionCalculator intersectionCal = this.viewModel.getIntersectionCalculator();
		SortedSet<IDrawingPrimitiveController> selectedController = intersectionCal.findIntersectingController(bounds);
		boolean firstOne = true;
		for(IDrawingPrimitiveController controller : selectedController){
			if(firstOne){
				this.selectionRecord.setPrimarySelection(controller);
				firstOne = false;
			}
			else{
				this.selectionRecord.addSecondarySelection(controller);
			}
		}
	}

	protected void restoreSelection() {
		Iterator<ISelection> selectionIter = this.selectionRecord.selectionIterator();
		SortedSet<ISelection> selectedSet = new TreeSet<ISelection>();
		while(selectionIter.hasNext()){
			ISelection selection = selectionIter.next();
			selectedSet.add(selection);
		}
		this.selectionRecord.clear();
		for(ISelection selection : selectedSet){
			ICanvasAttribute att = selection.getPrimitiveController().getDrawingElement();
			IDrawingPrimitiveController newController = this.viewModel.getDrawingPrimitiveController(att);
			if(selection.getSelectionType().equals(SelectionType.PRIMARY)){
				this.selectionRecord.setPrimarySelection(newController);
			}
			else{
				this.selectionRecord.addSecondarySelection(newController);
			}
		}
	}

	protected void createNewBendPointCommand(int lineSegmentIdx, Point position) {
		ILinkSelection linkSelection = this.selectionRecord.getUniqueLinkSelection(); 
		ICommand cmd = new CreateBendPointCommand(linkSelection.getPrimitiveController().getDrawingElement(), lineSegmentIdx, position);
		this.commandStack.execute(cmd);
	}

	protected void createMoveBendPointCommand(int bpIdx, Point position) {
		ILinkSelection linkSelection = this.selectionRecord.getUniqueLinkSelection(); 
		ICommand cmd = new MoveBendPointCommand(linkSelection.getPrimitiveController().getDrawingElement().getBendPoint(bpIdx), position);
		this.commandStack.execute(cmd);
	}

	private void resizeSelection(Point originDelta, Dimension resizeDelta) {
		Iterator<IFeedbackNode> moveNodeIterator = this.feedbackModel.nodeIterator();
		while(moveNodeIterator.hasNext()){
			IFeedbackNode nodePrimitive = moveNodeIterator.next();
			nodePrimitive.resizePrimitive(originDelta, resizeDelta);
			if(logger.isTraceEnabled()){
				logger.trace("Resizing shape to bounds: " + nodePrimitive.getBounds());
			}
		}
	}

//	private void newBendPointFeedback(Point location, int lineSegmentIdx){
//		IFeedbackLink feedback = this.feedbackModel.getUniqueLink();
//		while(moveNodeIterator.hasNext()){
//			IFeedbackNode nodePrimitive = moveNodeIterator.next();
//			nodePrimitive.translatePrimitive(delta);
//			logger.trace("Dragged shape to location: " + nodePrimitive.getBounds().getOrigin());
//		}
//	}
	
	private void createResizeCommand(Point originDelta, Dimension resizeDelta) {
		Iterator<ISelection> moveNodeIterator = this.selectionRecord.selectionIterator();
		ICompoundCommand cmpCommand = new CompoundCommand();
		while(moveNodeIterator.hasNext()){
			INodeController nodePrimitive = (INodeController)moveNodeIterator.next().getPrimitiveController();
			ICommand cmd = new ResizeNodeCommand(nodePrimitive.getDrawingElement(), originDelta, resizeDelta);
			cmpCommand.addCommand(cmd);
			logger.trace("Dragged shape to location: " + nodePrimitive.getBounds().getOrigin());
		}
		this.commandStack.execute(cmpCommand);
	}

	private boolean canContinueToResize(Point originDelta, Dimension resizeDelta){
		boolean retVal = true;
		Iterator<INodeSelection> iter = this.selectionRecord.selectedNodesIterator();
		while(iter.hasNext() && retVal){
			INodeController nodeController = iter.next().getPrimitiveController();
			retVal = nodeController.canResize(originDelta, resizeDelta);
		}
		
		return retVal;
	}
	
	
	private void createMoveCommand(Point delta, boolean reparentingEnabled){
		ICompoundCommand cmpCommand = new CompoundCommand();
		Iterator<INodeSelection> moveNodeIterator = this.selectionRecord.getSubgraphSelection().topSelectedNodeIterator();
		while(moveNodeIterator.hasNext()){
			INodeController nodePrimitive = (INodeController)moveNodeIterator.next().getPrimitiveController();
			ICommand cmd = new MoveNodeCommand(nodePrimitive.getDrawingElement(), delta);
			cmpCommand.addCommand(cmd);
			if(logger.isTraceEnabled()){
				logger.trace("Dragged shape to location: " + nodePrimitive.getBounds().getOrigin());
			}
		}
		Iterator<ILinkSelection> moveLinkIterator = this.selectionRecord.getSubgraphSelection().selectedLinkIterator();
		while(moveLinkIterator.hasNext()){
			ILinkAttribute nodePrimitive = moveLinkIterator.next().getPrimitiveController().getDrawingElement();
			Iterator<IBendPoint> bpIter = nodePrimitive.bendPointIterator();
			while(bpIter.hasNext()){
				IBendPoint bp = bpIter.next();
				Point newPosn = bp.getLocation().translate(delta);
				ICommand cmd = new MoveBendPointCommand(bp, newPosn);
				cmpCommand.addCommand(cmd);
			}
		}
		if(reparentingEnabled){
			INodeController target = calculateReparentTarget(delta);
			ICommand cmd = new ReparentSelectionCommand(target.getDrawingElement().getCurrentDrawingElement(), this.selectionRecord.getSubgraphSelection().getDrawingElementSelection());
			cmpCommand.addCommand(cmd);
		}
		this.commandStack.execute(cmpCommand);
	}
	
	private INodeController calculateReparentTarget(Point delta) {
		INodeController retVal = null;
		newParentCalc.findCommonParent(selectionRecord.getSubgraphSelection(), delta);
        if(newParentCalc.hasFoundCommonParent()) {
        	if(logger.isTraceEnabled()){
        		logger.trace("Common parent found. Node=" + newParentCalc.getCommonParent());
        	}
        	// parent is consistent - now we need to check if any node already has this parent
        	// if all do then we move, in one or more doesn't then we fail reparenting
        	retVal = newParentCalc.getCommonParent();
        }
        else{
        	logger.trace("No common parent found.");
        }
    	if(logger.isTraceEnabled()){
    		logger.trace("Can reparent=" + retVal);
    	}
        return retVal;
	}

	
	private void moveSelection(Point delta) {
		ISubgraphSelection subgraphSelection = this.selectionRecord.getSubgraphSelection();
		Iterator<INodeSelection> moveNodeIterator = subgraphSelection.selectedNodeIterator();
		while(moveNodeIterator.hasNext()){
			ISelection selection = moveNodeIterator.next();
			IFeedbackElement feedbackElement = this.feedbackModel.getFeedbackElement(selection.getPrimitiveController());
			feedbackElement.translatePrimitive(delta);
			if(logger.isTraceEnabled()){
				logger.trace("Dragged feedback element: " + feedbackElement);
			}
		}
		Iterator<ILinkSelection> moveLinkIterator = subgraphSelection.selectedLinkIterator();
		while(moveLinkIterator.hasNext()){
			ILinkSelection selection = moveLinkIterator.next();
			IFeedbackLink feedbackLink = (IFeedbackLink)this.feedbackModel.getFeedbackElement(selection.getPrimitiveController());
			for(int bpIdx = 0; bpIdx < feedbackLink.getLinkDefinition().numBendPoints(); bpIdx++){
				feedbackLink.translateBendPoint(bpIdx, delta);
				if(logger.isTraceEnabled()){
					logger.trace("Moved bendpont=" + bpIdx + " of feedback element: " + feedbackLink);
				}
			}
		}
	}
	
	private void moveBendPoint(int bendPointIdx, Point position) {
		IFeedbackLink feedbackLink = this.feedbackModel.uniqueFeedbackLink();
		feedbackLink.moveBendPoint(bendPointIdx, position);
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
