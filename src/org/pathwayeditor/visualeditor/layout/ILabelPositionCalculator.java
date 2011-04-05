package org.pathwayeditor.visualeditor.layout;

import org.pathwayeditor.businessobjects.typedefn.ILabelObjectType;
import org.pathwayeditor.figure.geometry.Envelope;
import org.pathwayeditor.figure.rendering.IFigureRenderingController;
import org.pathwayeditor.visualeditor.geometry.ILinkPointDefinition;

public interface ILabelPositionCalculator {

	public abstract Envelope calculateLabelPosition(IFigureRenderingController shape,
			ILabelObjectType objectType, String text);

	public abstract Envelope calculateLabelPosition(ILinkPointDefinition shape,
			ILabelObjectType objectType, String text);

}