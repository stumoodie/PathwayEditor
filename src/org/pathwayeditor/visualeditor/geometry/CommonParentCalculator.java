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
import org.pathwayeditor.businessobjects.drawingprimitives.IAnchorNodeAttribute;
import org.pathwayeditor.businessobjects.drawingprimitives.ICanvasElementAttributeVisitor;
import org.pathwayeditor.businessobjects.drawingprimitives.ILabelAttribute;
import org.pathwayeditor.businessobjects.drawingprimitives.ILinkAttribute;
import org.pathwayeditor.businessobjects.drawingprimitives.IRootAttribute;
import org.pathwayeditor.businessobjects.drawingprimitives.IShapeAttribute;
import org.pathwayeditor.figure.geometry.IConvexHull;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.visualeditor.controller.IDrawingElementController;
import org.pathwayeditor.visualeditor.controller.ILabelController;
import org.pathwayeditor.visualeditor.controller.INodeController;
import org.pathwayeditor.visualeditor.selection.INodeSelection;
import org.pathwayeditor.visualeditor.selection.ISubgraphSelection;

import uk.ac.ed.inf.graph.compound.ICompoundGraphElement;

public class CommonParentCalculator implements ICommonParentCalculator {
	private final Logger logger = Logger.getLogger(this.getClass());
	private final IIntersectionCalculator calc;
	private IDrawingElementController parent = null;
	private int numNodesAlreadyHaveParent = 0;
	private ISubgraphSelection selection = null;
	private IDrawingElementController potentialParent;
	
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
	
	private IDrawingElementController getNodeController(ICompoundGraphElement node){
		return this.calc.getModel().getController(node);
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
				return getNodeController(node.getAssociatedAttribute().getCurrentElement().getParent());
			}
			
		});
	}
	
	private void findCommonParentImpl(ISubgraphSelection testSelection, final Point delta, final IHandleLabel labelHandler) {
		if (this.selection == null || (!testSelection.equals(this.selection) ) ) {
			// if new or different selection to last one then recalculate the
			// common parent
			this.selection = testSelection;
			Iterator<INodeSelection> selectionIter = selection.topSelectedNodeIterator();
			parent = selection.getSelectionRecord().getPrimarySelection().getPrimitiveController().getViewModel().getRootNode();
			boolean firstTime = true;
			numNodesAlreadyHaveParent = 0;
			while (selectionIter.hasNext() && parent != null) {
				final INodeController node = selectionIter.next().getPrimitiveController();
				potentialParent = null;
				node.getAssociatedAttribute().visit(new ICanvasElementAttributeVisitor() {
					@Override
					public void visitShape(IShapeAttribute attribute) {
						IConvexHull hull = node.getConvexHull();
						IConvexHull newLocation = hull.translate(delta);
						potentialParent = findPotentialParent(node, newLocation);
					}
					@Override
					public void visitRoot(IRootAttribute attribute) {
					}
					@Override
					public void visitLink(ILinkAttribute attribute) {
					}
					@Override
					public void visitLabel(ILabelAttribute attribute) {
						potentialParent = labelHandler.getLabelParent((ILabelController)node);
					}
					@Override
					public void visitAnchorNode(IAnchorNodeAttribute anchorNodeAttribute) {
						IConvexHull hull = node.getConvexHull();
						IConvexHull newLocation = hull.translate(delta);
						potentialParent = findPotentialParent(node, newLocation);
					}
				});
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
					if (parent.equals(getNodeController(node.getAssociatedAttribute().getCurrentElement().getParent()))) {
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
	
	
	private IDrawingElementController findPotentialParent(final INodeController potentialChild, IConvexHull testPlacement){
		calc.setFilter(new IIntersectionCalcnFilter(){

			@Override
			public boolean accept(IDrawingElementController node) {
//			public boolean accept(IDrawingElementController cont) {
				boolean retVal = false;
//				if(cont instanceof INodeController){
//					INodeController node = (INodeController)cont;
					retVal = node.getAssociatedAttribute().getObjectType().getParentingRules().isValidChild(potentialChild.getAssociatedAttribute().getObjectType());
					if(logger.isTraceEnabled()){
						logger.trace("Node=" + node +" canParent=" + retVal + ", potentialChild=" + potentialChild);
					}
//				}
				return retVal;
			}
			
		});
		SortedSet<IDrawingElementController> nodes = calc.findIntersectingParentNodes(testPlacement, potentialChild);
//		INodeController retVal = null;
		IDrawingElementController retVal = null;
		if(logger.isTraceEnabled()){
			logger.trace("Potential parents = " + nodes);
		}
		if(!nodes.isEmpty()){
//			retVal = (INodeController)nodes.first();
			retVal = nodes.first();
		}
		return retVal;
	}
	
}
