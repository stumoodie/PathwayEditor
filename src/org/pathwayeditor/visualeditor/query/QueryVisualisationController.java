package org.pathwayeditor.visualeditor.query;

import java.util.Iterator;

import org.pathwayeditor.businessobjects.drawingprimitives.ILinkEdge;
import org.pathwayeditor.businessobjects.drawingprimitives.IModel;
import org.pathwayeditor.businessobjects.management.IModelFactory;
import org.pathwayeditor.businessobjects.management.ModelFactory;
import org.pathwayeditor.businessobjects.notationsubsystem.INotationSubsystem;
import org.pathwayeditor.notations.annotator.ndom.IInteraction;
import org.pathwayeditor.visualeditor.PathwayEditor;
import org.pathwayeditor.visualeditor.autolayout.LayoutGraphBuilder;

import uk.ac.ed.inf.graph.compound.ICompoundGraphCopyBuilder;
import uk.ac.ed.inf.graph.compound.ISubCompoundGraph;
import uk.ac.ed.inf.graph.compound.ISubCompoundGraphFactory;

public class QueryVisualisationController implements IQueryVisualisationController {
	private final INotationSubsystem annotationSubsystem;
	private IModel queryModel;
	private IPathwayQueryResult queryResult;
	private final PathwayEditor pathwayEditor;

	public QueryVisualisationController(INotationSubsystem notationSubsystem, PathwayEditor pathwayEditor){
		annotationSubsystem = notationSubsystem;
		this.pathwayEditor = pathwayEditor;
	}
	
	@Override
	public void visualiseQueryResults(){
		buildInteractionModel();
		layoutResults();
		pathwayEditor.renderModel(this.queryModel);
	}
	
	private void layoutResults() {
		LayoutGraphBuilder layoutBuilder = new LayoutGraphBuilder();
		layoutBuilder.setGraphToLayout(this.queryModel);
		layoutBuilder.createLayoutGraph();
		layoutBuilder.calculateLayout();
	}


	private void buildInteractionModel() {
		IModelFactory modelFactory = new ModelFactory();
		modelFactory.setNotationSubsystem(this.annotationSubsystem);
		modelFactory.setName("QueryResults");
		queryModel = modelFactory.createModel();
		ISubCompoundGraphFactory resultsGraphFractory = null; 
		Iterator<IInteraction> interIter = this.queryResult.resultIterator();
		while(interIter.hasNext()){
			IInteraction intern = interIter.next();
			ILinkEdge edge = intern.getLink();
			if(resultsGraphFractory == null){
				resultsGraphFractory = edge.getGraphElement().getGraph().subgraphFactory();
			}
			resultsGraphFractory.addElement(edge.getGraphElement());
			resultsGraphFractory.addElement(edge.getSourceShape());
			resultsGraphFractory.addElement(edge.getTargetShape());
		}
		if(resultsGraphFractory != null){
			ISubCompoundGraph copySubgraph = resultsGraphFractory.createSubgraph();
			ICompoundGraphCopyBuilder copyBuilder = queryModel.getGraph().getRoot().getChildCompoundGraph().newCopyBuilder();
			copyBuilder.setSourceSubgraph(copySubgraph);
			copyBuilder.makeCopy();
		}
	}


	@Override
	public IPathwayQueryResult getQueryResult() {
		return queryResult;
	}


	@Override
	public void setQueryResult(IPathwayQueryResult queryResult) {
		this.queryResult = queryResult;
	}


}
