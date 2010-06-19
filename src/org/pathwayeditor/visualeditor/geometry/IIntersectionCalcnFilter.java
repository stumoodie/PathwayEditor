/**
 * 
 */
package org.pathwayeditor.visualeditor.geometry;

import org.pathwayeditor.visualeditor.controller.IDrawingElementController;


/**
 * @author smoodie
 *
 */
public interface IIntersectionCalcnFilter {

	boolean accept(IDrawingElementController node);

}
