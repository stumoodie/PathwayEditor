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
import org.pathwayeditor.businessobjects.typedefn.IShapeAttributeDefaults;
import org.pathwayeditor.figure.geometry.Dimension;
import org.pathwayeditor.figure.rendering.GenericFont;

/**
 * @author smoodie
 *
 */
public abstract class StubShapeAttributeDefaults implements IShapeAttributeDefaults {
	private static final GenericFont DEFAULT_FONT = new GenericFont();
	public   Colour    FILL_COLOUR = new Colour ( 100 , 100 , 100 ) ;
	public   Colour    LINE_COLOUR = new Colour ( 150 , 150 , 150 ) ;
	public   LineStyle LINE_STYLE = LineStyle.DASH_DOT ;     
	public   int LINE_WIDTH = 1 ;
	public   String PRIMITIVE_SHAPE_TYPE = "curbounds oval (C) setanchor";
	public   Dimension SIZE = new Dimension ( 50 , 50 ) ;
	
	
	@Override
	public String getShapeDefinition(){
		return PRIMITIVE_SHAPE_TYPE;
	}
	
	
	/* (non-Javadoc)
	 * @see org.pathwayeditor.businessobjects.typedefn.IShapeAttributeDefaults#getFillColour()
	 */
	@Override
	public Colour getFillColour() {
		return FILL_COLOUR;
	}

	/* (non-Javadoc)
	 * @see org.pathwayeditor.businessobjects.typedefn.IShapeAttributeDefaults#getLineColour()
	 */
	@Override
	public Colour getLineColour() {
		return LINE_COLOUR;
	}

	/* (non-Javadoc)
	 * @see org.pathwayeditor.businessobjects.typedefn.IShapeAttributeDefaults#getLineStyle()
	 */
	@Override
	public LineStyle getLineStyle() {
		return LINE_STYLE;
	}

	/* (non-Javadoc)
	 * @see org.pathwayeditor.businessobjects.typedefn.IShapeAttributeDefaults#getLineWidth()
	 */
	@Override
	public double getLineWidth() {
		return LINE_WIDTH;
	}


	/* (non-Javadoc)
	 * @see org.pathwayeditor.businessobjects.typedefn.IShapeAttributeDefaults#getSize()
	 */
	@Override
	public Dimension getSize() {
		return SIZE;
	}

	/* (non-Javadoc)
	 * @see org.pathwayeditor.businessobjects.typedefn.IShapeAttributeDefaults#propertyIterator()
	 */
	@Override
	public Iterator<IPropertyDefinition> propertyDefinitionIterator() {
		return getpropdefns().iterator();
	}

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
	
	@Override
	public IPropertyDefinition getPropertyDefinition(String name) {
		return findPropDefn(name);
	}

	@Override
	public int numPropertyDefinitions() {
		return this.getpropdefns().size();
	}

	/**
	 * @return
	 */
	protected abstract Set<IPropertyDefinition> getpropdefns() ;

	@Override
	public boolean containsPropertyDefinition(IPropertyDefinition propDefn){
		return propDefn != null && this.getpropdefns().contains(propDefn);
	}
	
	
	@Override
	public Colour getFontColour(){
		return LINE_COLOUR;
	}
	
	@Override
	public GenericFont getFont(){
		return DEFAULT_FONT;
	}
}
