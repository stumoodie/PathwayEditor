package org.pathwayeditor.visualeditor.geometry;

import java.util.List;

import org.pathwayeditor.businessobjects.drawingprimitives.attributes.LinkEndDecoratorShape;
import org.pathwayeditor.figure.geometry.Dimension;

public interface IGraphicalLinkTerminusDefinition {

	double getGap();

	Dimension getEndSize();

	LinkEndDecoratorShape getEndDecoratorType();

	void setEndDecoratorType(LinkEndDecoratorShape endDecoratorType);

	void setGap(double gap);

	void setEndSize(Dimension endSize);
	
	void addDefinitionChangeListener(IGraphicalLinkTerminusDefinitionChangeListener l);

	void removeDefinitionChangeListener(IGraphicalLinkTerminusDefinitionChangeListener l);
	
	List<IGraphicalLinkTerminusDefinitionChangeListener> getDefinitionChangeListeners();
}
