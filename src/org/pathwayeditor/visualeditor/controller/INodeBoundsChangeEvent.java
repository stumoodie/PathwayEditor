package org.pathwayeditor.visualeditor.controller;

import org.pathwayeditor.figure.geometry.Envelope;

public interface INodeBoundsChangeEvent {

	INodeController getChangedNode();

	Envelope getOriginBounds();

	Envelope getNewBounds();
}
