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

package org.pathwayeditor.visualeditor.layout;

import org.pathwayeditor.businessobjects.drawingprimitives.ILabelAttribute;
import org.pathwayeditor.businessobjects.drawingprimitives.IShapeAttribute;
import org.pathwayeditor.figure.geometry.Envelope;
import org.pathwayeditor.figure.geometry.IConvexHull;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.figure.rendering.IFigureRenderingController;

import uk.ac.ed.inf.graph.compound.ICompoundNode;
import uk.ac.ed.inf.graph.util.IFilterCriteria;
import uk.ac.ed.inf.graph.util.impl.FilteredIterator;


/**
 * Places labels at compass points surrounding the bounding box of the 
 * map object to be labelled. Once  the initial 8 points have been filled,
 * labels are placed further out from the shape to be labelled.
 * If a laid out label is subsequently moved further  away from its original position
 * than getDefaultTolerance(), the position is regarded as free and can be occupied by new labels.
 * 
 * The initial default Tolerance is 2 pixels.
 * 
 * @author Richard Adams/Stuart Moodie
 *
 */
public class CompassLabelPositionPolicy implements IShapeLabelLocationPolicy {
    /**
     * Inner interface defines algorithms to place labels at particular locations
     * @author Richard Adams/Stuart Moodie
     *
     */
    private interface ILabelCommand {
		Point getLabelLocation(IConvexHull hull, double multiplier);
	}
    private static final int MAX_MULTIPLIER = 200;
	private static final int INIT_MULTIPLIER = 1;
	private static int X_OFFSET = 20;
    private static int Y_OFFSET = 20;
    
    // if a label is within this distance of calculated location, is considered
    // to be at that location
    private double tolerance = 2.0;
 	private final ILabelCommand []  loci;
	private final ILabelCommand N,E, W, S, NE, SE, SW, NW;
	private IFigureRenderingController attribute = null;
	private IShapeAttribute shapeNode;
	

	public CompassLabelPositionPolicy(){
		N = new ILabelCommand (){
			@Override
			public Point getLabelLocation(IConvexHull hull, double multiplier) {
				Envelope b = hull.getEnvelope();
				return new Point(b.getOrigin().getX() + b.getDimension().getWidth()/2, 
						            b.getOrigin().getY() - Y_OFFSET *multiplier); 
			}
			
		};
		
		E = new ILabelCommand (){
			@Override
			public Point getLabelLocation(IConvexHull hull, double multiplier) {
				Envelope b = hull.getEnvelope();
				return new Point(b.getHorizontalCorner().getX() + X_OFFSET*multiplier, 
						            b.getOrigin().getY() + b.getDimension().getHeight()/2); 
			}
			
		};
		
		S = new ILabelCommand (){
			@Override
			public Point getLabelLocation(IConvexHull hull, double multiplier) {
				Envelope b = hull.getEnvelope();
				return new Point(b.getOrigin().getX() + b.getDimension().getWidth()/2, 
						            b.getVerticalCorner().getY() + Y_OFFSET*multiplier); 
			}
		};
		W = new ILabelCommand (){
				@Override
				public Point getLabelLocation(IConvexHull hull, double multiplier) {
					Envelope b = hull.getEnvelope();
					return new Point(b.getOrigin().getX() - X_OFFSET*multiplier, 
							            b.getOrigin().getY() + b.getDimension().getHeight()/2); 
				}
			
		};
		NE = new ILabelCommand (){
			@Override
			public Point getLabelLocation(IConvexHull hull, double multiplier) {
				Envelope b = hull.getEnvelope();
				return b.getHorizontalCorner().translate((X_OFFSET/2)*multiplier, - (Y_OFFSET/2)*multiplier); 
			}
			
		};
		
		SE = new ILabelCommand (){
			@Override
			public Point getLabelLocation(IConvexHull hull, double multiplier) {
				Envelope b = hull.getEnvelope();
				return b.getDiagonalCorner().translate((X_OFFSET/2)*multiplier, (Y_OFFSET/2)*multiplier); 
			}
			
		};
		
		SW = new ILabelCommand (){
			@Override
			public Point getLabelLocation(IConvexHull hull, double multiplier) {
				Envelope b = hull.getEnvelope();
				return b.getVerticalCorner().translate(-(X_OFFSET/2)*multiplier, (Y_OFFSET/2)*multiplier);
			}
			
		};
		
		NW = new ILabelCommand (){
			@Override
			public Point getLabelLocation(IConvexHull hull, double multiplier) {
				Envelope b = hull.getEnvelope();
				return b.getOrigin().translate(-(X_OFFSET/2)*multiplier, -(Y_OFFSET/2)*multiplier );
			}
			
		};
		this.loci  = new ILabelCommand[]{N, E, W, S, NE, SE,SW, NW};
	}
	
	@Override
	public Point nextLabelLocation(){
		Point retVal = null;
		for(int m = INIT_MULTIPLIER; m < MAX_MULTIPLIER && retVal == null; m++){
			for (int i = 0; i< loci.length && retVal == null;i++) {
				IConvexHull hull = attribute.getConvexHull();
				Point loc= loci[i].getLabelLocation(hull, m);
				if(isNewLocation(loc)){
					retVal = loc;
				}
			}
		}
		return retVal;
	}
	
	
	
	private boolean isNewLocation(Point newloc) {
		FilteredIterator<ICompoundNode> iter = new FilteredIterator<ICompoundNode>(this.shapeNode.getCurrentElement().getChildCompoundGraph().nodeIterator(),
				new IFilterCriteria<ICompoundNode>() {
			@Override
			public boolean matched(ICompoundNode testObj) {
				return testObj.getAttribute() instanceof ILabelAttribute;
			}
		});
//		Iterator<ICompoundNode> iter = new SubModelFacade(this.shapeNode.getGraphElement().getChildCompoundGraph()).labelIterator();
		boolean retVal = true;
		while(iter.hasNext() && retVal){
			ILabelAttribute att = ((ILabelAttribute)iter.next().getAttribute());
			Point labLoc = att.getBounds().getOrigin();
			if(labLoc.getDistance(newloc) < getTolerance()) {
				retVal = false;
			}
		}
		return retVal;
	}

	private double getTolerance() {
		return tolerance;
	}

	@Override
	public IFigureRenderingController getShapeFigure() {
		return this.attribute;
	}

	@Override
	public void setShapeFigure(IFigureRenderingController shape) {
		this.attribute = shape;
	}

	@Override
	public void setOwningShape(IShapeAttribute shapeNode) {
		this.shapeNode = shapeNode;
	}

	@Override
	public IShapeAttribute getOwningShape() {
		return this.shapeNode;
	}

}
