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

/**
 * 
 */
package org.pathwayeditor.graphicsengine.stubs.notationsubsystem;

import java.util.Iterator;
import java.util.Set;

import org.pathwayeditor.businessobjects.drawingprimitives.attributes.Colour;
import org.pathwayeditor.businessobjects.drawingprimitives.attributes.LineStyle;
import org.pathwayeditor.businessobjects.drawingprimitives.properties.IPropertyDefinition;
import org.pathwayeditor.businessobjects.typedefn.ILabelAttributeDefaults;
import org.pathwayeditor.businessobjects.typedefn.ILinkAttributeDefaults;
import org.pathwayeditor.businessobjects.typedefn.ILinkTerminusDefaults;

/**
 * @author smoodie
 *
 */
public abstract class StubLinkAttributeDefaults implements ILinkAttributeDefaults {
	public static final int EXPECTED_DEFAULT_LINE_WIDTH = 1;
	public static final Colour EXPECTED_DEFAULT_LINE_COLOUR = new Colour(4,5, 6);
	public static final LineStyle EXPECTED_DEFAULT_LINE_STYLE = LineStyle.SOLID;

//	private  ILinkTerminusDefaults sourceTermDefaults = new StubSourceTerminusDefaults();
//	private  ILinkTerminusDefaults targetTermDefaults = new StubTargetTerminusDefaults();

	/* (non-Javadoc)
	 * @see org.pathwayeditor.businessobjects.typedefn.IShapeAttributeDefaults#getDefaultLabelAttributes()
	 */
	public ILabelAttributeDefaults getDefaultLabelAttributes() {
		throw new UnsupportedOperationException("not implemented");
	}

	/* (non-Javadoc)
	 * @see org.pathwayeditor.businessobjects.typedefn.IShapeAttributeDefaults#getLineColour()
	 */
	@Override
	public Colour getLineColour() {
		return EXPECTED_DEFAULT_LINE_COLOUR;
	}

	/* (non-Javadoc)
	 * @see org.pathwayeditor.businessobjects.typedefn.IShapeAttributeDefaults#getLineStyle()
	 */
	@Override
	public LineStyle getLineStyle() {
		return EXPECTED_DEFAULT_LINE_STYLE;
	}

	/* (non-Javadoc)
	 * @see org.pathwayeditor.businessobjects.typedefn.IShapeAttributeDefaults#getLineWidth()
	 */
	@Override
	public double getLineWidth() {
		return EXPECTED_DEFAULT_LINE_WIDTH;
	}

	/* (non-Javadoc)
	 * @see org.pathwayeditor.businessobjects.typedefn.IDefaultLinkAttributes#getLinkSource()
	 */
	public ILinkTerminusDefaults getLinkSource() {
		throw new UnsupportedOperationException("not implemented");
	}

	/* (non-Javadoc)
	 * @see org.pathwayeditor.businessobjects.typedefn.IDefaultLinkAttributes#getLinkTarget()
	 */
	public ILinkTerminusDefaults getLinkTarget() {
		throw new UnsupportedOperationException("not implemented");
	}


	/* (non-Javadoc)
	 * @see org.pathwayeditor.businessobjects.typedefn.ILinkAttributeDefaults#propertyIterator()
	 */
	@Override
	public Iterator<IPropertyDefinition> propertyDefinitionIterator() {
		return getpropdefns().iterator();
	}

	/**
	 * @return
	 */
	protected abstract Set<IPropertyDefinition> getpropdefns() ;
	
	/* (non-Javadoc)
	 * @see org.pathwayeditor.businessobjects.typedefn.IPropertyDefinitionContainer#containsPropertyDefinition(java.lang.String)
	 */
	@Override
	public boolean containsPropertyDefinition(String name) {
		return findPropDefn(name) != null;
	}

	IPropertyDefinition findPropDefn(String name){
		IPropertyDefinition retVal = null;
		for(IPropertyDefinition propDefn : this.getpropdefns()){
			if(propDefn.getName().equals(name)){
				retVal = propDefn;
				break;
			}
		}
		return retVal;
	}
	
	/* (non-Javadoc)
	 * @see org.pathwayeditor.businessobjects.typedefn.IPropertyDefinitionContainer#getPropertyDefinition(java.lang.String)
	 */
	@Override
	public IPropertyDefinition getPropertyDefinition(String name) {
		return findPropDefn(name);
	}

	/* (non-Javadoc)
	 * @see org.pathwayeditor.businessobjects.typedefn.IPropertyDefinitionContainer#numPropertyDefinitions()
	 */
	@Override
	public int numPropertyDefinitions() {
		return this.getpropdefns().size();
	}
	
	@Override
	public boolean containsPropertyDefinition(IPropertyDefinition propDefn){
		return propDefn != null && this.getpropdefns().contains(propDefn);
	}
}
