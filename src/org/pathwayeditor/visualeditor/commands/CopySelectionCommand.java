package org.pathwayeditor.visualeditor.commands;

import java.util.Iterator;

import org.apache.log4j.Logger;
import org.pathwayeditor.businessobjects.drawingprimitives.ICanvasElementAttribute;
import org.pathwayeditor.businessobjects.drawingprimitives.IDrawingElement;
import org.pathwayeditor.businessobjects.drawingprimitives.IDrawingElementSelection;
import org.pathwayeditor.figure.geometry.Point;

import uk.ac.ed.inf.graph.compound.ICompoundGraphCopyBuilder;
import uk.ac.ed.inf.graph.compound.ICompoundNode;
import uk.ac.ed.inf.graph.compound.ISubCompoundGraph;
import uk.ac.ed.inf.graph.state.IGraphState;
import uk.ac.ed.inf.graph.state.IRestorableGraph;

public class CopySelectionCommand implements ICommand {
	private final Logger logger = Logger.getLogger(this.getClass());
	/** Shape to manipulate. */
	private IDrawingElementSelection selection;
	private IDrawingElement newParent;
	private Point delta;
	private IGraphState beforeChangeMomento;
	private IGraphState afterChangeMomento;
	private ISubCompoundGraph copiedElementSelection;

	public CopySelectionCommand(IDrawingElement newParent, IDrawingElementSelection selection, Point delta) {
		this.newParent = newParent;
		this.selection = selection;
		this.delta = delta;
	}

	@Override
	public void execute() {
		this.beforeChangeMomento = this.newParent.getGraphElement().getGraph().getCurrentState();
		ICompoundGraphCopyBuilder moveBuilder = this.newParent.getGraphElement().getChildCompoundGraph().newCopyBuilder();
		moveBuilder.setSourceSubgraph(selection.getSubgraph());
		moveBuilder.makeCopy();
		this.copiedElementSelection = moveBuilder.getCopiedComponents();
		translateElements(this.delta);
		this.afterChangeMomento = this.newParent.getGraphElement().getGraph().getCurrentState();
		if(logger.isDebugEnabled()){
			logger.debug("Moved shape: " +  this.selection + " to  " + this.newParent);
		}
		this.newParent = null;
		this.selection = null;
	}

	private void translateElements(Point newDelta) {
		// only need to modify top nodes as children and links will automatically be moved by the
		// controllers.
		Iterator<ICompoundNode> iter = this.copiedElementSelection.topNodeIterator();
		while(iter.hasNext()){
			ICompoundNode el = iter.next();
			((ICanvasElementAttribute)el.getAttribute()).translate(newDelta);
		}
	}

	@Override
	public void undo() {
		translateElements(delta.negate());
		IRestorableGraph model = this.beforeChangeMomento.getGraph();
		model.restoreState(beforeChangeMomento);
		if(logger.isDebugEnabled()){
			logger.debug("undo: restored state");
		}
	}

	@Override
	public void redo() {
		IRestorableGraph model = this.afterChangeMomento.getGraph();
		model.restoreState(afterChangeMomento);
		translateElements(delta);
		if(logger.isDebugEnabled()){
			logger.debug("redo: restored state");
		}
	}

}
