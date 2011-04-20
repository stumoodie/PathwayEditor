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
package org.pathwayeditor.visualeditor.behaviour.linkcreation;

import java.awt.Cursor;

import org.pathwayeditor.visualeditor.behaviour.IMouseFeedbackResponse;

public class MouseLinkCreationFeedbackResponse implements IMouseFeedbackResponse {
//	private final Logger logger = Logger.getLogger(this.getClass());
	private StateType currentState;
	
	public MouseLinkCreationFeedbackResponse(){
		this.currentState = StateType.DEFAULT;
	}
	
	@Override
	public Cursor getCurrentCursor() {
		int retVal = Cursor.DEFAULT_CURSOR; 
		if(this.currentState.equals(StateType.DEFAULT)){
			retVal = Cursor.MOVE_CURSOR;
		}
		else if(this.currentState.equals(StateType.REPARENTING)){
			retVal = Cursor.HAND_CURSOR;
		}
		else if(this.currentState.equals(StateType.FORBIDDEN)){
			retVal = Cursor.WAIT_CURSOR;
		}
		return Cursor.getPredefinedCursor(retVal);
	}

	@Override
	public void changeState(StateType newState) {
		this.currentState = newState;
	}

	@Override
	public StateType getCurrentState() {
		return this.currentState;
	}

	@Override
	public void reset() {
		this.currentState = StateType.DEFAULT;
	}

	@Override
	public void altSelected(boolean isAltSelected) {
	}

}
