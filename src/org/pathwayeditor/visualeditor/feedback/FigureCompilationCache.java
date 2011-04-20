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

import java.util.HashMap;
import java.util.Map;

import org.pathwayeditor.figure.definition.FigureDefinitionCompiler;
import org.pathwayeditor.figure.definition.ICompiledFigureDefinition;

public class FigureCompilationCache {
	private static FigureCompilationCache anInstance = null;
	private final Map<String, ICompiledFigureDefinition> lookup;
	
	public static FigureCompilationCache getInstance(){
		if(anInstance == null){
			anInstance = new FigureCompilationCache();
		}
		return anInstance;
	}
	
	public FigureCompilationCache(){
		this.lookup = new HashMap<String, ICompiledFigureDefinition>();
	}
	
	public ICompiledFigureDefinition lookup(String figureDefnStr){
		if(!lookup.containsKey(figureDefnStr)){
			FigureDefinitionCompiler compiler = new FigureDefinitionCompiler(figureDefnStr);
			compiler.compile();
			this.lookup.put(figureDefnStr, compiler.getCompiledFigureDefinition());
		}
		return this.lookup.get(figureDefnStr);
	}
	
}
