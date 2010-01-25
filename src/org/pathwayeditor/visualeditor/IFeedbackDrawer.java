package org.pathwayeditor.visualeditor;

import java.awt.Graphics2D;

import org.pathwayeditor.visualeditor.feedback.IFeedbackModel;

public interface IFeedbackDrawer {

	IFeedbackModel getFeedbackModel();

	void paint(Graphics2D g2d);

}