package org.pathwayeditor.visualeditor.behaviour.operation;


public interface IOperationFactory {

	IEditingOperation getMoveOperation();

	IResizeOperation getResizeOperation();

	ILinkOperation getLinkOperation();

	IMarqueeOperation getMarqueeOperation();

	IShapePopupActions getShapePopupMenuResponse();

	ILinkPopupActions getLinkPopupMenuResponse();

	IDefaultPopupActions getDefaultPopupMenuResponse();

	ILinkBendPointPopupActions getLinkBendpointPopupMenuResponse();

	IShapeCreationOperation getShapeCreationOperation();
	
	ISelectionOperation getSelectionOperation();

}
