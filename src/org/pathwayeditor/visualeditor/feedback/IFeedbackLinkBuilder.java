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
package org.pathwayeditor.visualeditor.feedback;

import org.pathwayeditor.businessobjects.drawingprimitives.ILinkAttribute;
import org.pathwayeditor.businessobjects.typedefn.ILinkObjectType;
import org.pathwayeditor.figure.geometry.Point;

public interface IFeedbackLinkBuilder {

	IFeedbackModel getFeedbackModel();

//	IFeedbackLink createFromObjectType(IFeedbackNode srcNode, IFeedbackNode tgtNode, ILinkObjectType objectType);
//
//	IFeedbackLink createFromAttribute(IFeedbackNode srcNode, IFeedbackNode tgtNode, ILinkAttribute linkAttribute,
//			Point point, IAnchorLocator srcAnchorLocator, Point point2, IAnchorLocator tgtAnchorLocator);
//
//	IFeedbackLink createNodelessLinkFromObjectType(Point srcPosn, Point tgtPosn, ILinkObjectType linkObjectType);

//	IFeedbackLink createFromObjectType(ILinkObjectType objectType);

	IFeedbackLink createFromAttribute(ILinkAttribute linkAttribute);

	IFeedbackLink createNodelessLinkFromObjectType(Point srcPosn, Point tgtPosn, ILinkObjectType linkObjectType);
}
