/**
 * 
 */
package org.pathwayeditor.visualeditor.geometry;

import org.pathwayeditor.visualeditor.controller.INodeController;


/**
 * @author smoodie
 *
 */
public interface IIntersectionCalcnFilter {

	boolean accept(INodeController node);

}
