/*
  Licensed to the Court of the University of Edinburgh (UofE) under one
  or more contributor license agreements.  See the NOTICE file
  distributed with this work for additional information
  regarding copyright ownership.  The UofE licenses this file
  to you under the Apache License, Version 2.0 (the
  "License"); you may not use this file except in compliance
  with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing,
  software distributed under the License is distributed on an
  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  KIND, either express or implied.  See the License for the
  specific language governing permissions and limitations
  under the License.
*/
package org.pathwayeditor.visualeditor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;

import org.apache.log4j.Logger;
import org.pathwayeditor.businessobjects.drawingprimitives.ILinkAttribute;
import org.pathwayeditor.businessobjects.drawingprimitives.IModel;
import org.pathwayeditor.businessobjects.notationsubsystem.INotationSubsystem;
import org.pathwayeditor.figure.geometry.Envelope;
import org.pathwayeditor.visualeditor.IPathwayEditorStateChangeEvent.StateChangeType;
import org.pathwayeditor.visualeditor.behaviour.IViewBehaviourController;
import org.pathwayeditor.visualeditor.behaviour.ViewBehaviourController;
import org.pathwayeditor.visualeditor.commands.CommandStack;
import org.pathwayeditor.visualeditor.commands.DeleteSelectionCommand;
import org.pathwayeditor.visualeditor.commands.ICommandChangeEvent;
import org.pathwayeditor.visualeditor.commands.ICommandChangeEvent.CommandChangeType;
import org.pathwayeditor.visualeditor.commands.ICommandChangeListener;
import org.pathwayeditor.visualeditor.commands.ICommandStack;
import org.pathwayeditor.visualeditor.controller.IDrawingElementController;
import org.pathwayeditor.visualeditor.controller.IViewControllerModel;
import org.pathwayeditor.visualeditor.controller.ViewControllerStore;
import org.pathwayeditor.visualeditor.editingview.DomainModelLayer;
import org.pathwayeditor.visualeditor.editingview.FeedbackLayer;
import org.pathwayeditor.visualeditor.editingview.IShapePane;
import org.pathwayeditor.visualeditor.editingview.SelectionLayer;
import org.pathwayeditor.visualeditor.editingview.ShapePane;
import org.pathwayeditor.visualeditor.feedback.FeedbackModel;
import org.pathwayeditor.visualeditor.feedback.IFeedbackModel;
import org.pathwayeditor.visualeditor.geometry.EnvelopeBuilder;
import org.pathwayeditor.visualeditor.operations.LabelPropValueDialog;
import org.pathwayeditor.visualeditor.operations.OperationFactory;
import org.pathwayeditor.visualeditor.selection.ISelection;
import org.pathwayeditor.visualeditor.selection.ISelectionChangeEvent;
import org.pathwayeditor.visualeditor.selection.ISelectionChangeListener;
import org.pathwayeditor.visualeditor.selection.ISelectionRecord;
import org.pathwayeditor.visualeditor.selection.SelectionRecord;

import uk.ac.ed.inf.graph.compound.ICompoundEdge;

public class PathwayEditor extends JPanel {
	private static final double REFRESH_EXPANSION_Y = 20.0;
	private static final double REFRESH_EXPANSION_X = REFRESH_EXPANSION_Y;
	private static final long serialVersionUID = 1L;
	private final Logger logger = Logger.getLogger(this.getClass());
	private IShapePane shapePane;
	private JScrollPane scrollPane;
	private PalettePanel palettePane;
	private IViewControllerModel viewModel;
	private ISelectionRecord selectionRecord;
	private final ICommandStack commandStack;
	private IViewBehaviourController editBehaviourController;
	private final ISelectionChangeListener selectionChangeListener;
	private IFeedbackModel feedbackModel;
	private boolean isOpen = false;
	private ILayoutCalculator layoutCalculator;
	private List<IPathwayEditorStateChangeListener> listeners = new LinkedList<IPathwayEditorStateChangeListener>();
//	private final IGraphStructureChangeListener graphStuctureChangeListener;
	private final ICommandChangeListener commandStackListener;
	private Dialog dialog;
	private JLabel nameHeader;

	public PathwayEditor(Dialog dialog){
		super();
		this.dialog = dialog;
		this.setLayout(new BorderLayout());
		this.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		this.commandStackListener = new ICommandChangeListener() {
			
			@Override
			public void notifyCommandChange(ICommandChangeEvent e) {
				if(e.getChangeType().equals(CommandChangeType.EXECUTE)){
					notifyStateChange(StateChangeType.EDITED, viewModel.getDomainModel());
					updateNameHeader();
				}
				else if(e.getChangeType().equals(CommandChangeType.REDO)){
					notifyStateChange(StateChangeType.EDITED, viewModel.getDomainModel());
					updateNameHeader();
					selectionRecord.restoreSelection();
					shapePane.updateView();
				}
				else if(e.getChangeType().equals(CommandChangeType.UNDO)){
					if(!commandStack.canUndo()){
						notifyStateChange(StateChangeType.UNEDITED, viewModel.getDomainModel());
						updateNameHeader();
					}
					selectionRecord.restoreSelection();
					shapePane.updateView();
				}
				else if(e.getChangeType().equals(CommandChangeType.CLEAR)){
					notifyStateChange(StateChangeType.UNEDITED, viewModel.getDomainModel());
					updateNameHeader();
				}
			}
		};
		this.selectionChangeListener = new ISelectionChangeListener() {

			@Override
			public void selectionChanged(ISelectionChangeEvent event) {
				EnvelopeBuilder builder = null;
				Iterator<ISelection> oldIter = event.oldSelectionIter();
				while (oldIter.hasNext()) {
					ISelection seln = oldIter.next();
					if (logger.isTraceEnabled()) {
						logger.trace("Union old selection drawnBounds="
								+ seln.getPrimitiveController()
										.getDrawnBounds());
					}
					Envelope bounds = seln.getPrimitiveController()
							.getDrawnBounds();
					if (builder == null) {
						builder = new EnvelopeBuilder(bounds);
					} else {
						builder.union(bounds);
					}
				}
				if (builder != null) {
					builder.expand(REFRESH_EXPANSION_X, REFRESH_EXPANSION_Y);
					Envelope refreshBounds = builder.getEnvelope();
					if (logger.isTraceEnabled()) {
						logger.trace("Unselection refresh bounds: "
								+ refreshBounds);
					}
					shapePane.updateView(refreshBounds);
				} else {
					logger.debug("No old selection bounds identified");
				}
				builder = null;
				Iterator<ISelection> newIter = event.newSelectionIter();
				while (newIter.hasNext()) {
					ISelection seln = newIter.next();
					Envelope bounds = seln.getPrimitiveController()
							.getDrawnBounds();
					if (logger.isTraceEnabled()) {
						logger.trace("Union new selection drawnBounds="
								+ seln.getPrimitiveController()
										.getDrawnBounds());
					}
					if (builder == null) {
						builder = new EnvelopeBuilder(bounds);
					} else {
						builder.union(bounds);
					}
				}
				if (builder != null) {
					builder.expand(REFRESH_EXPANSION_X, REFRESH_EXPANSION_Y);
					Envelope refreshBounds = builder.getEnvelope();
					if (logger.isTraceEnabled()) {
						logger.trace("Selection refresh bounds: "
								+ refreshBounds);
					}
					shapePane.updateView(refreshBounds);
				} else {
					logger.debug("No new selection bounds identified");
				}
			}
		};
//		this.graphStuctureChangeListener = new IGraphStructureChangeListener() {
//			@Override
//			public void graphStructureChange(IGraphStructureChangeAction iGraphStructureChangeAction) {
//			}
//
//			@Override
//			public void notifyRestoreCompleted(IGraphRestoreStateAction e) {
//				selectionRecord.restoreSelection();
//				shapePane.updateView();
//			}
//		};
		this.commandStack = new CommandStack();
	}
	
	protected void updateNameHeader() {
		String headerText = getHeaderText();
		if(!headerText.equals(this.nameHeader.getText())){
			// only set if name has changed.
			nameHeader.setText(getHeaderText());
		}
	}

	public boolean isOpen(){
		return isOpen;
	}
	
	public boolean isEdited(){
		return this.commandStack.canUndo();
	}
	
	public void resetEdited(){
		this.commandStack.clear();
	}
	
	public void close(){
		IModel model = this.viewModel.getDomainModel();
		if(isOpen){
			reset();
			this.repaint();
			notifyStateChange(StateChangeType.CLOSED, model);
		}
		isOpen = false;
	}
	
	public ICommandStack getCommandStack(){
		return this.commandStack;
	}
	
	public void addEditorStateChangeListener(IPathwayEditorStateChangeListener l){
		this.listeners.add(l);
	}
	
	public void removeEditorStateChangeListener(IPathwayEditorStateChangeListener l){
		this.listeners.remove(l);
	}
	
	public List<IPathwayEditorStateChangeListener> getEditorStateChangeListeners(){
		return new ArrayList<IPathwayEditorStateChangeListener>(this.listeners);
	}
	
	private void notifyStateChange(final StateChangeType changeType, final IModel model){
		IPathwayEditorStateChangeEvent e = new IPathwayEditorStateChangeEvent(){
			@Override
			public PathwayEditor getSource() {
				return PathwayEditor.this;
			}

			@Override
			public IModel getModel() {
				return model;
			}

			@Override
			public StateChangeType getChangeType() {
				return changeType;
			}
			
		};
		for(IPathwayEditorStateChangeListener l : this.listeners){
			l.editorChangedEvent(e);
		}
	}
	
	private void setUpEditorViews(IModel canvas){
		this.selectionRecord = new SelectionRecord(viewModel);
		this.feedbackModel = new FeedbackModel(selectionRecord);
		this.shapePane = new ShapePane();
		this.shapePane.addLayer(new DomainModelLayer(viewModel));
		this.shapePane.addLayer(new SelectionLayer(selectionRecord));
		this.shapePane.addLayer(new FeedbackLayer(feedbackModel));
		Envelope canvasBounds = this.viewModel.getCanvasBounds();
		this.shapePane.setPaneBounds(canvasBounds);
		scrollPane = new JScrollPane((ShapePane)this.shapePane, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);
		scrollPane.setFocusable(true);
		scrollPane.setWheelScrollingEnabled(true);
		LabelPropValueDialog labelDialog = new LabelPropValueDialog(this.dialog);
        this.editBehaviourController = new ViewBehaviourController(shapePane, new OperationFactory(this.shapePane, this.feedbackModel, this.selectionRecord, viewModel, this.commandStack, labelDialog));
        INotationSubsystem notationSubsystem = canvas.getNotationSubsystem();
		this.palettePane = new PalettePanel(notationSubsystem, editBehaviourController);
		this.nameHeader = new JLabel();
		this.nameHeader.setBorder(BorderFactory.createLoweredBevelBorder());
		this.nameHeader.setHorizontalTextPosition(SwingConstants.CENTER);
		this.nameHeader.setHorizontalAlignment(SwingConstants.CENTER);
		this.nameHeader.setPreferredSize(new Dimension(500, 30));
		this.nameHeader.setText(getHeaderText());
		this.add(this.nameHeader, BorderLayout.PAGE_START);
		this.add(palettePane, BorderLayout.LINE_START);
		this.add(scrollPane, BorderLayout.CENTER);
 		this.revalidate();
		this.editBehaviourController.activate();
		this.viewModel.activate();
		this.selectionRecord.addSelectionChangeListener(selectionChangeListener);
		this.shapePane.updateView();
		this.isOpen = true;
		this.commandStack.addCommandChangeListener(commandStackListener);
	}
	
	private String getHeaderText() {
		StringBuilder buf = new StringBuilder();
		buf.append(this.viewModel.getDomainModel().getName());
		if(this.isEdited()){
			buf.append("*");
		}
		return buf.toString();
	}

	/**
	 * Sets the layout calculator to be used for auto-layout of the canvas.
	 * @param layoutCalculator the layout calculator 
	 */
	public void setLayoutCalculator(ILayoutCalculator layoutCalculator){
		this.layoutCalculator = layoutCalculator;
		
	}
	
	/**
	 * Carry out auto-layout on the canvas. Using the layout calculator set in {@link setLayoutCalculator}.
	 * @throws IllegalStateException if the layout calculator is not set
	 */
	public void layoutCanvas(){
		if(this.layoutCalculator != null){
			this.layoutCalculator.calculateLayout();
		}
		else{
			throw new IllegalStateException("No layout calculator was set");
		}
	}
	
	public void renderModel(IModel model){
		if(isOpen){
			reset();
		}
//        this.commandStack = new CommandStack();
		this.viewModel = new ViewControllerStore(model);
		setUpEditorViews(this.viewModel.getDomainModel());
		((ShapePane)this.shapePane).setPreferredSize(new Dimension(1800, 1800));
		((ShapePane)this.shapePane).revalidate();
//		model.getGraph().addGraphStructureChangeListener(graphStuctureChangeListener);
		notifyStateChange(StateChangeType.OPEN, model);
	}

	public IViewControllerModel getViewControllerModel(){
		return this.viewModel;
	}
	
	
	private void reset(){
		this.scrollPane.remove((JComponent)this.shapePane);
		this.remove(this.nameHeader);
		this.remove(scrollPane);
		this.remove(this.palettePane);
		this.validate();
//		this.viewModel.getDomainModel().getGraph().removeGraphStructureChangeListener(graphStuctureChangeListener);
		this.selectionRecord.removeSelectionChangeListener(selectionChangeListener);
		this.commandStack.removeCommandChangeListener(commandStackListener);
		this.commandStack.clear();
		this.viewModel.deactivate();
		this.editBehaviourController.deactivate();
		this.shapePane = null;
		this.scrollPane = null;
		this.selectionRecord = null;
		this.viewModel = null;
		this.editBehaviourController = null;
		this.feedbackModel = null;
	}
	
	public void selectAndFocusOnElement(ICompoundEdge linkEdge) {
		selectionRecord.clear();
		IDrawingElementController linkController = viewModel.getController((ILinkAttribute)linkEdge.getAttribute());
		selectionRecord.setPrimarySelection(linkController);
	}
	
	public ISelectionRecord getSelectionRecord(){
		return this.selectionRecord;
	}

	public void deleteSelection() {
		commandStack.execute(new DeleteSelectionCommand(selectionRecord.getSubgraphSelection()));
		selectionRecord.clear();
		shapePane.updateView();
	}
}
