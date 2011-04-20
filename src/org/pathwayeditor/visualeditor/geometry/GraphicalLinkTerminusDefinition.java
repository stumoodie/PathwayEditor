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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.pathwayeditor.businessobjects.drawingprimitives.ILinkTerminus;
import org.pathwayeditor.businessobjects.drawingprimitives.attributes.LinkEndDecoratorShape;
import org.pathwayeditor.businessobjects.typedefn.ILinkTerminusDefaults;
import org.pathwayeditor.businessobjects.typedefn.ILinkTerminusDefinition;
import org.pathwayeditor.figure.geometry.Dimension;
import org.pathwayeditor.visualeditor.geometry.IGraphicalLinkTerminusDefinitionChangeEvent.GraphicalLinkTerminusDefinitionChangeType;

public class GraphicalLinkTerminusDefinition implements IGraphicalLinkTerminusDefinition {
	private static final Dimension DEFAULT_END_SIZE = new Dimension(0, 0);
	private static final double DEFAULT_GAP = 0;
	private static final LinkEndDecoratorShape DEFAULT_END_DECORATOR_TYPE = LinkEndDecoratorShape.NONE;
	private Dimension endSize;
	private double gap;
	private LinkEndDecoratorShape endDecoratorType;
	private final List<IGraphicalLinkTerminusDefinitionChangeListener> listeners = new LinkedList<IGraphicalLinkTerminusDefinitionChangeListener>();
	
	public GraphicalLinkTerminusDefinition(ILinkTerminus terminus) {
		this.endSize = terminus.getEndSize();
		this.gap = terminus.getGap();
		this.endDecoratorType = terminus.getEndDecoratorType();
	}

	public GraphicalLinkTerminusDefinition(ILinkTerminusDefinition terminusDefn) {
		ILinkTerminusDefaults terminusDefaults = terminusDefn.getDefaultAttributes();
		this.endSize = terminusDefaults.getEndSize();
		this.gap = terminusDefaults.getGap();
		this.endDecoratorType = terminusDefaults.getEndDecoratorType();
	}

	public GraphicalLinkTerminusDefinition(){
		this.endSize = DEFAULT_END_SIZE;
		this.gap = DEFAULT_GAP;
		this.endDecoratorType = DEFAULT_END_DECORATOR_TYPE;
	}
	
	public GraphicalLinkTerminusDefinition(IGraphicalLinkTerminusDefinition other) {
		this.endDecoratorType = other.getEndDecoratorType();
		this.endSize = other.getEndSize();
		this.gap = other.getGap();
	}

	@Override
	public LinkEndDecoratorShape getEndDecoratorType() {
		return this.endDecoratorType;
	}

	@Override
	public Dimension getEndSize() {
		return this.endSize;
	}

	@Override
	public double getGap() {
		return this.gap;
	}

	@Override
	public void setEndSize(Dimension endSize) {
		if(!this.endSize.equals(endSize)){
			Dimension oldValue = this.endSize;
			this.endSize = endSize;
			notifyPropertyChange(GraphicalLinkTerminusDefinitionChangeType.SIZE, oldValue, this.endSize);
		}
	}

	@Override
	public void setGap(double gap) {
		if(this.gap != gap){
			double oldValue = this.gap;
			this.gap = gap;
			notifyPropertyChange(GraphicalLinkTerminusDefinitionChangeType.GAP, oldValue, gap);
		}
	}

	@Override
	public void setEndDecoratorType(LinkEndDecoratorShape endDecoratorType) {
		if(!this.endDecoratorType.equals(endDecoratorType)){
			LinkEndDecoratorShape oldValue = this.endDecoratorType;
			this.endDecoratorType = endDecoratorType;
			notifyPropertyChange(GraphicalLinkTerminusDefinitionChangeType.END_DECORATOR, oldValue, this.endDecoratorType);
		}
	}

	@Override
	public void addDefinitionChangeListener(IGraphicalLinkTerminusDefinitionChangeListener l) {
		this.listeners.add(l);
	}

	@Override
	public void removeDefinitionChangeListener(IGraphicalLinkTerminusDefinitionChangeListener l) {
		this.listeners.remove(l);
	}

	@Override
	public List<IGraphicalLinkTerminusDefinitionChangeListener> getDefinitionChangeListeners() {
		return new ArrayList<IGraphicalLinkTerminusDefinitionChangeListener>(this.listeners);
	}
	
	private void notifyPropertyChange(final GraphicalLinkTerminusDefinitionChangeType type, final Object oldGap, final Object newGap){
		IGraphicalLinkTerminusDefinitionChangeEvent e = new IGraphicalLinkTerminusDefinitionChangeEvent(){

			@Override
			public Object getOldValue() {
				return oldGap;
			}

			@Override
			public Object getNewValue() {
				return newGap;
			}

			@Override
			public GraphicalLinkTerminusDefinitionChangeType getChangeType() {
				return type;
			}

			@Override
			public IGraphicalLinkTerminusDefinition getSource() {
				return GraphicalLinkTerminusDefinition.this;
			}
			
		};
		for(IGraphicalLinkTerminusDefinitionChangeListener l : this.listeners){
			l.linkTerminusPropertyChange(e);
		}
	}
}
