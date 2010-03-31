package org.pathwayeditor.visualeditor.geometry;

import org.pathwayeditor.figure.geometry.IConvexHull;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.visualeditor.controller.INodeController;
import org.pathwayeditor.visualeditor.selection.ISubgraphSelection;

public interface ICommonParentCalculator {

	void findCommonParentExcludingLabels(ISubgraphSelection testSelection, Point delta);

	void findCommonParent(ISubgraphSelection testSelection, Point delta);

	ISubgraphSelection getSelectionToReparent();

	boolean canReparentSelection();

	boolean canMoveSelection();

	boolean hasFoundCommonParent();

	INodeController getCommonParent();

	INodeController findPotentialParent(final INodeController potentialChild, IConvexHull testPlacement);

}