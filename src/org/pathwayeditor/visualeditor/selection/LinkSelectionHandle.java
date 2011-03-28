package org.pathwayeditor.visualeditor.selection;

import org.pathwayeditor.figure.geometry.Envelope;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.visualeditor.controller.ILinkController;
import org.pathwayeditor.visualeditor.geometry.ILinkPointDefinition;

public class LinkSelectionHandle extends SelectionHandle implements ILinkSelectionHandleShape {
	private final ILinkPointDefinition linkPointDefinition;

	public LinkSelectionHandle(LinkSelection linkSelection, ILinkController controller) {
		super(controller, SelectionHandleType.Link, linkSelection);
		this.linkPointDefinition = controller.getLinkDefinition();
	}

	@Override
	public boolean containsPoint(Point point) {
		return this.linkPointDefinition.containsPoint(point);
	}

	@Override
	public void drawShape(IHandleShapeDrawer drawer) {
		drawer.drawHandle(this);
	}

	@Override
	public Envelope getBounds() {
		return this.linkPointDefinition.getBounds();
	}

	@Override
	public void translate(Point delta) {
		this.linkPointDefinition.translate(delta);
	}

	@Override
	public int compareTo(ISelectionHandle o) {
		return this.getDrawingPrimitiveController().compareTo(o.getDrawingPrimitiveController());
	}

	@Override
	public ILinkPointDefinition getLinkDefinition() {
		return ((ILinkController)this.getDrawingPrimitiveController()).getLinkDefinition();
	}

}
