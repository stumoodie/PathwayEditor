/*
  Licensed to the Court of the University of Edinburgh (UofE) under one
  or more contributor license agreements.  See the NOTICE file
  distributed with this work for additional information
  regarding copyright ownership.  The UofE licenses this file
  to you under the Apache License, Version 2.0 (the
  "License"); you may not use this file except in compliance
  with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing,
  software distributed under the License is distributed on an
  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  KIND, either express or implied.  See the License for the
  specific language governing permissions and limitations
  under the License.
*/
package org.pathwayeditor.visualeditor.geometry;

import java.util.Iterator;
import java.util.SortedSet;

import org.apache.log4j.Logger;
import org.pathwayeditor.businessobjects.drawingprimitives.IDrawingElement;
import org.pathwayeditor.businessobjects.impl.facades.DrawingElementFacade;
import org.pathwayeditor.figure.geometry.IConvexHull;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.visualeditor.controller.IDrawingElementController;
import org.pathwayeditor.visualeditor.controller.ILabelController;
import org.pathwayeditor.visualeditor.controller.INodeController;
import org.pathwayeditor.visualeditor.controller.IShapeController;
import org.pathwayeditor.visualeditor.selection.INodeSelection;
import org.pathwayeditor.visualeditor.selection.ISubgraphSelection;

public class CommonParentCalculator implements ICommonParentCalculator {
	private final Logger logger = Logger.getLogger(this.getClass());
	private final IIntersectionCalculator calc;
	private IDrawingElementController parent = null;
	private int numNodesAlreadyHaveParent = 0;
	private ISubgraphSelection selection = null;
	
	private interface IHandleLabel {
		
		IDrawingElementController getLabelParent(ILabelController node);
		
	}
	
	public CommonParentCalculator(IIntersectionCalculator calc){
		this.calc = calc;
	}
	
	/* (non-Javadoc)
	 * @see org.pathwayeditor.visualeditor.geometry.ICommonParentCalculator#findCommonParentExcludingLabels(org.pathwayeditor.visualeditor.selection.ISubgraphSelection, org.pathwayeditor.figure.geometry.Point)
	 */
	@Override
	public void findCommonParentExcludingLabels(ISubgraphSelection testSelection, Point delta){
		findCommonParentImpl(testSelection, delta, new IHandleLabel(){

			// we're ignoreing labels
			@Override
			public INodeController getLabelParent(ILabelController node) {
				return null;
			}
			
		});
	}
	
	private IDrawingElementController getNodeController(IDrawingElement node){
		return this.calc.getModel().getDrawingPrimitiveController(node);
	}
	
	/* (non-Javadoc)
	 * @see org.pathwayeditor.visualeditor.geometry.ICommonParentCalculator#findCommonParent(org.pathwayeditor.visualeditor.selection.ISubgraphSelection, org.pathwayeditor.figure.geometry.Point)
	 */
	@Override
	public void findCommonParent(ISubgraphSelection testSelection, Point delta) {
		findCommonParentImpl(testSelection, delta, new IHandleLabel(){

			// we're ignoreing labels
			@Override
			public IDrawingElementController getLabelParent(ILabelController node) {
				return getNodeController(new DrawingElementFacade(node.getDrawingElement().getGraphElement().getParent()));
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
				IDrawingElementController potentialParent = null;
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
					if (parent.equals(getNodeController(new DrawingElementFacade(node.getDrawingElement().getGraphElement().getParent())))) {
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
	@Override
	public ISubgraphSelection getSelectionToReparent(){
		return this.selection;
	}
	
	/* (non-Javadoc)
	 * @see org.pathwayeditor.visualeditor.geometry.ICommonParentCalculator#canReparentSelection()
	 */
	@Override
	public boolean canReparentSelection(){
		return this.numNodesAlreadyHaveParent == 0;
	}
	
	/* (non-Javadoc)
	 * @see org.pathwayeditor.visualeditor.geometry.ICommonParentCalculator#canMoveSelection()
	 */
	@Override
	public boolean canMoveSelection(){
		return this.numNodesAlreadyHaveParent == 0 || this.numNodesAlreadyHaveParent == this.selection.numTopDrawingNodes();
	}
	
	/* (non-Javadoc)
	 * @see org.pathwayeditor.visualeditor.geometry.ICommonParentCalculator#hasFoundCommonParent()
	 */
	@Override
	public boolean hasFoundCommonParent(){
		return this.parent != null;
	}
	
	@Override
	public IDrawingElementController getCommonParent(){
		return this.parent;
	}
	
	
	private INodeController findPotentialParent(final INodeController potentialChild, IConvexHull testPlacement){
		calc.setFilter(new IIntersectionCalcnFilter(){

			@Override
			public boolean accept(IDrawingElementController cont) {
				boolean retVal = false;
				if(cont instanceof INodeController){
					INodeController node = (INodeController)cont;
					retVal = node.getDrawingElement().getAttribute().getObjectType().getParentingRules().isValidChild(potentialChild.getDrawingElement().getAttribute().getObjectType());
					if(logger.isTraceEnabled()){
						logger.trace("Node=" + node +" canParent=" + retVal + ", potentialChild=" + potentialChild);
					}
				}
				return retVal;
			}
			
		});
		SortedSet<IDrawingElementController> nodes = calc.findIntersectingParentNodes(testPlacement, potentialChild);
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
