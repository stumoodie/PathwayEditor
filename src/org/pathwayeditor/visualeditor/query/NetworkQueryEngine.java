package org.pathwayeditor.visualeditor.query;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.pathwayeditor.businessobjects.drawingprimitives.IModel;
import org.pathwayeditor.businessobjects.exchange.FileXmlCanvasPersistenceManager;
import org.pathwayeditor.businessobjects.exchange.IXmlPersistenceManager;
import org.pathwayeditor.businessobjects.management.INotationSubsystemPool;
import org.pathwayeditor.notations.annotator.ndom.IEntity;
import org.pathwayeditor.notations.annotator.ndom.IInteraction;
import org.pathwayeditor.notations.annotator.ndom.INetwork;
import org.pathwayeditor.notations.annotator.ndom.IUndirectedInteraction;
import org.pathwayeditor.notations.annotator.ndom.impl.INetworkBuilder;
import org.pathwayeditor.notations.annotator.ndom.impl.NetworkBuilder;

public class NetworkQueryEngine implements INetworkQueryEngine {
	private final Logger logger = Logger.getLogger(this.getClass());
//	private static class ScoreLinkDescComparator implements Comparator<IInteraction>{
//
//		@Override
//		public int compare(IInteraction i1, IInteraction i2) {
//			double s1=i1.getConfidenceScore();
//			double s2=i2.getConfidenceScore();
//			return (s1>s2) ? -1 : ((s1<s2) ? 1:0);
//		}
//		
//	}

	private INetwork network;
	private final File idbFile;
	private IQueryObject queryObject;
	private final INotationSubsystemPool notationPool;
	private IQueryResult queryResult;
	private final Map<URI, IEntity> nopIndex;

	public NetworkQueryEngine(File idbFile, INotationSubsystemPool pool) {
		if (idbFile == null || pool == null) {
			throw new IllegalArgumentException("Parameters cannot be null");
		}
		this.idbFile = idbFile;
		this.notationPool = pool;
		this.nopIndex = new TreeMap<URI, IEntity>();
	}

	@Override
	public void queryIDb(){
		List<IInteraction> interationList = new LinkedList<IInteraction>();
//		Iterator<IInteraction> intnIterator = this.network.interactionIterator();
//		int resultCnt = 0;
//		while (intnIterator.hasNext()) {
//			IInteraction interaction = intnIterator.next();
//			IEntity node1 = interaction.getTerm1();
//			IEntity node2 = interaction.getTerm2();
			double minPrtrScore = this.queryObject.getScoreCutoff();
			Iterator<URI> nopIter = this.queryObject.nopUriIterator();
			while(nopIter.hasNext()){
				URI nopUri = nopIter.next();
				if(logger.isTraceEnabled()){
					logger.trace("Query NOP URI=" + nopUri);
				}
				findInterations(nopUri, minPrtrScore, interationList);
//				URI node1Uri = node1.getEntityURI();
//				URI node2Uri = node2.getEntityURI();
//				if(logger.isTraceEnabled()){
//					StringBuilder buf = new StringBuilder();
//					buf.append("Node 1 Entity URI=");
//					buf.append(node1Uri);
//					buf.append(", name=");
//					buf.append(node1.getStandardName());
//					buf.append(", Node 2 Entity URI=");
//					buf.append(node2Uri);
//					buf.append(", name=");
//					buf.append(node2.getStandardName());
//					buf.append(", Score=");
//					buf.append(interaction.getConfidenceScore());
//					logger.trace(buf.toString());
//				}
//				if (interaction.getConfidenceScore() > minPrtrScore && (node1Uri.equals(nopUri) || node2Uri.equals(nopUri))){
//					if(logger.isTraceEnabled()){
//						logger.trace("Interaction found. Node1=" + interaction.getTerm1().getStandardName() +
//								", Node2=" + interaction.getTerm2().getStandardName());
//					}
//					interationList.add(interaction);
//					resultCnt++;
//				}
			}
//		}
//			Collections.sort(interationList, new ScoreLinkDescComparator());
		logger.info("Number of interactions=" + interationList.size());
		this.queryResult = new QueryResult(this.queryObject, interationList);
	}

	private void findInterations(URI nopUri, double cutoff, List<IInteraction> interactionList) {
		IEntity queryEntity = this.nopIndex.get(nopUri);
		if(queryEntity != null){
			Iterator<IInteraction> interactionIter = queryEntity.allInteractionIterator();
			while(interactionIter.hasNext()){
				IUndirectedInteraction intern = (IUndirectedInteraction)interactionIter.next();
				if(intern.getConfidenceScore() > cutoff){
					if(logger.isTraceEnabled()){
						StringBuilder buf = new StringBuilder("Interaction found. Node1=");
						buf.append(intern.getThisEntity().getEntityURI());
						buf.append(", Node2=");
						buf.append(intern.getThatEntity().getEntityURI());
						logger.trace(buf.toString());
					}
					interactionList.add(intern);
				}
			}
		}
	}

	@Override
	public void loadIDb() {
		try {
			IXmlPersistenceManager canvasPersistenceManager = new FileXmlCanvasPersistenceManager(this.notationPool);
			logger.info("Loading DB file: " + idbFile.getName());
			InputStream in = new FileInputStream(idbFile);
			canvasPersistenceManager.readCanvasFromStream(in);
			in.close();
			IModel idbCanvas = canvasPersistenceManager.getCurrentCanvas();
			logger.info("Canvas loaded");
			INetworkBuilder builder = new NetworkBuilder(idbCanvas);
			builder.build();
			this.network = builder.getNetwork();
			Iterator<IEntity> entityIter = this.network.entityNodeIterator();
			while(entityIter.hasNext()){
				IEntity entity = entityIter.next();
				URI nopUri = entity.getEntityURI();
				this.nopIndex.put(nopUri, entity);
				if(entity.numTotalInteractions() == 0){
					logger.warn("Entity has no interactions: " + nopUri);
				}
			}
			logger.info("Network built");
		} catch (IOException ex) {
			logger.error("Error opening iDB file: " + idbFile.getPath(), ex);
			throw new RuntimeException(ex);
		}
	}

	@Override
	public void setQueryObject(IQueryObject queryObject) {
		this.queryObject = queryObject;
	}

	@Override
	public IQueryObject getQueryObject() {
		return this.queryObject;
	}

	@Override
	public IQueryResult getQueryResult() {
		return this.queryResult;
	}
	

}
