package org.pathwayeditor.notations.link2link;

import java.util.Set;

import org.pathwayeditor.businessobjects.drawingprimitives.IModel;
import org.pathwayeditor.businessobjects.drawingprimitives.attributes.Version;
import org.pathwayeditor.businessobjects.notationsubsystem.INotation;
import org.pathwayeditor.businessobjects.notationsubsystem.INotationAutolayoutService;
import org.pathwayeditor.businessobjects.notationsubsystem.INotationConversionService;
import org.pathwayeditor.businessobjects.notationsubsystem.INotationExportService;
import org.pathwayeditor.businessobjects.notationsubsystem.INotationImportService;
import org.pathwayeditor.businessobjects.notationsubsystem.INotationPluginService;
import org.pathwayeditor.businessobjects.notationsubsystem.INotationSubsystem;
import org.pathwayeditor.businessobjects.notationsubsystem.INotationSyntaxService;
import org.pathwayeditor.businessobjects.notationsubsystem.INotationValidationService;
import org.pathwayeditor.notationsubsystem.toolkit.definition.GeneralNotation;

public class Link2LinkNotationSubsystem implements INotationSubsystem {
	private final INotation notation;
	private Link2LinkSyntaxService syntaxService;

	public Link2LinkNotationSubsystem(){
		this.notation = new GeneralNotation("org.pathwayeditor.notations.link2link", "Link2Link",
				"Notation designed to test links to links", new Version(0,0,1));
		this.syntaxService = new Link2LinkSyntaxService(this);
	}
	
	@Override
	public void registerModel(IModel modelToRegister) {
		// do nothing just now.
	}

	@Override
	public void unregisterModel(IModel modelToRegister) {
		// do nothing just now
	}

	@Override
	public INotation getNotation() {
		return this.notation;
	}

	@Override
	public boolean isFallback() {
		return false;
	}

	@Override
	public INotationSyntaxService getSyntaxService() {
		return this.syntaxService;
	}

	@Override
	public Set<INotationExportService> getExportServices() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Not implemented yet!");

	}

	@Override
	public Set<INotationImportService> getImportServices() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Not implemented yet!");

	}

	@Override
	public INotationAutolayoutService getAutolayoutService() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Not implemented yet!");

	}

	@Override
	public INotationValidationService getValidationService() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Not implemented yet!");

	}

	@Override
	public Set<INotationPluginService> getPluginServices() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Not implemented yet!");

	}

	@Override
	public Set<INotationConversionService> getConversionServices() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Not implemented yet!");

	}

}
