package org.pathwayeditor.visualeditor.geometry;

import org.pathwayeditor.businessobjects.drawingprimitives.attributes.LinkEndDecoratorShape;
import org.pathwayeditor.figure.geometry.Dimension;

public interface IGraphicalLinkTerminusDefinition {

	double getGap();

	Dimension getEndSize();

	LinkEndDecoratorShape getEndDecoratorType();

	void setEndDecoratorType(LinkEndDecoratorShape endDecoratorType);

	void setGap(double gap);

	void setEndSize(Dimension endSize);

}
