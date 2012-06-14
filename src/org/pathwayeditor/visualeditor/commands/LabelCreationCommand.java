package org.pathwayeditor.visualeditor.commands;

import java.text.Format;

import org.apache.log4j.Logger;
import org.pathwayeditor.businessobjects.drawingprimitives.ICanvasElementAttribute;
import org.pathwayeditor.businessobjects.drawingprimitives.ILabelAttribute;
import org.pathwayeditor.businessobjects.drawingprimitives.ILabelAttributeFactory;
import org.pathwayeditor.businessobjects.drawingprimitives.IModel;
import org.pathwayeditor.businessobjects.drawingprimitives.IShapeAttribute;
import org.pathwayeditor.businessobjects.drawingprimitives.properties.IAnnotationProperty;
import org.pathwayeditor.businessobjects.notationsubsystem.INotationSyntaxService;
import org.pathwayeditor.businessobjects.typedefn.ILabelObjectType;
import org.pathwayeditor.figure.geometry.Envelope;
import org.pathwayeditor.visualeditor.controller.ShapeFigureControllerHelper;
import org.pathwayeditor.visualeditor.layout.ILabelPositionCalculator;

import uk.ac.ed.inf.graph.compound.ICompoundNode;
import uk.ac.ed.inf.graph.compound.ICompoundNodeFactory;
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
		ICompoundNodeFactory labelFactory = prop.getOwner().getCurrentElement().getChildCompoundGraph().nodeFactory();
		this.oldState = labelFactory.getGraph().getCurrentState();
		IModel att = ((ICanvasElementAttribute)prop.getOwner()).getModel();
		ILabelAttributeFactory labelAttFact = att.labelAttributeFactory();
		labelAttFact.setProperty(prop);
		ILabelObjectType labelObjectType = att.getNotationSubsystem().getSyntaxService().getLabelObjectTypeByProperty(prop.getDefinition());
		labelAttFact.setLabelObjectType(labelObjectType);
		labelFactory.setAttributeFactory(labelAttFact);
		ICompoundNode newNode = labelFactory.createNode();
		this.newState = newNode.getGraph().getCurrentState();
		ILabelAttribute labelAtt = (ILabelAttribute)newNode.getAttribute();
		createShapeLabels(labelAtt);
		if(logger.isDebugEnabled()){
			logger.debug("Created label. Att=" + labelAtt + ", bounds=" + labelAtt.getBounds());
		}
	}

	private void createShapeLabels(ILabelAttribute labelAtt){
		IShapeAttribute owningShapeAtt = (IShapeAttribute)prop.getOwner();
		INotationSyntaxService syntaxService = owningShapeAtt.getModel().getNotationSubsystem().getSyntaxService();
		ShapeFigureControllerHelper figureHelper = new ShapeFigureControllerHelper(owningShapeAtt);
		figureHelper.createFigureController();
		ILabelObjectType labelObjectType = syntaxService.getLabelObjectTypeByProperty(this.prop.getDefinition());
		String defaultText = getDisplayedLabelText(labelObjectType, this.prop);
		Envelope labelBounds = this.labelPositionCalculator.calculateLabelPosition(figureHelper.getFigureController(), labelObjectType, defaultText);
		labelAtt.setBounds(labelBounds);
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
