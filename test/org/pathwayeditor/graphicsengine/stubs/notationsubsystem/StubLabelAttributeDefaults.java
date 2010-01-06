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

import org.pathwayeditor.businessobjects.drawingprimitives.attributes.LabelLocationPolicy;
import org.pathwayeditor.businessobjects.drawingprimitives.attributes.LineStyle;
import org.pathwayeditor.businessobjects.drawingprimitives.attributes.RGB;
import org.pathwayeditor.businessobjects.typedefn.ILabelAttributeDefaults;
import org.pathwayeditor.figure.figuredefn.GenericFont;
import org.pathwayeditor.figure.figuredefn.IFont;
import org.pathwayeditor.figure.geometry.Dimension;

/**
 * @author smoodie
 *
 */
public class StubLabelAttributeDefaults implements ILabelAttributeDefaults {
	public static final LineStyle LINE_STYLE = LineStyle.SOLID;
	public static final int LINE_WIDTH = 1;
	public static final RGB FILL_COLOUR = RGB.WHITE;
	public static final RGB LINE_COLOUR = RGB.BLACK;
	public static final Dimension SIZE = new Dimension(20, 10);

	/* (non-Javadoc)
	 * @see org.pathwayeditor.businessobjects.typedefn.ILabelAttributeDefaults#getFillColour()
	 */
	public RGB getFillColour() {
		return FILL_COLOUR;
	}

	/* (non-Javadoc)
	 * @see org.pathwayeditor.businessobjects.typedefn.ILabelAttributeDefaults#getLineColour()
	 */
	public RGB getLineColour() {
		return LINE_COLOUR;
	}

	/* (non-Javadoc)
	 * @see org.pathwayeditor.businessobjects.typedefn.ILabelAttributeDefaults#getLineStyle()
	 */
	public LineStyle getLineStyle() {
		return LINE_STYLE;
	}

	/* (non-Javadoc)
	 * @see org.pathwayeditor.businessobjects.typedefn.ILabelAttributeDefaults#getLineWidth()
	 */
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
	public Dimension getMinimumSize() {
		return new Dimension(10.0, 10.0);
	}

	/* (non-Javadoc)
	 * @see org.pathwayeditor.businessobjects.typedefn.ILabelAttributeDefaults#hasNoBorder()
	 */
	public boolean hasNoBorder() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.pathwayeditor.businessobjects.typedefn.ILabelAttributeDefaults#hasNoFill()
	 */
	public boolean hasNoFill() {
		return false;
	}

}
