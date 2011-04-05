package org.pathwayeditor.visualeditor.layout;

import org.pathwayeditor.businessobjects.drawingprimitives.IShapeNode;
import org.pathwayeditor.figure.geometry.IConvexHull;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.figure.rendering.IFigureRenderingController;

public class ShapeCentreLabelPositionPolicy implements IShapeLabelLocationPolicy {
	private IFigureRenderingController shape;
	private IShapeNode shapeNode;
	
	public ShapeCentreLabelPositionPolicy(){
		this.shape = null;
	}
	
	@Override
	public IFigureRenderingController getShapeFigure() {
		return this.shape;
	}

	@Override
	public Point nextLabelLocation() {
		IConvexHull hull = this.shape.getConvexHull();
		return hull.getCentre();
	}

	@Override
	public void setShapeFigure(IFigureRenderingController shape) {
		this.shape = shape;
	}

	@Override
	public void setOwningShape(IShapeNode shapeNode) {
		this.shapeNode = shapeNode;
	}

	@Override
	public IShapeNode getOwningShape() {
		return this.shapeNode;
	}

}
