/*
Copyright 2009, Court of the University of Edinburgh

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License. 
*/
/**
 * 
 */
package org.pathwayeditor.graphicsengine.stubs.notationsubsystem;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.pathwayeditor.businessobjects.drawingprimitives.ICanvas;
import org.pathwayeditor.businessobjects.notationsubsystem.INotation;
import org.pathwayeditor.businessobjects.notationsubsystem.INotationAutolayoutService;
import org.pathwayeditor.businessobjects.notationsubsystem.INotationConversionService;
import org.pathwayeditor.businessobjects.notationsubsystem.INotationExportService;
import org.pathwayeditor.businessobjects.notationsubsystem.INotationImportService;
import org.pathwayeditor.businessobjects.notationsubsystem.INotationPluginService;
import org.pathwayeditor.businessobjects.notationsubsystem.INotationSubsystem;
import org.pathwayeditor.businessobjects.notationsubsystem.INotationSyntaxService;
import org.pathwayeditor.businessobjects.notationsubsystem.INotationValidationService;

/**
 * @author smoodie
 *
 */
public class StubNotationSubSystem implements INotationSubsystem {
	private final INotation notation;
	private final INotationAutolayoutService autolayoutService;
	private INotationSyntaxService syntaxService;
	private INotationValidationService validationService;
	private Set<INotationExportService> exportServices=new HashSet<INotationExportService>();
	private Set<INotationImportService> importServices=new HashSet<INotationImportService>();
	private Set<INotationConversionService> conversionServices=new HashSet<INotationConversionService>();
	
	public StubNotationSubSystem(){
		this(null);
	}
	
	public StubNotationSubSystem(String notationName){
		if(notationName!=null)
			this.notation = new StubNotation(notationName);
		else
			this.notation = new StubNotation();
		this.autolayoutService = new StubAutoLayoutService();
		this.syntaxService = new StubNotationSyntaxService(this);
		this.validationService = new StubNotationValidationService(this);
		exportServices.add(new StubSBMLExportService());
		exportServices.add(new StubSBGNExportService());
		importServices.add(new StubSBMLImportService());
		importServices.add(new StubSBGNImportService());
		conversionServices.add(new StubNotationConversionService());
	}
	
	/* (non-Javadoc)
	 * @see org.pathwayeditor.businessobjects.contextadapter.INotationSubsystem#getAutolayoutService()
	 */
	public INotationAutolayoutService getAutolayoutService() {
		return this.autolayoutService;
	}

	/* (non-Javadoc)
	 * @see org.pathwayeditor.businessobjects.contextadapter.INotationSubsystem#getConversionServices()
	 */
	public Set<INotationConversionService> getConversionServices() {
		return conversionServices;
	}

	/* (non-Javadoc)
	 * @see org.pathwayeditor.businessobjects.contextadapter.INotationSubsystem#getExportServices()
	 */
	public Set<INotationExportService> getExportServices() {
		return exportServices;
	}

	/* (non-Javadoc)
	 * @see org.pathwayeditor.businessobjects.contextadapter.INotationSubsystem#getImportServices()
	 */
	public Set<INotationImportService> getImportServices() {
		return importServices;
	}

	/* (non-Javadoc)
	 * @see org.pathwayeditor.businessobjects.contextadapter.INotationSubsystem#getNotation()
	 */
	public INotation getNotation() {
		return this.notation;
	}

	/* (non-Javadoc)
	 * @see org.pathwayeditor.businessobjects.contextadapter.INotationSubsystem#getPluginServices()
	 */
	public Set<INotationPluginService> getPluginServices() {
		return Collections.emptySet();
	}

	/* (non-Javadoc)
	 * @see org.pathwayeditor.businessobjects.contextadapter.INotationSubsystem#getSyntaxService()
	 */
	public INotationSyntaxService getSyntaxService() {
		return this.syntaxService;
	}

	/* (non-Javadoc)
	 * @see org.pathwayeditor.businessobjects.contextadapter.INotationSubsystem#getValidationService()
	 */
	public INotationValidationService getValidationService() {
		return this.validationService;
	}

	/* (non-Javadoc)
	 * @see org.pathwayeditor.businessobjects.notationsubsystem.INotationSubsystem#isFallback()
	 */
	public boolean isFallback() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.pathwayeditor.businessobjects.notationsubsystem.INotationSubsystem#registerCanvas(org.pathwayeditor.businessobjects.drawingprimitives.ICanvas)
	 */
	public void registerCanvas(ICanvas canvasToRegister) {
		
	}

	/* (non-Javadoc)
	 * @see org.pathwayeditor.businessobjects.notationsubsystem.INotationSubsystem#unregisterCanvas(org.pathwayeditor.businessobjects.drawingprimitives.ICanvas)
	 */
	public void unregisterCanvas(ICanvas canvasToRegister) {
	}

}
