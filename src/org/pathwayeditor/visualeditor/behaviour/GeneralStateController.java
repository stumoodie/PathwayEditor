package org.pathwayeditor.visualeditor.behaviour;

import java.util.SortedSet;

import javax.swing.JPopupMenu;

import org.apache.log4j.Logger;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.visualeditor.controller.IDrawingElementController;
import org.pathwayeditor.visualeditor.controller.INodeController;
import org.pathwayeditor.visualeditor.editingview.IDomainModelLayer;
import org.pathwayeditor.visualeditor.editingview.ISelectionLayer;
import org.pathwayeditor.visualeditor.editingview.IShapePane;
import org.pathwayeditor.visualeditor.editingview.LayerType;
import org.pathwayeditor.visualeditor.geometry.IIntersectionCalcnFilter;
import org.pathwayeditor.visualeditor.geometry.IIntersectionCalculator;
import org.pathwayeditor.visualeditor.selection.ISelectionHandle;

public class GeneralStateController implements ISelectionStateBehaviourController {
	private final Logger logger = Logger.getLogger(this.getClass());
	private final IShapePane shapePane;
	private final IControllerResponses responses;
	private Point mousePosition;

	public GeneralStateController(IShapePane pane, IControllerResponses responses){
		this.shapePane = pane;
		this.responses = responses;
	}
	

	@Override
	public Point getDiagramLocation(){
		Point retVal = this.shapePane.getPaneBounds().getOrigin();
		retVal = retVal.translate(this.mousePosition);
		if(logger.isTraceEnabled()){
			logger.trace("Adjust position. orig=" + this.mousePosition + " : adjustedPoint=" + retVal + ", paneBounds=" + shapePane.getPaneBounds());
		}
		return retVal;  
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
		this.shapePane.showPopup(popup, this.mousePosition.getX(), this.mousePosition.getY());
	}


	@Override
	public void setMousePosition(double x, double y) {
		this.mousePosition = new Point(x, y);
	}


	@Override
	public INodeController getNodeAtCurrentPoint() {
		IDomainModelLayer domainLayer = this.shapePane.getLayer(LayerType.DOMAIN);
		IIntersectionCalculator intnCalc = domainLayer.getViewControllerStore().getIntersectionCalculator();
		intnCalc.setFilter(new IIntersectionCalcnFilter() {
			@Override
			public boolean accept(IDrawingElementController node) {
				return node instanceof INodeController;
			}
		});
		SortedSet<IDrawingElementController> hits = intnCalc.findDrawingPrimitivesAt(getDiagramLocation());
		INodeController retVal = null;
		if(!hits.isEmpty()){
			retVal = (INodeController)hits.first();
		}
		return retVal;
	}
}
