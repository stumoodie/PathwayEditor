package org.pathwayeditor.visualeditor.selection;

import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.visualeditor.controller.ILinkController;
import org.pathwayeditor.visualeditor.selection.ISelectionHandle.SelectionRegion;

public class LinkSelection extends Selection implements ILinkSelection {

	public LinkSelection(boolean isPrimitive, ILinkController drawingElement) {
		super(isPrimitive);
		// TODO Auto-generated constructor stub
	}

	@Override
	public ISelectionHandle findSelectionModelAt(Point point) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ILinkController getPrimitiveController() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ISelectionHandle getSelectionModel(SelectionRegion region) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isPrimary() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isSecondary() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int compareTo(ISelection o) {
		// TODO Auto-generated method stub
		return 0;
	}

}
