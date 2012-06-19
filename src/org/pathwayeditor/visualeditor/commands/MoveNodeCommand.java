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

import org.apache.log4j.Logger;
import org.pathwayeditor.businessobjects.drawingprimitives.IDrawingNodeAttribute;
import org.pathwayeditor.figure.geometry.Point;

public class MoveNodeCommand implements ICommand {
	private final Logger logger = Logger.getLogger(this.getClass());
	private final IDrawingNodeAttribute node;
	private final Point locationDelta;
	
	public MoveNodeCommand(IDrawingNodeAttribute node, Point locationDelta) {
		this.node = node;
		this.locationDelta = locationDelta;
	}

	@Override
	public void execute() {
		this.redo();
	}

	@Override
	public void redo() {
		if(logger.isTraceEnabled()){
			logger.trace("Redo: Translating node:" + this.node + " by " + this.locationDelta);
		}
		this.node.translate(this.locationDelta);
	}

	@Override
	public void undo() {
		if(logger.isTraceEnabled()){
			logger.trace("Undo: Translating node:" + this.node + " by " + this.locationDelta.negate());
		}
		this.node.translate(this.locationDelta.negate());
	}

}
