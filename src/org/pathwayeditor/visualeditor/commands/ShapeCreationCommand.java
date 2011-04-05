package org.pathwayeditor.visualeditor.commands;

import java.text.Format;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.pathwayeditor.businessobjects.drawingprimitives.IDrawingElement;
import org.pathwayeditor.businessobjects.drawingprimitives.ILabelNode;
import org.pathwayeditor.businessobjects.drawingprimitives.ILabelNodeFactory;
import org.pathwayeditor.businessobjects.drawingprimitives.IShapeNode;
import org.pathwayeditor.businessobjects.drawingprimitives.IShapeNodeFactory;
import org.pathwayeditor.businessobjects.drawingprimitives.properties.IAnnotationProperty;
import org.pathwayeditor.businessobjects.impl.facades.LabelNodeFactoryFacade;
import org.pathwayeditor.businessobjects.impl.facades.ShapeNodeFactoryFacade;
import org.pathwayeditor.businessobjects.notationsubsystem.INotationSyntaxService;
import org.pathwayeditor.businessobjects.typedefn.ILabelObjectType;
import org.pathwayeditor.businessobjects.typedefn.IShapeObjectType;
import org.pathwayeditor.figure.geometry.Envelope;
import org.pathwayeditor.figure.rendering.IFigureRenderingController;
import org.pathwayeditor.visualeditor.layout.ILabelPositionCalculator;

import uk.ac.ed.inf.graph.state.IGraphState;

public class ShapeCreationCommand implements ICommand {
	private final Logger logger = Logger.getLogger(this.getClass());
	private final IDrawingElement parentNode;
	private final IShapeObjectType objectType;
	private final IFigureRenderingController figController;
	private IGraphState createdState;
	private IGraphState originalState;
	private ILabelPositionCalculator labelPositionCalculator;
	
	public ShapeCreationCommand(IDrawingElement rootNode, IShapeObjectType shapeObjectType, IFigureRenderingController iFigureRenderingController,
			ILabelPositionCalculator labelPosnCalc) {
		this.parentNode = rootNode;
		this.objectType = shapeObjectType;
		this.figController = iFigureRenderingController;
		this.labelPositionCalculator = labelPosnCalc;
	}

	@Override
	public void execute() {
		this.originalState = this.parentNode.getGraphElement().getGraph().getCurrentState();
		IShapeNodeFactory fact = new ShapeNodeFactoryFacade(parentNode.getGraphElement().getChildCompoundGraph().nodeFactory());
		fact.setObjectType(objectType);
		IShapeNode node = fact.createShapeNode();
		node.getAttribute().setBounds(figController.getEnvelope());
		createShapeLabels(node);
		if(logger.isDebugEnabled()){
			logger.debug("Creating shapeNode=" + node + ", bounds=" + node.getAttribute().getBounds());
		}
		this.createdState = node.getGraphElement().getGraph().getCurrentState();
	}

	private void createShapeLabels(IShapeNode shapeHull){
		INotationSyntaxService syntaxService = this.parentNode.getAttribute().getModel().getNotationSubsystem().getSyntaxService();
		Iterator<IAnnotationProperty> defnIter = shapeHull.getAttribute().propertyIterator();
		while(defnIter.hasNext()){
			IAnnotationProperty defn = defnIter.next();
			if(syntaxService.isVisualisableProperty(defn.getDefinition())){
				ILabelObjectType labelObjectType = syntaxService.getLabelObjectTypeByProperty(defn.getDefinition());
				if(labelObjectType.isAlwaysDisplayed()){
					String defaultText = getDisplayedLabelText(labelObjectType, defn);
					ILabelNodeFactory labelFact = new LabelNodeFactoryFacade(shapeHull.getGraphElement().getChildCompoundGraph().nodeFactory());
					// display props that are always displayed
					labelFact.setProperty(defn);
					ILabelNode labelNode = labelFact.createLabelNode();
					if(logger.isDebugEnabled()){
						logger.debug("Create labelNode=" + labelNode + ", bounds=" + labelNode.getAttribute().getBounds());
					}
					Envelope labelBounds = this.labelPositionCalculator.calculateLabelPosition(this.figController, labelObjectType, defaultText);
					labelNode.getAttribute().setBounds(labelBounds);
				}
			}
		}
//		this.commandStack.execute(currentCmd);
	}
	
	private String getDisplayedLabelText(ILabelObjectType labelObjectType, IAnnotationProperty defn) {
		Format displayFormat = labelObjectType.getDefaultAttributes().getDisplayFormat();
		String retVal = null;
		if(displayFormat != null){
			retVal = displayFormat.format(defn.getValue());
		}
		else{
			retVal = defn.getValue().toString();
		}
		return retVal;
	}

	@Override
	public void redo() {
		this.parentNode.getGraphElement().getGraph().restoreState(createdState);
	}

	@Override
	public void undo() {
		this.parentNode.getGraphElement().getGraph().restoreState(this.originalState);
	}

	@Override
	public String toString(){
		StringBuilder buf = new StringBuilder(this.getClass().getSimpleName());
		buf.append("(");
		buf.append("parentNodeIdx=");
		buf.append(parentNode.getGraphElement().getIndex());
		buf.append(",bounds=");
		buf.append(this.figController.getEnvelope());
		buf.append(",objectType=");
		buf.append(objectType.getName());
		return buf.toString();
	}
}
