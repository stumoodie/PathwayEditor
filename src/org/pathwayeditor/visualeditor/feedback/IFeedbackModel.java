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

import java.util.Iterator;

import org.pathwayeditor.visualeditor.controller.IDrawingElementController;
import org.pathwayeditor.visualeditor.selection.ILinkSelection;
import org.pathwayeditor.visualeditor.selection.ISelectionRecord;

public interface IFeedbackModel {

	void rebuildIncludingHierarchy();
	
	void rebuildWithStrictSelection();
	
	void clear();
	
	ISelectionRecord getSelectionRecord();
	
	Iterator<IFeedbackNode> nodeIterator();
	
	Iterator<IFeedbackLink> linkIterator();
	
	Iterator<IFeedbackElement> elementIterator();

	void rebuildOnLinkSelection(ILinkSelection selection);

	IFeedbackLink uniqueFeedbackLink();

	IFeedbackElement getFeedbackElement(IDrawingElementController element);

	IFeedbackNodeBuilder getFeedbackNodeBuilder();

	IFeedbackLinkBuilder getFeedbackLinkBuilder();

	IFeedbackNode uniqueFeedbackNode();
}
