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

import org.pathwayeditor.businessobjects.notationsubsystem.INotationSyntaxService;
import org.pathwayeditor.businessobjects.typedefn.ILinkAttributeDefaults;
import org.pathwayeditor.businessobjects.typedefn.ILinkConnectionRules;
import org.pathwayeditor.businessobjects.typedefn.ILinkObjectType;
import org.pathwayeditor.businessobjects.typedefn.ILinkTerminusDefinition;
import org.pathwayeditor.businessobjects.typedefn.IObjectType;
import org.pathwayeditor.businessobjects.typedefn.IObjectTypeParentingRules;
import org.pathwayeditor.businessobjects.typedefn.IShapeObjectType;

/**
 * @author smoodie
 *
 */
public class StubLinkAObjectType implements ILinkObjectType {
	private static final EnumSet<LinkEditableAttributes> EDITABLE_ATTRIBUTES = EnumSet.of(LinkEditableAttributes.COLOUR);
	public static final int UNIQUE_ID = 5;
	public static final String DESCRIPTION = "Link A Object Type";
	public static final String NAME = "Link A";
	private final INotationSyntaxService syntaxService;
	private final ILinkAttributeDefaults linkAttributeDefaults;
	private final ILinkConnectionRules connectionRules;
	private final ILinkTerminusDefinition sourceTerminusDefn;
	private final ILinkTerminusDefinition targetTerminusDefn;
	private final int id = UNIQUE_ID;
	
	/**
	 * @param stubNotationSyntaxService
	 */
	public StubLinkAObjectType(INotationSyntaxService stubNotationSyntaxService) {
		this.syntaxService = stubNotationSyntaxService;
		this.linkAttributeDefaults = new StubLinkAttributeDefaultsWithRichText();
		this.sourceTerminusDefn = new StubSourceLinkTerminusDefinition(this);
		this.targetTerminusDefn = new StubTargetLinkTerminusDefinition(this);
		this.connectionRules = new ILinkConnectionRules(){

			@Override
			public ILinkObjectType getLinkObjectType() {
				return StubLinkAObjectType.this;
			}

			@Override
			public boolean isValidSource(IShapeObjectType source) {
				return StubShapeAObjectType.UNIQUE_ID == source.getUniqueId();
			}

			@Override
			public boolean isValidTarget(IShapeObjectType source, IShapeObjectType target) {
				return isValidSource(source)
					&& (StubShapeAObjectType.UNIQUE_ID == target.getUniqueId()
							|| StubShapeBObjectType.UNIQUE_ID == target.getUniqueId()
								|| StubShapeCObjectType.UNIQUE_ID == target.getUniqueId());
			}
			
		};
	}

	/* (non-Javadoc)
	 * @see org.pathwayeditor.businessobjects.typedefn.ILinkObjectType#getDefaultLinkAttributes()
	 */
	@Override
	public ILinkAttributeDefaults getDefaultAttributes() {
		return this.linkAttributeDefaults;
	}

	/* (non-Javadoc)
	 * @see org.pathwayeditor.businessobjects.typedefn.ILinkObjectType#getEditiableAttributes()
	 */
	@Override
	public EnumSet<LinkEditableAttributes> getEditableAttributes() {
		return EDITABLE_ATTRIBUTES;
	}

	/* (non-Javadoc)
	 * @see org.pathwayeditor.businessobjects.typedefn.ILinkObjectType#getLinkConnectionRules()
	 */
	@Override
	public ILinkConnectionRules getLinkConnectionRules() {
		return connectionRules;
	}

	/* (non-Javadoc)
	 * @see org.pathwayeditor.businessobjects.typedefn.ILinkObjectType#getSourceTerminusDefinition()
	 */
	@Override
	public ILinkTerminusDefinition getSourceTerminusDefinition() {
		return this.sourceTerminusDefn;
	}

	/* (non-Javadoc)
	 * @see org.pathwayeditor.businessobjects.typedefn.ILinkObjectType#getTargetTerminusDefinition()
	 */
	@Override
	public ILinkTerminusDefinition getTargetTerminusDefinition() {
		return this.targetTerminusDefn;
	}

	/* (non-Javadoc)
	 * @see org.pathwayeditor.businessobjects.typedefn.ILinkObjectType#getUniqueId()
	 */
	@Override
	public int getUniqueId() {
		return this.id;
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

	@Override
	public IObjectTypeParentingRules getParentingRules() {
		return new LinkParentingRules(this);
	}

}
