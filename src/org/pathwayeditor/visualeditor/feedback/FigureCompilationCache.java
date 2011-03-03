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
