/**
 * 
 */
package org.pathwayeditor.visualeditor.geometry;

import org.pathwayeditor.visualeditor.controller.IDrawingPrimitiveController;


/**
 * @author smoodie
 *
 */
public interface IIntersectionCalcnFilter {

	boolean accept(IDrawingPrimitiveController node);

}
