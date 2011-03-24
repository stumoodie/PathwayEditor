package org.pathwayeditor.visualeditor.commands;

import org.pathwayeditor.businessobjects.drawingprimitives.ILinkAttribute;
import org.pathwayeditor.businessobjects.drawingprimitives.ILinkEdge;
import org.pathwayeditor.businessobjects.drawingprimitives.ILinkEdgeFactory;
import org.pathwayeditor.businessobjects.drawingprimitives.IShapeNode;
import org.pathwayeditor.businessobjects.impl.facades.LinkEdgeFactoryFacade;
import org.pathwayeditor.businessobjects.typedefn.ILinkObjectType;
import org.pathwayeditor.figure.geometry.Point;

import uk.ac.ed.inf.graph.compound.ICompoundGraph;
import uk.ac.ed.inf.graph.state.IGraphState;

public class LinkCreationCommand implements ICommand {
	private final IShapeNode srcShape;
	private final IShapeNode tgtShape;
	private final ILinkObjectType linkObjectType;
	private IGraphState originalState;
	private IGraphState createdState;
	private final Point srcAnchorPosn;
	private final Point tgtAnchorPosn;

	public LinkCreationCommand(IShapeNode srcNode, IShapeNode tgtNode, ILinkObjectType linkObjectType, Point srcAnchorPosn, Point tgtAnchorPosn) {
		this.srcShape = srcNode;
		this.tgtShape = tgtNode;
		this.linkObjectType = linkObjectType;
		this.srcAnchorPosn = srcAnchorPosn;
		this.tgtAnchorPosn = tgtAnchorPosn;
	}

	@Override
	public void execute() {
		ICompoundGraph graph = this.srcShape.getGraphElement().getGraph();
		this.originalState = graph.getCurrentState();
		ILinkEdgeFactory fact = new LinkEdgeFactoryFacade(graph.edgeFactory());
		fact.setShapeNodePair(srcShape, tgtShape);
		fact.setObjectType(linkObjectType);
		ILinkEdge link = fact.createLinkEdge();
		ILinkAttribute linkAttribute = link.getAttribute();
		linkAttribute.getSourceTerminus().setLocation(srcAnchorPosn);
		linkAttribute.getTargetTerminus().setLocation(tgtAnchorPosn);
		this.createdState = graph.getCurrentState();
	}

	@Override
	public void undo() {
		this.srcShape.getGraphElement().getGraph().restoreState(this.originalState);
	}

	@Override
	public void redo() {
		this.srcShape.getGraphElement().getGraph().restoreState(createdState);
	}

}
