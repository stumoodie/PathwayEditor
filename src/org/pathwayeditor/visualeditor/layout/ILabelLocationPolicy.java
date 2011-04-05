package org.pathwayeditor.visualeditor.layout;

import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.visualeditor.geometry.ILinkPointDefinition;

public interface ILabelLocationPolicy {

	ILinkPointDefinition getOwner();
	
	void setOwner(ILinkPointDefinition linkController);

	Point nextLabelLocation();

}
