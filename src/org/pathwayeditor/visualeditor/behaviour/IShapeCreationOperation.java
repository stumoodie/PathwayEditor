package org.pathwayeditor.visualeditor.behaviour;

import org.pathwayeditor.businessobjects.typedefn.IShapeObjectType;
import org.pathwayeditor.figure.geometry.Point;

public interface IShapeCreationOperation {

//	void createShape(Point origin);

	/**
	 * Set shape object type of new shate to create.
	 * @param shapeType the new shape type.
	 */
	void setShapeObjectType(IShapeObjectType shapeType);

	/**
	 * Gets the shape type currently set.
	 * @return current shape type, null if none set.
	 */
	IShapeObjectType getShapeObjectType();

	/**
	 * Start creation drag.
	 * @param location staring position of creation drag.
	 */
	void startCreationDrag(Point location);

	/**
	 * Drag is continuing.
	 * @param delta the displacement if the current mouse posn from when the drag started.
	 */
	void ongoingCreationDrag(Point delta);

	/**
	 * Drag has completed.
	 * @param delta the displacement if the current mouse posn from when the drag started.
	 */
	void finishCreationDrag(Point delta);
	
	/**
	 * Tests if creation will succeed with this operation in its current state.
	 * @return true if it will, false otherwise.
	 */
	boolean canCreationSucceed();
}
