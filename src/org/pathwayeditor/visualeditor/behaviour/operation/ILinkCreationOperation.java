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

import org.pathwayeditor.businessobjects.typedefn.ILinkObjectType;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.visualeditor.controller.IDrawingElementController;

public interface ILinkCreationOperation {

	void startCreation(Point startPosn);

	void setLinkObjectType(ILinkObjectType currentLinkType);

	ILinkObjectType getLinkObjectType();

	void creationOngoing(Point lastDelta);

	void finishCreation();

	void setPotentialTarget(IDrawingElementController potentialTarget);

	boolean isLinkCreationStarted();

	boolean canFinishCreation();

	void setPotentialSourceNode(IDrawingElementController potentialSource);

	boolean canStartCreation();

	boolean canCreateIntermediatePoint(Point intermediatePoint);

	void createIntermediatePoint(Point intermediatePoint);

	void cancel();

}
