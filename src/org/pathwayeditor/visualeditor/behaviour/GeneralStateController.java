package org.pathwayeditor.visualeditor.behaviour;

import javax.swing.JPopupMenu;

import org.apache.log4j.Logger;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.visualeditor.editingview.ISelectionLayer;
import org.pathwayeditor.visualeditor.editingview.IShapePane;
import org.pathwayeditor.visualeditor.editingview.LayerType;
import org.pathwayeditor.visualeditor.selection.ISelectionHandle;

public class GeneralStateController implements ISelectionStateBehaviourController {
	private final Logger logger = Logger.getLogger(this.getClass());
	private final IHitCalculator hitCalc;
	private final IShapePane shapePane;
	private final IControllerResponses responses;

	public GeneralStateController(IShapePane pane, IHitCalculator hitCalc, IControllerResponses responses){
		this.shapePane = pane;
		this.responses = responses;
		this.hitCalc = hitCalc;
	}
	

	@Override
	public Point getDiagramLocation(){
		return this.hitCalc.getDiagramLocation();
	}
	

	
	@Override
	public IDragResponse getDragResponse() {
		return this.responses.getDragResponse(getSelectionHandle());
	}


	@Override
	public IMouseFeedbackResponse getMouseFeedbackResponse() {
		return this.responses.getFeedbackResponse(getSelectionHandle());
	}


	@Override
	public ISelectionResponse getClickResponse() {
		return this.responses.getSelectionResponse();
	}


	private ISelectionHandle getSelectionHandle() {
		ISelectionLayer selectionLayer = this.shapePane.getLayer(LayerType.SELECTION);		
		return selectionLayer.getSelectionRecord().findSelectionModelAt(getDiagramLocation());
	}


	@Override
	public IPopupMenuResponse getPopupMenuResponse() {
		IPopupMenuResponse retVal = this.responses.getPopupMenuResponse(getSelectionHandle());
		return retVal;
	}


	@Override
	public void showPopupMenus(JPopupMenu popup) {
		Point mousePosition = this.hitCalc.getMousePosition();
		this.shapePane.showPopup(popup, mousePosition.getX(), mousePosition.getY());
	}


	@Override
	public void setMousePosition(double x, double y) {
		this.hitCalc.setMousePosition(x, y);
	}


//	@Override
//	public INodeController getNodeAtCurrentPoint() {
//		IDomainModelLayer domainLayer = this.shapePane.getLayer(LayerType.DOMAIN);
//		IIntersectionCalculator intnCalc = domainLayer.getViewControllerStore().getIntersectionCalculator();
//		intnCalc.setFilter(new IIntersectionCalcnFilter() {
//			@Override
//			public boolean accept(IDrawingElementController node) {
//				return node instanceof INodeController;
//			}
//		});
//		SortedSet<IDrawingElementController> hits = intnCalc.findDrawingPrimitivesAt(getDiagramLocation());
//		INodeController retVal = null;
//		if(!hits.isEmpty()){
//			retVal = (INodeController)hits.first();
//		}
//		return retVal;
//	}
}
