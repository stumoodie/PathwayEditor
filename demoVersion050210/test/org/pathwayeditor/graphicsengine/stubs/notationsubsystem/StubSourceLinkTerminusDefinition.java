/*
Copyright 2009, Court of the University of Edinburgh

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License. 
*/
/**
 * 
 */
package org.pathwayeditor.graphicsengine.stubs.notationsubsystem;

import java.util.EnumSet;

import org.pathwayeditor.businessobjects.drawingprimitives.attributes.LinkTermType;
import org.pathwayeditor.businessobjects.typedefn.ILinkObjectType;
import org.pathwayeditor.businessobjects.typedefn.ILinkTerminusDefaults;
import org.pathwayeditor.businessobjects.typedefn.ILinkTerminusDefinition;

/**
 * @author smoodie
 *
 */
public class StubSourceLinkTerminusDefinition implements ILinkTerminusDefinition {
	private final ILinkTerminusDefaults sourceTermDefaults = new StubSourceTerminusDefaults();
	private final ILinkObjectType owningType;

	public StubSourceLinkTerminusDefinition(ILinkObjectType owningType){
		this.owningType = owningType;
	}
	
	/* (non-Javadoc)
	 * @see org.pathwayeditor.businessobjects.typedefn.ILinkTerminusDefinition#getEditableAttributes()
	 */
	public EnumSet<LinkTermEditableAttributes> getEditableAttributes() {
		return EnumSet.noneOf(LinkTermEditableAttributes.class);
	}

	/* (non-Javadoc)
	 * @see org.pathwayeditor.businessobjects.typedefn.ILinkTerminusDefinition#getLinkEndCode()
	 */
	public LinkTermType getLinkEndCode() {
		return LinkTermType.SOURCE;
	}

	/* (non-Javadoc)
	 * @see org.pathwayeditor.businessobjects.typedefn.ILinkTerminusDefinition#getLinkTerminusDefaults()
	 */
	public ILinkTerminusDefaults getDefaultAttributes() {
		return this.sourceTermDefaults ;
	}

	/* (non-Javadoc)
	 * @see org.pathwayeditor.businessobjects.typedefn.ILinkTerminusDefinition#getOwningObjectType()
	 */
	public ILinkObjectType getOwningObjectType() {
		return this.owningType;
	}

}
