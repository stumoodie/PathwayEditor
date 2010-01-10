/**
 * 
 */
package org.pathwayeditor.visualeditor.geometry;

import org.pathwayeditor.visualeditor.controller.INodePrimitive;


/**
 * @author smoodie
 *
 */
public interface IIntersectionCalcnFilter {

	boolean accept(INodePrimitive node);

}
