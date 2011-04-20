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

import java.math.BigDecimal;
import java.util.List;

import org.pathwayeditor.businessobjects.drawingprimitives.properties.IAnnotationProperty;
import org.pathwayeditor.businessobjects.drawingprimitives.properties.IAnnotationPropertyVisitor;
import org.pathwayeditor.businessobjects.drawingprimitives.properties.IBooleanAnnotationProperty;
import org.pathwayeditor.businessobjects.drawingprimitives.properties.IIntegerAnnotationProperty;
import org.pathwayeditor.businessobjects.drawingprimitives.properties.IListAnnotationProperty;
import org.pathwayeditor.businessobjects.drawingprimitives.properties.INumberAnnotationProperty;
import org.pathwayeditor.businessobjects.drawingprimitives.properties.IPlainTextAnnotationProperty;

public class ChangeAnnotationPropertyValue implements ICommand {
	private IAnnotationProperty property;
	private Object labelValue;
	private Object oldValue;

	public ChangeAnnotationPropertyValue(IAnnotationProperty prop, Object labelValue) {
		this.property = prop;
		this.labelValue = labelValue;
		this.oldValue = null;
	}

	@Override
	public void execute() {
		this.oldValue = this.property.getValue();
		this.redo();
	}

	@Override
	public void undo() {
		this.property.visit(new IAnnotationPropertyVisitor() {
			
			@Override
			public void visitPlainTextAnnotationProperty(IPlainTextAnnotationProperty prop) {
				prop.setValue((String)oldValue);
			}
			
			@Override
			public void visitNumberAnnotationProperty(INumberAnnotationProperty prop) {
				prop.setValue((BigDecimal)oldValue);
			}
			
			@SuppressWarnings({ "unchecked" })
			@Override
			public void visitListAnnotationProperty(IListAnnotationProperty prop) {
				prop.getValue().addAll((List<? extends String>)oldValue);
			}
			
			@Override
			public void visitIntegerAnnotationProperty(IIntegerAnnotationProperty prop) {
				prop.setValue((Integer)oldValue);
			}
			
			@Override
			public void visitBooleanAnnotationProperty(IBooleanAnnotationProperty prop) {
				prop.setValue((Boolean)oldValue);
			}
		});
	}

	@Override
	public void redo() {
		this.property.visit(new IAnnotationPropertyVisitor() {
			
			@Override
			public void visitPlainTextAnnotationProperty(IPlainTextAnnotationProperty prop) {
				prop.setValue((String)labelValue);
			}
			
			@Override
			public void visitNumberAnnotationProperty(INumberAnnotationProperty prop) {
				prop.setValue((BigDecimal)labelValue);
			}
			
			@SuppressWarnings({ "unchecked" })
			@Override
			public void visitListAnnotationProperty(IListAnnotationProperty prop) {
				prop.getValue().addAll((List<? extends String>)labelValue);
			}
			
			@Override
			public void visitIntegerAnnotationProperty(IIntegerAnnotationProperty prop) {
				prop.setValue((Integer)labelValue);
			}
			
			@Override
			public void visitBooleanAnnotationProperty(IBooleanAnnotationProperty prop) {
				prop.setValue((Boolean)labelValue);
			}
		});
	}

}
