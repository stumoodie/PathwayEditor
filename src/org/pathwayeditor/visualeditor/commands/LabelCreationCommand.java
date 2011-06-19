package org.pathwayeditor.visualeditor.commands;

import java.text.Format;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.pathwayeditor.businessobjects.drawingprimitives.ILabelNode;
import org.pathwayeditor.businessobjects.drawingprimitives.ILabelNodeFactory;
import org.pathwayeditor.businessobjects.drawingprimitives.IShapeAttribute;
import org.pathwayeditor.businessobjects.drawingprimitives.IShapeNode;
import org.pathwayeditor.businessobjects.drawingprimitives.properties.IAnnotationProperty;
import org.pathwayeditor.businessobjects.impl.facades.LabelNodeFactoryFacade;
import org.pathwayeditor.businessobjects.impl.facades.ShapeNodeFacade;
import org.pathwayeditor.businessobjects.notationsubsystem.INotationSyntaxService;
import org.pathwayeditor.businessobjects.typedefn.ILabelObjectType;
import org.pathwayeditor.figure.geometry.Envelope;
import org.pathwayeditor.visualeditor.controller.ShapeFigureControllerHelper;
import org.pathwayeditor.visualeditor.layout.ILabelPositionCalculator;

import uk.ac.ed.inf.graph.compound.ICompoundNode;
import uk.ac.ed.inf.graph.state.IGraphState;

public class LabelCreationCommand implements ICommand {
	private final Logger logger = Logger.getLogger(this.getClass());
	private final IAnnotationProperty prop;
	private IGraphState newState;
	private IGraphState oldState;
	private ILabelPositionCalculator labelPositionCalculator;
	
	public LabelCreationCommand(IAnnotationProperty prop, ILabelPositionCalculator labelPosCalc) {
		this.prop = prop;
		this.labelPositionCalculator = labelPosCalc;
	}
	

	@Override
	public void execute() {
		ILabelNodeFactory labelFactory = new LabelNodeFactoryFacade(prop.getOwner().getCurrentElement().getChildCompoundGraph().nodeFactory());
		this.oldState = labelFactory.getGraphElementFactory().getGraph().getCurrentState();
		labelFactory.setProperty(prop);
		ILabelNode newNode = labelFactory.createLabelNode();
		this.newState = newNode.getGraphElement().getGraph().getCurrentState();
		createShapeLabels();
	}

	private void createShapeLabels(){
		IShapeNode shapeNode = new ShapeNodeFacade((ICompoundNode)prop.getOwner().getCurrentElement());
		IShapeAttribute owningShapeAtt = shapeNode.getAttribute();
		INotationSyntaxService syntaxService = owningShapeAtt.getModel().getNotationSubsystem().getSyntaxService();
		ShapeFigureControllerHelper figureHelper = new ShapeFigureControllerHelper(owningShapeAtt);
		figureHelper.createFigureController();
		Iterator<IAnnotationProperty> defnIter = shapeNode.getAttribute().propertyIterator();
		while(defnIter.hasNext()){
			IAnnotationProperty defn = defnIter.next();
			if(syntaxService.isVisualisableProperty(defn.getDefinition())){
				ILabelObjectType labelObjectType = syntaxService.getLabelObjectTypeByProperty(defn.getDefinition());
				if(labelObjectType.isAlwaysDisplayed()){
					String defaultText = getDisplayedLabelText(labelObjectType, defn);
					ILabelNodeFactory labelFact = new LabelNodeFactoryFacade(shapeNode.getGraphElement().getChildCompoundGraph().nodeFactory());
					// display props that are always displayed
					labelFact.setProperty(defn);
					ILabelNode labelNode = labelFact.createLabelNode();
					if(logger.isDebugEnabled()){
						logger.debug("Create labelNode=" + labelNode + ", bounds=" + labelNode.getAttribute().getBounds());
					}
					Envelope labelBounds = this.labelPositionCalculator.calculateLabelPosition(figureHelper.getFigureController(), labelObjectType, defaultText);
					labelNode.getAttribute().setBounds(labelBounds);
				}
			}
		}
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
	public void undo() {
		this.oldState.getGraph().restoreState(oldState);
	}

	@Override
	public void redo() {
		this.newState.getGraph().restoreState(newState);
	}

}
