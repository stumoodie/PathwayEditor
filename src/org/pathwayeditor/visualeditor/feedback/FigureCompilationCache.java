package org.pathwayeditor.visualeditor.feedback;

import java.util.HashMap;
import java.util.Map;

import org.pathwayeditor.figurevm.FigureDefinitionCompiler;
import org.pathwayeditor.figurevm.IFigureDefinition;

public class FigureCompilationCache {
	private static FigureCompilationCache anInstance = null;
	private final Map<String, IFigureDefinition> lookup;
	
	public static FigureCompilationCache getInstance(){
		if(anInstance == null){
			anInstance = new FigureCompilationCache();
		}
		return anInstance;
	}
	
	public FigureCompilationCache(){
		this.lookup = new HashMap<String, IFigureDefinition>();
	}
	
	public IFigureDefinition lookup(String figureDefnStr){
		if(!lookup.containsKey(figureDefnStr)){
			FigureDefinitionCompiler compiler = new FigureDefinitionCompiler(figureDefnStr);
			compiler.compile();
			this.lookup.put(figureDefnStr, compiler.getCompiledFigureDefinition());
		}
		return this.lookup.get(figureDefnStr);
	}
	
}
