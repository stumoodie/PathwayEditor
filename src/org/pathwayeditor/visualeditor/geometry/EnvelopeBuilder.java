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
package org.pathwayeditor.visualeditor.geometry;

import org.pathwayeditor.figure.geometry.Envelope;

public class EnvelopeBuilder {
	private double x;
	private double y;
	private double width;
	private double height;
	
	public EnvelopeBuilder(Envelope initial){
		this.x = initial.getOrigin().getX();
		this.y = initial.getOrigin().getY();
		this.width = initial.getDimension().getWidth();
		this.height = initial.getDimension().getHeight();
	}

	
	public Envelope getEnvelope(){
		return new Envelope(x, y, width, height);
	}
	
	public void union(Envelope envelope){
		double newOriginX = envelope.getOrigin().getX() < this.x ? envelope.getOrigin().getX() : this.x;
		double newOriginY = envelope.getOrigin().getY() < this.y ? envelope.getOrigin().getY() : this.y;
		double diagX = this.x + this.width;
		double diagY = this.y + this.height;
		double envDiagX = envelope.getDiagonalCorner().getX();
		double envDiagY = envelope.getDiagonalCorner().getY();
		double newDblCrnrX = envDiagX > diagX ? envDiagX : diagX;
		double newDblCrnrY = envDiagY > diagY ? envDiagY : diagY;
		this.x = newOriginX;
		this.y = newOriginY;
		this.width = newDblCrnrX - newOriginX;
		this.height = newDblCrnrY - newOriginY;
	}


	public void expand(double widthExpansion, double heightExpansion) {
		x -= (widthExpansion/2.0);
		y -= (heightExpansion/2.0);
		width += widthExpansion;
		height += heightExpansion;
	}
	
}
