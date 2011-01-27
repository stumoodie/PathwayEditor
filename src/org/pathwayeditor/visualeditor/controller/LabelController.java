package org.pathwayeditor.visualeditor.controller;

import org.pathwayeditor.businessobjects.drawingprimitives.ICanvasElementAttribute;
import org.pathwayeditor.businessobjects.drawingprimitives.ILabelNode;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.ICanvasAttributeChangeListener;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.ICanvasAttributePropertyChangeEvent;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.ICanvasAttributeResizedEvent;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.ICanvasAttributeTranslationEvent;

public class LabelController extends CommonLabelController implements ILabelController {
	private final ICanvasElementAttribute parentAttribute;
//	private final ICanvasAttributeChangeListener drawingNodePropertyChangeListener;
	private final ICanvasAttributeChangeListener parentDrawingNodePropertyChangeListener;
	
	public LabelController(IViewControllerModel viewModel, final ILabelNode node, int index) {
		super(viewModel, node, index);
		this.parentAttribute = (ICanvasElementAttribute)node.getGraphElement().getParent().getAttribute();
		parentDrawingNodePropertyChangeListener = new ICanvasAttributeChangeListener() {
			
			@Override
			public void propertyChange(ICanvasAttributePropertyChangeEvent e) {
			}
			
			@Override
			public void elementTranslated(ICanvasAttributeTranslationEvent e) {
				node.getAttribute().translate(e.getTranslationDelta());
			}
			
			@Override
			public void nodeResized(ICanvasAttributeResizedEvent e) {
			}
		};
	}


	@Override
	public void inactivateOverride() {
		parentAttribute.removeChangeListener(parentDrawingNodePropertyChangeListener);
	}


	@Override
	public void activateOverride() {
		parentAttribute.addChangeListener(parentDrawingNodePropertyChangeListener);
	}

}
