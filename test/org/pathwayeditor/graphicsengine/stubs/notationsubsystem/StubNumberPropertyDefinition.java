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

import java.math.BigDecimal;

import org.pathwayeditor.businessobjects.drawingprimitives.properties.IAnnotationProperty;
import org.pathwayeditor.businessobjects.drawingprimitives.properties.INumberAnnotationProperty;
import org.pathwayeditor.businessobjects.drawingprimitives.properties.INumberPropertyDefinition;
import org.pathwayeditor.businessobjects.drawingprimitives.properties.IPropertyBuilder;
import org.pathwayeditor.businessobjects.drawingprimitives.properties.IPropertyDefinition;

/**
 * @author smoodie
 *
 */
public class StubNumberPropertyDefinition implements INumberPropertyDefinition {
	public static final BigDecimal DEFAULT_VALUE = new BigDecimal(2468);
	public static final String NAME = "NumberProperty";
	public static final boolean IS_EDITABLE = false;
//	public static final boolean IS_VISUALISABLE = true;

//	private final ILabelAttributeDefaults labelDefaults;

	public StubNumberPropertyDefinition(){
//		this.labelDefaults = new StubLabelAttributeDefaults();
	}
	
	/* (non-Javadoc)
	 * @see org.pathwayeditor.businessobjects.drawingprimitives.properties.IPropertyDefinition#copyProperty(org.pathwayeditor.businessobjects.drawingprimitives.properties.IPropertyBuilder)
	 */
	@Override
	public IAnnotationProperty copyProperty(IPropertyBuilder propertyBuilder, IAnnotationProperty otherProp) {
		return propertyBuilder.copyNumberProperty((INumberAnnotationProperty)otherProp);
	}

	/* (non-Javadoc)
	 * @see org.pathwayeditor.businessobjects.drawingprimitives.properties.IPropertyDefinition#createProperty(org.pathwayeditor.businessobjects.drawingprimitives.properties.IPropertyBuilder)
	 */
	@Override
	public IAnnotationProperty createProperty(IPropertyBuilder propertyBuilder) {
		return propertyBuilder.createNumberProperty(this);
	}

	/* (non-Javadoc)
	 * @see org.pathwayeditor.businessobjects.drawingprimitives.properties.IPropertyDefinition#getDefaultValue()
	 */
	@Override
	public BigDecimal getDefaultValue() {
		return DEFAULT_VALUE;
	}

//	/* (non-Javadoc)
//	 * @see org.pathwayeditor.businessobjects.drawingprimitives.properties.IPropertyDefinition#getLabelDefaults()
//	 */
//	@Override
//	public ILabelAttributeDefaults getLabelDefaults() {
//		return this.labelDefaults;
//	}

	/* (non-Javadoc)
	 * @see org.pathwayeditor.businessobjects.drawingprimitives.properties.IPropertyDefinition#getName()
	 */
	@Override
	public String getName() {
		return NAME;
	}

	/* (non-Javadoc)
	 * @see org.pathwayeditor.businessobjects.drawingprimitives.properties.IPropertyDefinition#isEditable()
	 */
	@Override
	public boolean isEditable() {
		return IS_EDITABLE;
	}

//	/* (non-Javadoc)
//	 * @see org.pathwayeditor.businessobjects.drawingprimitives.properties.IPropertyDefinition#isVisualisable()
//	 */
//	@Override
//	public boolean isVisualisable() {
//		return IS_VISUALISABLE;
//	}

//	/* (non-Javadoc)
//	 * @see org.pathwayeditor.businessobjects.drawingprimitives.properties.IPropertyDefinition#isDisplayed()
//	 */
//	@Override
//	public boolean isAlwaysDisplayed() {
//		return false;
//	}

	@Override
	public String getDisplayName() {
		return NAME;
	}
	

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(IPropertyDefinition o) {
		return this.getName().compareTo(o.getName());
	}

	@Override
	public String toString(){
		StringBuilder builder = new StringBuilder(this.getClass().getSimpleName());
		builder.append("(");
		builder.append("name=");
		builder.append(this.getName());
		builder.append(")");
		return builder.toString();
	}
}
