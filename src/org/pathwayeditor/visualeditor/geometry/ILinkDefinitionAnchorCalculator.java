package org.pathwayeditor.visualeditor.geometry;

import org.pathwayeditor.figure.rendering.IAnchorLocator;

public interface ILinkDefinitionAnchorCalculator {

	ILinkPointDefinition getLinkDefinition();
	
	IAnchorLocator getSrcLocator();

	void setSrcLocation(IAnchorLocator anchorLocator);
	
	IAnchorLocator getTgtLocator();
	
	void setTgtLocation(IAnchorLocator anchorLocator);
	
	void recalculateSrcAnchor();
	
	void recalculateTgtAnchor();
	
	void recalculateBothAnchors();
}
