package org.pathwayeditor.visualeditor.layout;

import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

import org.pathwayeditor.businessobjects.typedefn.ILabelObjectType;
import org.pathwayeditor.figure.geometry.Dimension;
import org.pathwayeditor.figure.geometry.Envelope;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.figure.rendering.IFigureRenderingController;
import org.pathwayeditor.visualeditor.geometry.ILinkPointDefinition;

public class LabelPositionCalculator implements ILabelPositionCalculator {
	private static final String FONT_NAME = "Arial";
//	private static final double DEFAULT_FONT_WIDTH = 8.5;
	private static final double DEFAULT_FONT_HEIGHT = 15.0;
//	private static ILabelPositionCalculator anInstance = null;
	
//	public static ILabelPositionCalculator getInstance(){
//		if(anInstance == null){
//			anInstance = new LabelPositionCalculator();
//		}
//		return anInstance;
//	}
	
	public LabelPositionCalculator(){
	}
	
//	public Envelope calculateLabelPosition(IShapeController shape, ILabelAttribute labelAtt){
//		Point newLocation = null;
//		IShapeLabelLocationPolicy policy = LabelLocationPolicyLookup.getInstance().getShapeLabelLocationPolicy(labelAtt.getObjectType());
//		policy.setOwningShape(shape);
//		newLocation = policy.nextLabelLocation();
//		return adjustForDefaultTextLength(newLocation, labelAtt.g);
//	}
	
	/* (non-Javadoc)
	 * @see org.pathwayeditor.visualeditor.layout.ILabelPositionCalculator#calculateLabelPosition(org.pathwayeditor.visualeditor.controller.IShapeController, org.pathwayeditor.businessobjects.typedefn.ILabelObjectType, java.lang.String)
	 */
	@Override
	public Envelope calculateLabelPosition(IFigureRenderingController shape, ILabelObjectType objectType, String text){
		IShapeLabelLocationPolicy policy = LabelLocationPolicyLookup.getInstance().getShapeLabelLocationPolicy(objectType);
		policy.setShapeFigure(shape);
		Point newLocation = policy.nextLabelLocation();
		return adjustForDefaultTextLength(newLocation, text);
	}
	
	/* (non-Javadoc)
	 * @see org.pathwayeditor.visualeditor.layout.ILabelPositionCalculator#calculateLabelPosition(org.pathwayeditor.visualeditor.controller.ILinkController, org.pathwayeditor.businessobjects.typedefn.ILabelObjectType, java.lang.String)
	 */
	@Override
	public Envelope calculateLabelPosition(ILinkPointDefinition shape, ILabelObjectType objectType, String text){
		Point newLocation = null;
		ILabelLocationPolicy policy = LabelLocationPolicyLookup.getInstance().getLinkLabelLocationPolicy(objectType);
		policy.setOwner(shape);
		newLocation = policy.nextLabelLocation();
		return adjustForDefaultTextLength(newLocation, text);
	}
	
	
	private Dimension handleGetTextBounds(String text) {
		double size = DEFAULT_FONT_HEIGHT;
		int style = Font.PLAIN;
    	Font f = new Font(FONT_NAME, style, (int)Math.ceil(size));
    	AffineTransform af = new AffineTransform();
    	FontRenderContext ctx = new FontRenderContext(af, false, false);
    	Rectangle2D bounds = f.getStringBounds(text, ctx);
		return new Dimension(bounds.getWidth(), bounds.getHeight());
	}

	private Envelope adjustForDefaultTextLength(Point centralPosition, String text){
		Dimension textExtent = handleGetTextBounds(text);
		double textWidth = textExtent.getWidth();
		double textHeight = textExtent.getHeight();
		return new Envelope(new Point(centralPosition.getX() - (textWidth/2), centralPosition.getY() - (textHeight/2)), textExtent);
	}
	
	
//	public Point adjustForTextLength(Point centralPosition, IAnnotationProperty prop, Graphics2D g){
//		 labelExtents = g..getTextExtents(prop.getValue().toString(), labelFigure.getFont());
//		Dimension labelSize = PixelConverter.getInstance().convertPixelToPointDimensions(labelExtents);
//		return new Point(centralPosition.getX() - (labelSize.getWidth()/2), centralPosition.getY() - (labelSize.getHeight()/2));
//	}
}
