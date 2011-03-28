package org.pathwayeditor.visualeditor.selection;


public interface IHandleShapeDrawer {

	void drawHandle(ILinkMidLineHandleShape shape);
	
	void drawHandle(ILinkBendPointHandleShape shape);

	void drawHandle(ILinkSelectionHandleShape linkSelectionHandleShape);

	void drawHandle(ICentralSelectionHandleShape centralSelectionHandleShape);

	void drawHandle(ICornerSelectionHandleShape cornerSelectionHandleShape);

	void drawHandle(IMidPointSelectionHandleShape midPointSelectionHandleShape);
	
}
