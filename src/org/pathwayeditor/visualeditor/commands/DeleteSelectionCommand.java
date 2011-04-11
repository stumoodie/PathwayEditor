package org.pathwayeditor.visualeditor.commands;

import org.pathwayeditor.visualeditor.selection.ISubgraphSelection;

import uk.ac.ed.inf.graph.compound.ICompoundGraph;
import uk.ac.ed.inf.graph.compound.ISubgraphRemovalBuilder;
import uk.ac.ed.inf.graph.state.IGraphState;

public class DeleteSelectionCommand implements ICommand {
	private ISubgraphSelection subgraphSelection;
	private IGraphState beforeChangeMomento;
	private IGraphState afterChangeMomento;

	public DeleteSelectionCommand(ISubgraphSelection subgraphSelection) {
		this.subgraphSelection = subgraphSelection;
	}

	@Override
	public void execute() {
		ICompoundGraph graph = subgraphSelection.getDrawingElementSelection().getSubgraph().getSuperGraph();
		this.beforeChangeMomento = graph.getCurrentState();
		ISubgraphRemovalBuilder removalbuilder = graph.newSubgraphRemovalBuilder();
		removalbuilder.setRemovalSubgraph(subgraphSelection.getDrawingElementSelection().getSubgraph());
		removalbuilder.removeSubgraph();
		this.afterChangeMomento = graph.getCurrentState();
	}

	@Override
	public void undo() {
		this.subgraphSelection.getDrawingElementSelection().getSubgraph().getSuperGraph().restoreState(beforeChangeMomento);
	}

	@Override
	public void redo() {
		this.subgraphSelection.getDrawingElementSelection().getSubgraph().getSuperGraph().restoreState(afterChangeMomento);
	}

}
