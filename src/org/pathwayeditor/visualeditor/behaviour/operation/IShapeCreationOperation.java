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
package org.pathwayeditor.visualeditor.behaviour.operation;

import org.pathwayeditor.businessobjects.typedefn.INodeObjectType;
import org.pathwayeditor.figure.geometry.Point;

public interface IShapeCreationOperation<T extends INodeObjectType> {

//	void createShape(Point origin);

	/**
	 * Set shape object type of new shate to create.
	 * @param shapeType the new shape type.
	 */
	void setShapeObjectType(T shapeType);

	/**
	 * Gets the shape type currently set.
	 * @return current shape type, null if none set.
	 */
	T getShapeObjectType();

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
