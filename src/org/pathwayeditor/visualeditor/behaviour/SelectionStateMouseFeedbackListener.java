package org.pathwayeditor.visualeditor.behaviour;

import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

import org.apache.log4j.Logger;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.visualeditor.selection.ISelectionHandle;
import org.pathwayeditor.visualeditor.selection.ISelectionHandle.SelectionHandleType;

public class SelectionStateMouseFeedbackListener implements MouseMotionListener {
	private final Logger logger = Logger.getLogger(this.getClass());
	private final IMouseStateBehaviourController mouseBehaviour;
	
	public SelectionStateMouseFeedbackListener(IMouseStateBehaviourController mouseBehaviour){
		this.mouseBehaviour = mouseBehaviour;
	}
	
	
	@Override
	public void mouseDragged(MouseEvent e) {
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		Point location = this.mouseBehaviour.getAdjustedMousePosition(e.getPoint().getX(), e.getPoint().getY());
		ISelectionHandle selectionHandle = this.mouseBehaviour.getSelectionRecord().findSelectionModelAt(location);
		if(logger.isTraceEnabled()){
			logger.trace("Selection handle = " + selectionHandle);
		}
		SelectionHandleType selectionRegion = selectionHandle != null ? selectionHandle.getType() : SelectionHandleType.None;
		IMouseFeedbackResponse currMouseFeedbackResponse = this.mouseBehaviour.getMouseFeedbackResponse(selectionRegion);
		if(logger.isTraceEnabled()){
			logger.trace("selection handle type = " + selectionRegion);
		}
		Cursor feedbackCursor = currMouseFeedbackResponse.getCurrentCursor();
		if(logger.isTraceEnabled()){
			logger.trace("feedback cursor = " + feedbackCursor.getName());
		}
		e.getComponent().setCursor(feedbackCursor);
	}
}
