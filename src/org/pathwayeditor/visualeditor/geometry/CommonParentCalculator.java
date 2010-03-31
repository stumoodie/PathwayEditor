package org.pathwayeditor.visualeditor.geometry;

import java.util.Iterator;
import java.util.SortedSet;

import org.apache.log4j.Logger;
import org.pathwayeditor.businessobjects.drawingprimitives.IDrawingNodeAttribute;
import org.pathwayeditor.figure.geometry.IConvexHull;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.visualeditor.controller.IDrawingPrimitiveController;
import org.pathwayeditor.visualeditor.controller.ILabelController;
import org.pathwayeditor.visualeditor.controller.INodeController;
import org.pathwayeditor.visualeditor.controller.IShapeController;
import org.pathwayeditor.visualeditor.selection.INodeSelection;
import org.pathwayeditor.visualeditor.selection.ISubgraphSelection;

public class CommonParentCalculator implements ICommonParentCalculator {
	private final Logger logger = Logger.getLogger(this.getClass());
	private final IIntersectionCalculator calc;
	private INodeController parent = null;
	private int numNodesAlreadyHaveParent = 0;
	private ISubgraphSelection selection = null;
	
	private interface IHandleLabel {
		
		INodeController getLabelParent(ILabelController node);
		
	}
	
	public CommonParentCalculator(IIntersectionCalculator calc){
		this.calc = calc;
	}
	
	/* (non-Javadoc)
	 * @see org.pathwayeditor.visualeditor.geometry.ICommonParentCalculator#findCommonParentExcludingLabels(org.pathwayeditor.visualeditor.selection.ISubgraphSelection, org.pathwayeditor.figure.geometry.Point)
	 */
	public void findCommonParentExcludingLabels(ISubgraphSelection testSelection, Point delta){
		findCommonParentImpl(testSelection, delta, new IHandleLabel(){

			// we're ignoreing labels
			public INodeController getLabelParent(ILabelController node) {
				return null;
			}
			
		});
	}
	
	private INodeController getNodeController(IDrawingNodeAttribute node){
		return this.calc.getModel().getNodeController(node);
	}
	
	/* (non-Javadoc)
	 * @see org.pathwayeditor.visualeditor.geometry.ICommonParentCalculator#findCommonParent(org.pathwayeditor.visualeditor.selection.ISubgraphSelection, org.pathwayeditor.figure.geometry.Point)
	 */
	public void findCommonParent(ISubgraphSelection testSelection, Point delta) {
		findCommonParentImpl(testSelection, delta, new IHandleLabel(){

			// we're ignoreing labels
			public INodeController getLabelParent(ILabelController node) {
				return getNodeController(node.getDrawingElement().getCurrentDrawingElement().getParentNode().getAttribute());
			}
			
		});
	}
	
	private void findCommonParentImpl(ISubgraphSelection testSelection, Point delta, IHandleLabel labelHandler) {
		if (this.selection == null || (!testSelection.equals(this.selection) ) ) {
			// if new or different selection to last one then recalculate the
			// common parent
			this.selection = testSelection;
			Iterator<INodeSelection> selectionIter = selection.topSelectedNodeIterator();
			parent = selection.getSelectionRecord().getPrimarySelection().getPrimitiveController().getViewModel().getRootNode();
			boolean firstTime = true;
			numNodesAlreadyHaveParent = 0;
			while (selectionIter.hasNext() && parent != null) {
				INodeController node = selectionIter.next().getPrimitiveController();
				INodeController potentialParent = null;
				if (node instanceof IShapeController) {
					IConvexHull hull = node.getConvexHull();
					IConvexHull newLocation = hull.translate(delta);
					potentialParent = this.findPotentialParent(node, newLocation);
				} else if (node instanceof ILabelController) {
					// labels cannot be reparented
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
					if (parent.equals(getNodeController(node.getDrawingElement().getCurrentDrawingElement().getParentNode().getAttribute()))) {
						numNodesAlreadyHaveParent++;
					}
				}
			}
		} else {
			// the selection and change are the same so the results must be the
			// same as last time
		}
	}

	/* (non-Javadoc)
	 * @see org.pathwayeditor.visualeditor.geometry.ICommonParentCalculator#getSelectionToReparent()
	 */
	public ISubgraphSelection getSelectionToReparent(){
		return this.selection;
	}
	
	/* (non-Javadoc)
	 * @see org.pathwayeditor.visualeditor.geometry.ICommonParentCalculator#canReparentSelection()
	 */
	public boolean canReparentSelection(){
		return this.numNodesAlreadyHaveParent == 0;
	}
	
	/* (non-Javadoc)
	 * @see org.pathwayeditor.visualeditor.geometry.ICommonParentCalculator#canMoveSelection()
	 */
	public boolean canMoveSelection(){
		return this.numNodesAlreadyHaveParent == 0 || this.numNodesAlreadyHaveParent == this.selection.numTopDrawingNodes();
	}
	
	/* (non-Javadoc)
	 * @see org.pathwayeditor.visualeditor.geometry.ICommonParentCalculator#hasFoundCommonParent()
	 */
	public boolean hasFoundCommonParent(){
		return this.parent != null;
	}
	
	/* (non-Javadoc)
	 * @see org.pathwayeditor.visualeditor.geometry.ICommonParentCalculator#getCommonParent()
	 */
	public INodeController getCommonParent(){
		return this.parent;
	}
	
	
	/* (non-Javadoc)
	 * @see org.pathwayeditor.visualeditor.geometry.ICommonParentCalculator#findPotentialParent(org.pathwayeditor.visualeditor.controller.INodeController, org.pathwayeditor.figure.geometry.IConvexHull)
	 */
	public INodeController findPotentialParent(final INodeController potentialChild, IConvexHull testPlacement){
		calc.setFilter(new IIntersectionCalcnFilter(){

			@Override
			public boolean accept(IDrawingPrimitiveController cont) {
				boolean retVal = false;
				if(cont instanceof INodeController){
					INodeController node = (INodeController)cont;
					retVal = node.getDrawingElement().getCurrentDrawingElement().canParent(potentialChild.getDrawingElement().getCurrentDrawingElement());
					if(logger.isTraceEnabled()){
						logger.trace("Node=" + node +" canParent=" + retVal + ", potentialChild=" + potentialChild);
					}
				}
				return retVal;
			}
			
		});
		SortedSet<IDrawingPrimitiveController> nodes = calc.findIntersectingNodes(testPlacement, potentialChild);
		INodeController retVal = null;
		if(logger.isTraceEnabled()){
			logger.trace("Potential parents = " + nodes);
		}
		if(!nodes.isEmpty()){
			retVal = (INodeController)nodes.first();
		}
		return retVal;
	}
	
}
