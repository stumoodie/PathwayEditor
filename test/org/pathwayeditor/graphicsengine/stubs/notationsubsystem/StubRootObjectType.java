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

import org.pathwayeditor.businessobjects.notationsubsystem.INotationSyntaxService;
import org.pathwayeditor.businessobjects.typedefn.IObjectType;
import org.pathwayeditor.businessobjects.typedefn.IRootObjectParentingRules;
import org.pathwayeditor.businessobjects.typedefn.IRootObjectType;

/**
 * @author smoodie
 *
 */
public class StubRootObjectType implements IRootObjectType {
	private static final int UNIQUE_ID = 0;
	public static final String DESCRIPTION = "Root Object Type Description";
	public static final String NAME = "Root Object Type";
	private final INotationSyntaxService syntaxService;
	
	/**
	 * 
	 */
	public StubRootObjectType(INotationSyntaxService syntaxService) {
		this.syntaxService = syntaxService;
	}

	/* (non-Javadoc)
	 * @see org.pathwayeditor.businessobjects.typedefn.IRootObjectType#getParentingRules()
	 */
	@Override
	public IRootObjectParentingRules getParentingRules() {
		return new RootObjectParentingRules();
	}

	/* (non-Javadoc)
	 * @see org.pathwayeditor.businessobjects.typedefn.IRootObjectType#getUniqueId()
	 */
	@Override
	public int getUniqueId() {
		return UNIQUE_ID;
	}

	/* (non-Javadoc)
	 * @see org.pathwayeditor.businessobjects.typedefn.IObjectType#getDescription()
	 */
	@Override
	public String getDescription() {
		return DESCRIPTION;
	}

	/* (non-Javadoc)
	 * @see org.pathwayeditor.businessobjects.typedefn.IObjectType#getName()
	 */
	@Override
	public String getName() {
		return NAME;
	}

	/* (non-Javadoc)
	 * @see org.pathwayeditor.businessobjects.typedefn.IObjectType#getSyntaxService()
	 */
	@Override
	public INotationSyntaxService getSyntaxService() {
		return this.syntaxService;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(IObjectType o) {
		return this.getUniqueId() < o.getUniqueId() ? -1 : this.getUniqueId() > o.getUniqueId() ? 1 : 0;
	}

	private class RootObjectParentingRules implements IRootObjectParentingRules {

		/* (non-Javadoc)
		 * @see org.pathwayeditor.businessobjects.typedefn.IRootObjectParentingRules#getObjectType()
		 */
		@Override
		public IRootObjectType getObjectType() {
			return StubRootObjectType.this;
		}

		/* (non-Javadoc)
		 * @see org.pathwayeditor.businessobjects.typedefn.IObjectTypeParentingRules#isValidChild(org.pathwayeditor.businessobjects.typedefn.IObjectType)
		 */
		@Override
		public boolean isValidChild(IObjectType possibleChild) {
			boolean retVal = false;
			if(possibleChild.getUniqueId() == StubShapeAObjectType.UNIQUE_ID
					|| possibleChild.getUniqueId() == StubShapeBObjectType.UNIQUE_ID
					|| possibleChild.getUniqueId() == StubShapeCObjectType.UNIQUE_ID
					|| possibleChild.getUniqueId() == StubShapeDObjectType.UNIQUE_ID){
				retVal = true; 
			}
			return retVal;
		}
		
	}
}
