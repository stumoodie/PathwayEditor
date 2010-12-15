package org.pathwayeditor.visualeditor.autolayout;

import java.awt.Rectangle;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;
import org.pathwayeditor.businessobjects.drawingprimitives.IBendPointContainer;
import org.pathwayeditor.businessobjects.drawingprimitives.IDrawingNodeAttribute;
import org.pathwayeditor.businessobjects.drawingprimitives.ILinkAttribute;
import org.pathwayeditor.businessobjects.drawingprimitives.IModel;
import org.pathwayeditor.businessobjects.drawingprimitives.IShapeAttribute;
import org.pathwayeditor.figure.geometry.Envelope;
import org.pathwayeditor.figure.geometry.Point;

import uk.ac.ed.inf.graph.compound.CompoundNodePair;
import uk.ac.ed.inf.graph.compound.ICompoundEdge;
import uk.ac.ed.inf.graph.compound.ICompoundNode;
import y.base.Edge;
import y.base.EdgeCursor;
import y.base.EdgeMap;
import y.base.Node;
import y.base.NodeCursor;
import y.geom.YDimension;
import y.geom.YPoint;
import y.layout.BufferedLayouter;
import y.layout.DefaultLayoutGraph;
import y.layout.EdgeLayout;
import y.layout.IntersectionCalculator;
import y.layout.LayoutGraph;
import y.layout.NodeLayout;
import y.layout.PortCalculator;
import y.layout.organic.SmartOrganicLayouter;
import y.layout.router.OrganicEdgeRouter;

public class LayoutGraphBuilder {
	private final Logger logger = Logger.getLogger(this.getClass());
	private final Map<IDrawingNodeAttribute, Node> nodeMap;
	private final Map<Node, IDrawingNodeAttribute> nodeAttMap;
	private final Map<Edge, ILinkAttribute> edgeAttMap;
	private LayoutGraph yGraph;
	private IModel model;
	private EdgeMap srcEdgeMap;
	private EdgeMap tgtEdgeMap;

	public LayoutGraphBuilder(){
		nodeMap = new HashMap<IDrawingNodeAttribute, Node>();
		nodeAttMap = new HashMap<Node, IDrawingNodeAttribute>();
		edgeAttMap = new HashMap<Edge, ILinkAttribute>();
	}
	
	public void setGraphToLayout(IModel model){
		this.model = model;
	}
	
	public IModel getGraphToLayout(){
		return this.model;
	}
	
	public void createLayoutGraph(){
		yGraph = new DefaultLayoutGraph();
		Iterator<ICompoundNode> nodeIter = model.shapeNodeIterator();
		while(nodeIter.hasNext()){
			ICompoundNode node = nodeIter.next();
			Node yNode = yGraph.createNode();
			IDrawingNodeAttribute att = (IDrawingNodeAttribute)node.getAttribute();
			yGraph.setSize(yNode, createYDimension(att.getBounds()));
			nodeMap.put(att, yNode);
			nodeAttMap.put(yNode, att);
			if(logger.isTraceEnabled()){
				logger.trace("Adding to layout graph node=" + node);
			}
		}
		srcEdgeMap = yGraph.createEdgeMap();
		tgtEdgeMap = yGraph.createEdgeMap();
		Iterator<ICompoundEdge> edgeIter = model.linkEdgeIterator();
		while(edgeIter.hasNext()){
			ICompoundEdge edge = edgeIter.next();
			CompoundNodePair pair = edge.getConnectedNodes();
			Node outNode = this.nodeMap.get(pair.getOutNode().getAttribute());
			Node inNode = this.nodeMap.get(pair.getInNode().getAttribute());
			Edge currEdge = yGraph.createEdge(outNode, inNode);
			srcEdgeMap.set(currEdge, new ShapeIntersectionCalculator((IShapeAttribute)edge.getConnectedNodes().getOutNode().getAttribute()));
			tgtEdgeMap.set(currEdge, new ShapeIntersectionCalculator((IShapeAttribute)edge.getConnectedNodes().getInNode().getAttribute()));
			edgeAttMap.put(currEdge, (ILinkAttribute)edge.getAttribute());
			if(logger.isTraceEnabled()){
				logger.trace("Adding to layout graph edge=" + edge);
			}
		}
	}
	
	public void calculateLayout(){
		yGraph.addDataProvider(IntersectionCalculator.SOURCE_INTERSECTION_CALCULATOR_DPKEY, srcEdgeMap);
		yGraph.addDataProvider(IntersectionCalculator.TARGET_INTERSECTION_CALCULATOR_DPKEY, tgtEdgeMap);
		SmartOrganicLayouter layouter = new SmartOrganicLayouter();
		PortCalculator portCalculator = new PortCalculator(); 
		layouter.prependStage(portCalculator);
		layouter.setNodeEdgeOverlapAvoided(true);
		layouter.setNodeSizeAware(true);
//		layouter.setPreferredEdgeLength(75);
//		layouter.setMinimalNodeDistance(20);
		layouter.setNodeOverlapsAllowed(false);
		layouter.setQualityTimeRatio(1.0);
//		layouter.setCompactness(1.0);
		OrganicEdgeRouter oer = new OrganicEdgeRouter();
//		oer.setCoreLayouter(layouter);
		layouter.setComponentLayouter(oer);
		new BufferedLayouter(layouter).doLayout(yGraph);
//		OrganicEdgeRouter oer = new OrganicEdgeRouter();
//		LayoutStage nodeEnlarger = oer.createNodeEnlargementStage();
//		CompositeLayoutStage cls = new CompositeLayoutStage();
//		cls.appendStage(nodeEnlarger);
//		cls.appendStage(new BendConverter());
//		cls.appendStage(new RemoveOverlapsLayoutStage(0.0));
//		oer.setCoreLayouter(cls);
//		oer.setRoutingAll(true);
//		oer.setUsingBends(true);
//		oer.doLayout(yGraph);
		model.getRootAttribute().setCanvasBounds(createEnvelope(yGraph.getBoundingBox()));
		for(NodeCursor nodeCurs = yGraph.nodes(); nodeCurs.ok(); nodeCurs.next()){
			Node currNode = nodeCurs.node();
			NodeLayout nodeLayout = yGraph.getLayout(currNode);
			IDrawingNodeAttribute att = nodeAttMap.get(currNode);
			Envelope newBounds = createBoundsFromLayout(nodeLayout);
			if(logger.isTraceEnabled()){
				logger.trace("Node=" + currNode + ",bounds=" + newBounds);
			}
			att.setBounds(newBounds);
		}
		for(EdgeCursor edgeCurs = yGraph.edges(); edgeCurs.ok(); edgeCurs.next()){
			Edge currEdge = edgeCurs.edge();
			ILinkAttribute att = edgeAttMap.get(currEdge);
			if(logger.isTraceEnabled()){
				logger.trace("Defining linkAtt=" + att);
			}
			YPoint srcAnchor = yGraph.getSourcePointAbs(currEdge);
			Node srcNode = currEdge.source();
			if(logger.isTraceEnabled()){
				logger.trace("Src node=" + srcNode + ", anchor relpos=" + srcAnchor);
			}
			att.getSourceTerminus().setLocation(createPoint(srcAnchor));
			EdgeLayout edgeLayout = yGraph.getLayout(currEdge);
			IBendPointContainer bpContainer = att.getBendPointContainer();
			for(int i = 1; i < edgeLayout.pointCount()-1; i++){
				YPoint next = edgeLayout.getPoint(i);
				bpContainer.createNewBendPoint(createPoint(next));
				if(logger.isTraceEnabled()){
					logger.trace("Adding bendpoint = " + next);
				}
			}
			Node tgtNode = currEdge.target();
			YPoint tgtAnchor = yGraph.getTargetPointAbs(currEdge); 
			if(logger.isTraceEnabled()){
				logger.trace("Tgt node=" + tgtNode + ", anchor relpos=" + tgtAnchor);
			}
			att.getTargetTerminus().setLocation(createPoint(tgtAnchor));
		}
		yGraph.removeDataProvider(IntersectionCalculator.SOURCE_INTERSECTION_CALCULATOR_DPKEY);
		yGraph.removeDataProvider(IntersectionCalculator.TARGET_INTERSECTION_CALCULATOR_DPKEY);
	}

	private Envelope createEnvelope(Rectangle boundingBox) {
		return new Envelope(boundingBox.getX(), boundingBox.getY(), boundingBox.getWidth(), boundingBox.getHeight());
	}

	private Point createPoint(YPoint tgt) {
		return new Point(tgt.getX(), tgt.getY());
	}

	private Envelope createBoundsFromLayout(NodeLayout nodeLayout) {
		return new Envelope(nodeLayout.getX(), nodeLayout.getY(), nodeLayout.getWidth(), nodeLayout.getHeight());
	}

	private YDimension createYDimension(Envelope bounds) {
		return new YDimension(bounds.getDimension().getWidth(), bounds.getDimension().getHeight());
	}
}
