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
package org.pathwayeditor.visualeditor.commands;

import org.pathwayeditor.businessobjects.drawingprimitives.IDrawingNodeAttribute;
import org.pathwayeditor.figure.geometry.Dimension;
import org.pathwayeditor.figure.geometry.Point;

public class ResizeNodeCommand implements ICommand {
	private final IDrawingNodeAttribute node;
	private Point originDelta;
	private Dimension sizeDelta;
	
	
	public ResizeNodeCommand(IDrawingNodeAttribute drawingElement, Point originDelta, Dimension sizeDelta) {
		this.node = drawingElement;
		this.originDelta = originDelta;
		this.sizeDelta = sizeDelta;
	}

	@Override
	public void execute() {
		redo();
	}

	@Override
	public void redo() {
		this.node.resize(this.originDelta, this.sizeDelta);
	}

	@Override
	public void undo() {
		this.node.resize(this.originDelta.negate(), this.sizeDelta.negate());
	}

}
