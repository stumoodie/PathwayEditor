package org.pathwayeditor.visualeditor.geometry;

import org.pathwayeditor.businessobjects.drawingprimitives.ILinkTerminus;
import org.pathwayeditor.businessobjects.drawingprimitives.attributes.LinkEndDecoratorShape;
import org.pathwayeditor.businessobjects.typedefn.ILinkTerminusDefaults;
import org.pathwayeditor.businessobjects.typedefn.ILinkTerminusDefinition;
import org.pathwayeditor.figure.geometry.Dimension;

public class GraphicalLinkTerminusDefinition implements IGraphicalLinkTerminusDefinition {
	private static final Dimension DEFAULT_END_SIZE = new Dimension(0, 0);
	private static final double DEFAULT_GAP = 0;
	private static final LinkEndDecoratorShape DEFAULT_END_DECORATOR_TYPE = LinkEndDecoratorShape.NONE;
	private Dimension endSize;
	private double gap;
	private LinkEndDecoratorShape endDecoratorType;
	
	public GraphicalLinkTerminusDefinition(ILinkTerminus terminus) {
		this.endSize = terminus.getEndSize();
		this.gap = terminus.getGap();
		this.endDecoratorType = terminus.getEndDecoratorType();
	}

	public GraphicalLinkTerminusDefinition(ILinkTerminusDefinition terminusDefn) {
		ILinkTerminusDefaults terminusDefaults = terminusDefn.getDefaultAttributes();
		this.endSize = terminusDefaults.getEndSize();
		this.gap = terminusDefaults.getGap();
		this.endDecoratorType = terminusDefaults.getEndDecoratorType();
	}

	public GraphicalLinkTerminusDefinition(){
		this.endSize = DEFAULT_END_SIZE;
		this.gap = DEFAULT_GAP;
		this.endDecoratorType = DEFAULT_END_DECORATOR_TYPE;
	}
	
	public GraphicalLinkTerminusDefinition(IGraphicalLinkTerminusDefinition other) {
		this.endDecoratorType = other.getEndDecoratorType();
		this.endSize = other.getEndSize();
		this.gap = other.getGap();
	}

	@Override
	public LinkEndDecoratorShape getEndDecoratorType() {
		return this.endDecoratorType;
	}

	@Override
	public Dimension getEndSize() {
		return this.endSize;
	}

	@Override
	public double getGap() {
		return this.gap;
	}

	@Override
	public void setEndSize(Dimension endSize) {
		this.endSize = endSize;
	}

	@Override
	public void setGap(double gap) {
		this.gap = gap;
	}

	@Override
	public void setEndDecoratorType(LinkEndDecoratorShape endDecoratorType) {
		this.endDecoratorType = endDecoratorType;
	}
}
