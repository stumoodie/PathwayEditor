package org.pathwayeditor.visualeditor.selection;

public interface IHandleShapeDrawer {

	void drawHandle(ILinkMidLineHandleShape shape);
	
	void drawHandle(ILinkBendPointHandleShape shape);

	void drawHandle(LinkSelectionHandle linkSelectionHandle);
	
}
