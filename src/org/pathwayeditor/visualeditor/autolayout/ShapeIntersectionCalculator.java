package org.pathwayeditor.visualeditor.autolayout;

import org.apache.log4j.Logger;
import org.pathwayeditor.businessobjects.drawingprimitives.IShapeAttribute;
import org.pathwayeditor.figure.figuredefn.IAnchorLocator;
import org.pathwayeditor.figure.figuredefn.IFigureController;
import org.pathwayeditor.figure.geometry.Envelope;
import org.pathwayeditor.figure.geometry.IConvexHull;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.figure.geometry.Vector;
import org.pathwayeditor.visualeditor.controller.IFigureControllerHelper;
import org.pathwayeditor.visualeditor.controller.ShapeFigureControllerHelper;

import y.geom.YPoint;
import y.layout.IntersectionCalculator;
import y.layout.NodeLayout;

public class ShapeIntersectionCalculator implements IntersectionCalculator {
	private final Logger logger = Logger.getLogger(this.getClass());
	private final IShapeAttribute nodeCont;
	private final IFigureController figureController;

	public ShapeIntersectionCalculator(IShapeAttribute nodeCont){
		this.nodeCont = nodeCont;
		IFigureControllerHelper help = new ShapeFigureControllerHelper(nodeCont);
		help.createFigureController();
		this.figureController = help.getFigureController();
	}
	
	@Override
	public YPoint calculateIntersectionPoint(NodeLayout nl, double xOffset,	double yOffset, double dx, double dy) {
		Envelope shapeBounds = new Envelope(nl.getX(), nl.getY(), nl.getWidth(), nl.getHeight());
		IAnchorLocator locator = figureController.getAnchorLocatorFactory().createAnchorLocator(shapeBounds);
		Vector lineSegUnitVector = new Vector(-dx, -dy, 0);
		lineSegUnitVector = lineSegUnitVector.scale(Math.max(nl.getWidth(), nl.getHeight()));
		IConvexHull hull = figureController.getConvexHull().changeEnvelope(shapeBounds);
		Point hullCentre = hull.getCentre();
		Point otherPoint = hullCentre.translate(lineSegUnitVector.getIMagnitude(), lineSegUnitVector.getJMagnitude());
		locator.setOtherEndPoint(otherPoint);
		Point anchorPosn = locator.calcAnchorPosition();
		Point retVal = calcRelativeToCentre(hullCentre, anchorPosn);
		YPoint yRetVal = new YPoint(retVal.getX(), retVal.getY()); 
		if(logger.isTraceEnabled()){
			logger.trace("Intersection pt=" + yRetVal +" : abs=" + anchorPosn + " with shape:" + nodeCont);
			logger.trace("nl=" + shapeBounds + ",refPoint=" + otherPoint + ",hullCentre=" + hullCentre);
			logger.trace("nl=" + shapeBounds + ",xOffset=" + xOffset + " yOffset=" + yOffset + ",dx=" + dx + ",dy=" + dy);
		}
		return yRetVal;
	}

	private Point calcRelativeToCentre(Point centre, Point calcAnchorPosition) {
		return calcAnchorPosition.translate(centre.negate());
	}


}
