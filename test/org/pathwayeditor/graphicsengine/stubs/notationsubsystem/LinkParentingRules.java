package org.pathwayeditor.graphicsengine.stubs.notationsubsystem;

import org.pathwayeditor.businessobjects.typedefn.ILinkObjectType;
import org.pathwayeditor.businessobjects.typedefn.IObjectType;
import org.pathwayeditor.businessobjects.typedefn.IObjectTypeParentingRules;

public class LinkParentingRules implements IObjectTypeParentingRules {
	private final ILinkObjectType objectType;
	
	public LinkParentingRules(ILinkObjectType objectType){
		this.objectType = objectType;
	}
	
	@Override
	public IObjectType getObjectType() {
		return this.objectType;
	}

	@Override
	public boolean isValidChild(IObjectType possibleChild) {
		return true;
	}

}
