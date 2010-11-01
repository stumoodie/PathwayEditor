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

import java.io.File;

import org.pathwayeditor.businessobjects.drawingprimitives.IRootAttribute;
import org.pathwayeditor.businessobjects.notationsubsystem.ExportServiceException;
import org.pathwayeditor.businessobjects.notationsubsystem.INotation;
import org.pathwayeditor.businessobjects.notationsubsystem.INotationExportService;
import org.pathwayeditor.businessobjects.notationsubsystem.INotationSubsystem;

/**
 * @author nhanlon
 *
 */
public class StubSBMLExportService implements INotationExportService {

	public static final String DISPLAY_NAME = "SBML Export";

	/* (non-Javadoc)
	 * @see org.pathwayeditor.businessobjects.notationsubsystem.INotationExportService#exportMap(org.pathwayeditor.businessobjects.drawingprimitives.ICanvas, java.io.File)
	 */
	@Override
	public void exportMap(IRootAttribute map, File exportFile) throws ExportServiceException {
		System.out.println("Map " + map + " was exported to " + exportFile.getAbsolutePath());

	}

	/* (non-Javadoc)
	 * @see org.pathwayeditor.businessobjects.notationsubsystem.INotationExportService#getCode()
	 */
	@Override
	public String getCode() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.pathwayeditor.businessobjects.notationsubsystem.INotationExportService#getDisplayName()
	 */
	@Override
	public String getDisplayName() {
		
		return DISPLAY_NAME;
	}

	/* (non-Javadoc)
	 * @see org.pathwayeditor.businessobjects.notationsubsystem.INotationExportService#getRecommendedSuffix()
	 */
	@Override
	public String getRecommendedSuffix() {
		return "xml";
	}

	/* (non-Javadoc)
	 * @see org.pathwayeditor.businessobjects.notationsubsystem.INotationService#getNotation()
	 */
	@Override
	public INotation getNotation() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.pathwayeditor.businessobjects.notationsubsystem.INotationService#getNotationSubsystem()
	 */
	@Override
	public INotationSubsystem getNotationSubsystem() {
		// TODO Auto-generated method stub
		return null;
	}

}
