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

import java.text.Format;

import org.pathwayeditor.businessobjects.drawingprimitives.attributes.LabelLocationPolicy;
import org.pathwayeditor.businessobjects.drawingprimitives.attributes.LineStyle;
import org.pathwayeditor.businessobjects.drawingprimitives.attributes.Colour;
import org.pathwayeditor.businessobjects.typedefn.ILabelAttributeDefaults;
import org.pathwayeditor.figure.geometry.Dimension;
import org.pathwayeditor.figure.rendering.GenericFont;
import org.pathwayeditor.figure.rendering.IFont;


/**
 * @author smoodie
 *
 */
public class StubLabelAttributeDefaults implements ILabelAttributeDefaults {
	public static final LineStyle LINE_STYLE = LineStyle.SOLID;
	public static final int LINE_WIDTH = 1;
	public static final Colour FILL_COLOUR = Colour.WHITE;
	public static final Colour LINE_COLOUR = Colour.BLACK;
	public static final Dimension SIZE = new Dimension(20, 10);

	/* (non-Javadoc)
	 * @see org.pathwayeditor.businessobjects.typedefn.ILabelAttributeDefaults#getFillColour()
	 */
	@Override
	public Colour getFillColour() {
		return FILL_COLOUR;
	}

	/* (non-Javadoc)
	 * @see org.pathwayeditor.businessobjects.typedefn.ILabelAttributeDefaults#getLineColour()
	 */
	@Override
	public Colour getLineColour() {
		return LINE_COLOUR;
	}

	/* (non-Javadoc)
	 * @see org.pathwayeditor.businessobjects.typedefn.ILabelAttributeDefaults#getLineStyle()
	 */
	@Override
	public LineStyle getLineStyle() {
		return LINE_STYLE;
	}

	/* (non-Javadoc)
	 * @see org.pathwayeditor.businessobjects.typedefn.ILabelAttributeDefaults#getLineWidth()
	 */
	@Override
	public double getLineWidth() {
		return LINE_WIDTH;
	}

	/* (non-Javadoc)
	 * @see org.pathwayeditor.businessobjects.typedefn.ILabelAttributeDefaults#getSize()
	 */
	public Dimension getSize() {
		return SIZE;
	}

	/* (non-Javadoc)
	 * @see org.pathwayeditor.businessobjects.typedefn.ILabelAttributeDefaults#getLabelLocationPolicy()
	 */
	@Override
	public LabelLocationPolicy getLabelLocationPolicy() {
		return LabelLocationPolicy.CENTRE;
	}

	/* (non-Javadoc)
	 * @see org.pathwayeditor.businessobjects.typedefn.ILabelAttributeDefaults#getFont()
	 */
	public IFont getFont() {
		return new GenericFont();
	}

	/* (non-Javadoc)
	 * @see org.pathwayeditor.businessobjects.typedefn.ILabelAttributeDefaults#getMinimumSize()
	 */
	@Override
	public Dimension getMinimumSize() {
		return new Dimension(10.0, 10.0);
	}

	/* (non-Javadoc)
	 * @see org.pathwayeditor.businessobjects.typedefn.ILabelAttributeDefaults#hasNoBorder()
	 */
	@Override
	public boolean hasNoBorder() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.pathwayeditor.businessobjects.typedefn.ILabelAttributeDefaults#hasNoFill()
	 */
	@Override
	public boolean hasNoFill() {
		return false;
	}

	@Override
	public Format getDisplayFormat() {
		return null;
	}

}
