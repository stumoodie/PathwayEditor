package org.pathwayeditor.visualeditor.query;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Iterator;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.pathwayeditor.businessobjects.drawingprimitives.ILinkEdge;
import org.pathwayeditor.businessobjects.drawingprimitives.IModel;
import org.pathwayeditor.businessobjects.management.IModelFactory;
import org.pathwayeditor.businessobjects.management.ModelFactory;
import org.pathwayeditor.businessobjects.notationsubsystem.INotationSubsystem;
import org.pathwayeditor.naming.of.parts.IDictionaryEntry;
import org.pathwayeditor.naming.of.parts.INamingOfPartsLookup;
import org.pathwayeditor.naming.of.parts.impl.DBNoPLookup;
import org.pathwayeditor.notations.annotator.ndom.IInteraction;
import org.pathwayeditor.notations.annotator.services.AnnotatorNotationSubsystem;

import uk.ac.ed.inf.graph.compound.ICompoundGraphCopyBuilder;
import uk.ac.ed.inf.graph.compound.ISubCompoundGraph;
import uk.ac.ed.inf.graph.compound.ISubCompoundGraphFactory;

public class PathwayQueryController implements IPathwayQueryController {
	private final Logger logger = Logger.getLogger(this.getClass());
	
	public static final double DEFAULT_THRESHOLD = 1e-6;
	public static final int DEFAULT_DIAMETER = 1;
	private final INetworkQueryEngine networkQueryEngine;
	private final DataSource dbSource;
	private QueryData queryData;
//	private String proteinName;
//	private double cutOff;
	private IPathwayQueryResult queryResult;
	private INamingOfPartsLookup nopLookup;
	private final INotationSubsystem annotationSubsystem;
	
	public PathwayQueryController(INetworkQueryEngine networkQueryEngine, DataSource dbSource){
		this.dbSource = dbSource;
		this.networkQueryEngine = networkQueryEngine;
		annotationSubsystem = new AnnotatorNotationSubsystem();
	}
	
	
	@Override
	public void initialise() {
		this.networkQueryEngine.loadIDb();
	}

//	@Override
//	public void setProteinName(String proteinName) {
//		this.proteinName = proteinName;
//	}
//
//	@Override
//	public String getProteinName() {
//		return this.proteinName;
//	}
//
//	@Override
//	public void setCutoff(double cutoff) {
//		this.cutOff = cutoff;
//	}

//	@Override
//	public double getCutoff() {
//		return this.cutOff;
//	}

	@Override
	public IPathwayQueryResult getQueryResult() {
		return this.queryResult;
	}

	private void initialiseDbLookups(){
		Connection conn;
		try {
			conn = this.dbSource.getConnection();
			nopLookup = new DBNoPLookup();
			nopLookup.setConnection(conn);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	private void finaliseDbLookups(){
		try {
			Connection conn= this.nopLookup.getConnection();
			if(conn != null){
				conn.close();
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		this.nopLookup.setConnection(null);
	}
	
	
	@Override
	public void runQuery() {
//		ResultsTableModel resultsTableModel = this.resultsTable.getTableModel();
//		resultsTableModel.reset();
		executeQuery();
		buildInteractionModel();
//		Iterator<IInteraction> edgeIter = queryResult.resultIterator();
//		logger.info("Query found " + queryResult.numInteractions() + " interactions");
//		resultsTableModel.setData(edgeIter, queryResult.numInteractions());
	}
	
	
	private void buildInteractionModel() {
		IModelFactory modelFactory = new ModelFactory();
		modelFactory.setNotationSubsystem(this.annotationSubsystem);
		modelFactory.setName("QueryResults");
		IModel queryModel= modelFactory.createModel();
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


	private void executeQuery() {
		if(logger.isDebugEnabled()){
			logger.debug("Running query with query data=" + queryData);
		}
		initialiseDbLookups();
		String proteinName = this.queryData.getTerm1();
		Iterator<IDictionaryEntry> nameIter = nopLookup.findEntityByName(proteinName, false);
		IQueryBuilder builder = new QueryBuilder();
		double cutOff = this.queryData.getConfScoreCutoff().doubleValue();
		builder.setCutoff(cutOff);
		while(nameIter.hasNext()){
			builder.addNopUri(nameIter.next().getEntityURI());
		}	
		builder.buildQuery();
		this.networkQueryEngine.setQueryObject(builder.getCurrentQuery());
		this.networkQueryEngine.queryIDb();
		this.networkQueryEngine.getQueryResult();
		this.queryResult = new PathwayQueryResult(this.networkQueryEngine.getQueryResult());
		finaliseDbLookups();
		if(logger.isDebugEnabled()){
			logger.debug("Finished query and num results=" + this.queryResult.numInteractions());
		}
	}


//	@Override
//	public void setResultTable(ResultsTable resultsTable) {
//		this.resultsTable = resultsTable;
//	}
//
//
//	@Override
//	public ResultsTable getResultTable() {
//		return this.resultsTable;
//	}


	@Override
	public void setQueryData(QueryData queryData) {
		this.queryData = queryData;
	}


	@Override
	public QueryData getQueryData() {
		return this.queryData;
	}

}
