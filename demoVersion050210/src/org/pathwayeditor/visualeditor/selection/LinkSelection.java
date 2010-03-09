package org.pathwayeditor.visualeditor.selection;

import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.visualeditor.controller.ILinkController;
import org.pathwayeditor.visualeditor.selection.ISelectionHandle.SelectionHandleType;

public class LinkSelection extends Selection implements ILinkSelection {
	private final ILinkController controller;
	
	
	public LinkSelection(boolean primaryFlag, ILinkController drawingElement) {
		super(primaryFlag);
		this.controller = drawingElement;
	}

	@Override
	public ISelectionHandle findSelectionModelAt(Point point) {
		return null;
	}

	@Override
	public ILinkController getPrimitiveController() {
		return this.controller;
	}

	@Override
	public ISelectionHandle getSelectionModel(SelectionHandleType region) {
		return null;
	}
}
