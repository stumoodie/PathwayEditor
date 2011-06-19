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
package org.pathwayeditor.graphicsengine;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.Arrays;
import java.util.EnumSet;

import org.apache.log4j.Logger;
import org.pathwayeditor.businessobjects.drawingprimitives.attributes.Colour;
import org.pathwayeditor.businessobjects.drawingprimitives.attributes.LineStyle;
import org.pathwayeditor.businessobjects.drawingprimitives.attributes.Colour;
import org.pathwayeditor.figure.rendering.GraphicalTextAlignment;
import org.pathwayeditor.figure.rendering.IFont;
import org.pathwayeditor.figure.rendering.IFont.Style;
import org.pathwayeditor.figure.rendering.IGraphicsEngine;

public class Java2DGraphicsEngine implements IGraphicsEngine {
	private final Logger logger = Logger.getLogger(this.getClass());
	private static final int MIN_LINE_WIDTH_PIXELS = 1;
	private static final double POINT_H = 1.0;
	private static final double POINT_W = 1.0;
	private static final String DEFAULT_FONT = "SansSerif";
	private Graphics2D g;
	private Colour fillColour = Colour.WHITE;
	private Colour lineColour = Colour.BLACK;
	private double lineWidth = MIN_LINE_WIDTH_PIXELS;
	private LineStyle lineStyle = LineStyle.SOLID;

	public Java2DGraphicsEngine(Graphics2D localG){
		this.g = localG;
	}

	public Java2DGraphicsEngine(){
	}

	public void setGraphics(Graphics2D g){
		this.g = g;
	}
 
	public Graphics2D getGraphics(){
		return this.g;
	}
	
	private void setColour(Colour col){
		Color awtCol = new Color(col.getRgb().getRed(), col.getRgb().getGreen(), col.getRgb().getBlue(), col.getAlpha());
		g.setColor(awtCol);
	}
	
	private void setLineColour(){
		if(this.lineColour != null){
			setColour(this.lineColour);
		}
	}
	
	private void setFillColour(){
		if(this.fillColour != null){
			setColour(this.fillColour);
		}
	}
	
	private void setStroke(){
		Stroke stroke = null;
		if(this.lineStyle.equals(LineStyle.SOLID)){
			stroke = new BasicStroke((float)this.lineWidth, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND);
		}
		else{
			float dash[] = new float[] {};
			if(this.lineStyle.equals(LineStyle.DASHED)){
				dash = new float[] { 10.0f };
			}
			else if(this.lineStyle.equals(LineStyle.DASH_DOT)){
				dash = new float[] { 10.0f, 10.0f, 3.0f, 3.0f };
			}
			else if(this.lineStyle.equals(LineStyle.DOT)){
				dash = new float[] { 2.0f, 4.0f };
			}
			else if(this.lineStyle.equals(LineStyle.DASH_DOT_DOT)){
				dash = new float[] { 10.0f, 10.0f, 3.0f, 3.0f, 3.0f, 3.0f };
			}
			stroke = new BasicStroke((float)this.lineWidth, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND, 10.0f, dash, 0.0f);
		}
			
		g.setStroke(stroke);
	}

	@Override
	public void drawArc(double pos, double pos2, double widthSize, double heightSize, double roundedOffset, double roundedLength) {
		this.setLineColour();
		this.setStroke();
		this.g.draw(new Arc2D.Double(pos, pos2, widthSize, heightSize, roundedOffset, roundedLength, Arc2D.OPEN));
		if(logger.isDebugEnabled()){
			logger.debug("drawArc: x=" + pos + ", y=" + pos2 + ", w=" + widthSize + ", h=" + heightSize + ",cw=" + roundedOffset + ",ch=" + roundedLength);
		}
	}

	@Override
	public void drawLine(double x1, double y1, double x2, double y2) {
		this.setLineColour();
		this.setStroke();
		this.g.draw(new Line2D.Double(x1, y1, x2, y2));
		if(logger.isDebugEnabled()){
			logger.debug("drawLine: x1=" + x1 + ", y1=" + y1 + ", x2=" + x2 + ", y2=" + y2);
		}
	}

	@Override
	public void drawOval(double x, double y, double w,	double h) {
		this.setLineColour();
		this.setStroke();
		g.draw(new Ellipse2D.Double(x, y, w, h));
		if(logger.isDebugEnabled()){
			logger.debug("drawOval: x=" + x + ", y=" + y + ", w=" + w + ", h=" + h);
		}
	}

	@Override
	public void drawPoint(double x, double y) {
		this.setLineColour();
		this.setStroke();
		g.draw(new Ellipse2D.Double(x, y, POINT_W, POINT_H));
		if(logger.isDebugEnabled()){
			logger.debug("drawPoint: x=" + x + ",y=" + y);
		}
	}

	@Override
	public void drawPolygon(double[] pointArr) {
		GeneralPath path = createPolygonPath(pointArr);
		this.setLineColour();
		this.setStroke();
		g.draw(path);
		if(logger.isDebugEnabled()){
			logger.debug("drawPolygon: points=" + Arrays.toString(pointArr));
		}
	}

	@Override
	public void drawPolyline(double[] pointArr) {
		GeneralPath path = createPolylinePath(pointArr);
		this.setLineColour();
		this.setStroke();
		g.draw(path);
		if(logger.isDebugEnabled()){
			logger.debug("drawPolyline: points=" + Arrays.toString(pointArr));
		}
	}

	@Override
	public void drawRectangle(double pos, double pos2, double widthSize, double heightSize) {
		handleRectangle(true, pos, pos2, widthSize, heightSize);
	}

	@Override
	public void drawString(String text, double x, double y, GraphicalTextAlignment align) {
		if (!text.isEmpty()) {
			this.setLineColour();
			this.setStroke();
			Font f = g.getFont();
			TextLayout layout = new TextLayout(text, f,
					g.getFontRenderContext());
			Rectangle2D textBounds = layout.getBounds();
			Point2D.Double p = getAlignedTextPosition(align, x, y, textBounds);
			p.setLocation(p.getX(), p.getY() + textBounds.getHeight());
			layout.draw(g, (float) p.getX(), (float) p.getY());
			if (logger.isDebugEnabled()) {
				logger.debug("drawString: x=" + x + ", y=" + y + ", text="
						+ text);
			}
		}
	}
	
	private Point2D.Double getAlignedTextPosition(final GraphicalTextAlignment alignment, double x, double y, Rectangle2D textExtents){
		Point2D.Double textPos = new Point2D.Double();
		switch(alignment){
		case C:
		case N:
		case S:
			// centre horizontally
			textPos.x = x - textExtents.getWidth()/2.0;
			break;
		case W:
		case NW:
		case SW:
			// left
			textPos.x = x;
			break;
		case E:
		case NE:
		case SE:
			// right
			textPos.x = x - textExtents.getWidth();
		}
		switch(alignment){
		case C:
		case W:
		case E:
			// middle vert
			textPos.y = y - textExtents.getHeight()/2.0;
			break;
		case N:
		case NW:
		case NE:
			// top
			textPos.y = y - textExtents.getHeight();
			break;
		case S:
		case SE:
		case SW:
			// bottom
			textPos.y = y;
		}
		return textPos;
	}
	

	@Override
	public void fillArc(double x, double y, double w, double h, double offset, double length) {
		this.setFillColour();
		this.g.fill(new Arc2D.Double(x, y, w, h, offset, length, Arc2D.PIE));
		if(logger.isDebugEnabled()){
			logger.debug("fillArc: x=" + x + ", y=" + y + ", w=" + w + ", h=" + h + ",cw=" + offset + ",ch=" + length);
		}
	}

	@Override
	public void fillOval(double x, double y, double w, double h) {
		this.setFillColour();
		g.fill(new Ellipse2D.Double(x, y, w, h));
		if(logger.isDebugEnabled()){
			logger.debug("fillOval: x=" + x + ", y=" + y + ", w=" + w + ", h=" + h);
		}
	}
	
	private GeneralPath createPolylinePath(double[] pointArr){
		GeneralPath path = new GeneralPath(Path2D.WIND_EVEN_ODD, pointArr.length/2);
		path.moveTo((float)pointArr[0], (float)pointArr[1]);
		for(int i = 2; i < pointArr.length;){
			path.lineTo((float)pointArr[i++], (float)pointArr[i++]);
		}
		return path;
	}

	private GeneralPath createPolygonPath(double[] pointArr){
		GeneralPath path = new GeneralPath(Path2D.WIND_EVEN_ODD, pointArr.length/2);
		path.moveTo((float)pointArr[0], (float)pointArr[1]);
		for(int i = 2; i < pointArr.length;){
			path.lineTo((float)pointArr[i++], (float)pointArr[i++]);
		}
		path.closePath();
		return path;
	}

	@Override
	public void fillPolygon(double[] pointArr) {
		GeneralPath path = createPolygonPath(pointArr);
		this.setFillColour();
		g.fill(path);
		if(logger.isDebugEnabled()){
			logger.debug("fillPolygon: points=" + Arrays.toString(pointArr));
		}
	}

	public void handleRectangle(boolean isLine, double x, double y, double w, double h){
		Rectangle2D rect = new Rectangle2D.Double(x, y, w, h);
		if(isLine){
			this.setLineColour();
			this.setStroke();
			g.draw(rect);
		}
		else{
			this.setFillColour();
			g.fill(rect);
		}
		if(logger.isDebugEnabled()){
			logger.debug("handleRectangle: fill=" + !isLine + ",x=" + x + ", y=" + y + ", w=" + w + ", h=" + h);
		}
	}
	
	@Override
	public void fillRectangle(double pos, double pos2, double widthSize, double heightSize) {
		handleRectangle(false, pos, pos2, widthSize, heightSize);
	}

	@Override
	public void fillRoundRectangle(double x, double y, double w, double h, double cw, double ch) {
		RoundRectangle2D rect = new RoundRectangle2D.Double(x, y, w, h, cw, ch);
		this.setFillColour();
		g.fill(rect);
		if(logger.isDebugEnabled()){
			logger.debug("fillRoundedRectangle: x=" + x + ", y=" + y + ", w=" + w + ", h=" + h + ",cw=" + cw + ",ch=" + ch);
		}
	}

	@Override
	public void drawRoundRectangle(double x, double y, double w, double h, double cw, double ch) {
		RoundRectangle2D rect = new RoundRectangle2D.Double(x, y, w, h, cw, ch);
		this.setLineColour();
		this.setStroke();
		g.draw(rect);
		if(logger.isDebugEnabled()){
			logger.debug("drawRoundedRectangle: x=" + x + ", y=" + y + ", w=" + w + ", h=" + h + ",cw=" + cw + ",ch=" + ch);
		}
	}

	@Override
	public void fillString(String text, double x, double y, GraphicalTextAlignment align) {
		Font f = g.getFont();
		this.setFillColour();
		this.setStroke();
		TextLayout layout = new TextLayout(text, f, g.getFontRenderContext());
		Rectangle2D textBounds = layout.getBounds();
		Point2D.Double p = getAlignedTextPosition(align, x, y, textBounds);
		Shape outline = layout.getOutline(null);
		p.setLocation(p.getX(), p.getY()+ textBounds.getHeight());
		AffineTransform origTransform = g.getTransform();
		AffineTransform transform = new AffineTransform(origTransform);
		transform.translate(p.getX(), p.getY());
		g.setTransform(transform);
		Area clipArea = new Area(textBounds);
		clipArea.subtract(new Area(outline));
		g.setClip(clipArea);
		g.fill(textBounds);
		g.setClip(null);
		g.setTransform(origTransform);
		if(logger.isDebugEnabled()){
			logger.debug("fillString: x=" + x + ", y=" + y + ", text=" + text);
		}
	}

	@Override
	public void setFillColor(Colour color) {
		if(color == null) throw new IllegalArgumentException("Cannot be null");
		this.fillColour = color;
		if(logger.isDebugEnabled()){
			logger.debug("setFillColor: color=" + this.fillColour);
		}
	}

	@Override
	public void setLineColor(Colour color) {
		if(color == null) throw new IllegalArgumentException("Cannot be null");
		this.lineColour = color;
		if(logger.isDebugEnabled()){
			logger.debug("setLineColor: color=" + this.lineColour);
		}
	}

	@Override
	public void setLineWidth(double lineWidthVal) {
		this.lineWidth = lineWidthVal;
		if(logger.isDebugEnabled()){
			logger.debug("setLineWidth: width=" + lineWidthVal);
		}
	}

	@Override
	public void setFont(IFont modifiedFont) {
		int fontSize = (int)Math.ceil(modifiedFont.getFontSize());
		Font f = new Font(DEFAULT_FONT, getFontStyle(modifiedFont.getStyle()), fontSize);
		g.setFont(f);
		if(logger.isDebugEnabled()){
			logger.debug("setFont: font=" + g.getFont());
		}
	}
	
	int getFontStyle(EnumSet<Style> styles){
		int retVal = Font.PLAIN;
		if(styles.contains(Style.ITALIC)){
			retVal |= Font.ITALIC;
		}
		if(styles.contains(Style.BOLD)){
			retVal |= Font.BOLD;
		}
		return retVal;
	}

	@Override
	public void setLineStyle(LineStyle style) {
		this.lineStyle = style;
	}

}
