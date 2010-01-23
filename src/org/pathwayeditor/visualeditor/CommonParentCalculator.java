package org.pathwayeditor.visualeditor;

import java.util.Iterator;
import java.util.SortedSet;

import org.pathwayeditor.businessobjects.drawingprimitives.IDrawingElementSelection;
import org.pathwayeditor.businessobjects.drawingprimitives.IDrawingNode;
import org.pathwayeditor.visualeditor.controller.ILabelController;
import org.pathwayeditor.visualeditor.controller.INodeController;
import org.pathwayeditor.visualeditor.controller.IShapeController;
import org.pathwayeditor.visualeditor.controller.IViewControllerStore;
import org.pathwayeditor.visualeditor.geometry.IIntersectionCalcnFilter;
import org.pathwayeditor.visualeditor.geometry.INodeIntersectionCalculator;
import org.pathwayeditor.visualeditor.geometry.ShapeIntersectionCalculator;

public class CommonParentCalculator {
	private final INodeIntersectionCalculator calc;
	private INodeController parent = null;
	private int numNodesAlreadyHaveParent = 0;
	private IDrawingElementSelection selection = null;
	
	private interface IHandleLabel {
		
		INodeController getLabelParent(ILabelController node);
		
	}
	
	public CommonParentCalculator(IViewControllerStore model){
		this.calc = new ShapeIntersectionCalculator(model);
	}
	
	public void findCommonParentExcludingLabels(IDrawingElementSelection testSelection){
		findCommonParentImpl(testSelection, new IHandleLabel(){

			// we're ignoreing labels
			public INodeController getLabelParent(ILabelController node) {
				return null;
			}
			
		});
	}
	
	private INodeController getNodeController(IDrawingNode node){
		return this.calc.getModel().getNodePrimitive(node);
	}
	
	public void findCommonParent(IDrawingElementSelection testSelection) {
		findCommonParentImpl(testSelection, new IHandleLabel(){

			// we're ignoreing labels
			public INodeController getLabelParent(ILabelController node) {
				return getNodeController(node.getDrawingElement().getParentNode());
			}
			
		});
	}
	
	private void findCommonParentImpl(IDrawingElementSelection testSelection, IHandleLabel labelHandler) {
		if (this.selection == null
				|| (!testSelection.equals(this.selection) ) ) {
			// if new or different selection to last one then recalculate the
			// common parent
			this.selection = testSelection;
			Iterator<IDrawingNode> selectionIter = selection
					.topDrawingNodeIterator();
			parent = this.calc.getModel().getNodePrimitive(selection.getModel().getRootNode());
			boolean firstTime = true;
			numNodesAlreadyHaveParent = 0;
			while (selectionIter.hasNext() && parent != null) {
				INodeController node = getNodeController(selectionIter.next());
				INodeController potentialParent = null;
				if (node instanceof IShapeController) {
					potentialParent = this.findPotentialParent(node);
				} else if (node instanceof ILabelController) {
					// labels cannot be reparented
//					potentialParent = node.getParentNode();
					potentialParent = labelHandler.getLabelParent((ILabelController)node);
				}
				if (firstTime) {
					firstTime = false;
					parent = potentialParent;
				} else if (!parent.equals(potentialParent)) {
					// parents are inconsistent and so fail reparenting
					parent = null;
				} else {
					// do nothing as parent must equal potential parent
				}
				if (parent == null) {
					numNodesAlreadyHaveParent = 0;
				} else {
					// now check if already has this parent
					if (parent.equals(getNodeController(node.getDrawingElement().getParentNode()))) {
						numNodesAlreadyHaveParent++;
					}
				}
			}
		} else {
			// the selection and change are the same so the results must be the
			// same as last time
		}
	}

	public IDrawingElementSelection getSelectionToReparent(){
		return this.selection;
	}
	
	public boolean canReparentSelection(){
		return this.numNodesAlreadyHaveParent == 0;
	}
	
	public boolean canMoveSelection(){
		return this.numNodesAlreadyHaveParent == 0 || this.numNodesAlreadyHaveParent == this.selection.numTopDrawingNodes();
	}
	
	public boolean hasFoundCommonParent(){
		return this.parent != null;
	}
	
	public INodeController getCommonParent(){
		return this.parent;
	}
	
	
	public INodeController findPotentialParent(final INodeController potentialChild){
		calc.setFilter(new IIntersectionCalcnFilter(){

			@Override
			public boolean accept(INodeController node) {
				return node.getDrawingElement().canParent(potentialChild.getDrawingElement()); 
			}
			
		});
		SortedSet<INodeController> nodes = calc.findIntersectingNodes(potentialChild.getConvexHull(), potentialChild);
		INodeController retVal = null;
		if(!nodes.isEmpty()){
			retVal = nodes.first();
		}
		return retVal;
	}
	
}
