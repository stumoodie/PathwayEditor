package org.pathwayeditor.curationtool.sentences;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.pathwayeditor.curationtool.dataviewer.DataViewEvent;
import org.pathwayeditor.curationtool.dataviewer.DataViewListener;
import org.pathwayeditor.curationtool.dataviewer.DataViewTableModel;
import org.pathwayeditor.curationtool.dataviewer.IRowDefn;
import org.pathwayeditor.notations.annotator.ndom.ISentence;
import org.pathwayeditor.notations.annotator.ndom.ISentenceStateChangeListener;
import org.pathwayeditor.notations.annotator.ndom.impl.ISentenceStateChangeEvent;

public class SentencesPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private BorderLayout borderLayout1 = new BorderLayout();
	private DataViewTableModel tableModel;
	private JScrollPane dataViewScrollPane = new JScrollPane();
	private TableColumnModel tableColumnModel;
	private JTable dataViewTable;
	private javax.swing.JPanel actionPanel = new JPanel();
	private transient List<DataViewListener> dataViewListeners = new LinkedList<DataViewListener>();
	private ListSelectionModel selectionModel;
	private JButton prevButton;
	private JButton nextButton;
	private ISentence currentSentence;
	private final List<ISentenceSelectionChangeListener> listeners;
	private JCheckBox irrelevantCheckBox;
	private JCheckBox focusCheckBox;
	private JCheckBox interactingNodeCheckBox;
	private ISentenceStateChangeListener sentenceListener;
	
	public SentencesPanel(){
		IRowDefn rowDefn = new SentenceRowDefinition();
		tableModel = new DataViewTableModel(rowDefn);
		tableColumnModel = new DefaultTableColumnModel();
		dataViewTable = new JTable(tableModel, tableColumnModel);
		dataViewTable.setAutoscrolls(true);
		selectionModel = dataViewTable.getSelectionModel();
		selectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		setColumnDefs();
		this.setLayout(borderLayout1);
		dataViewScrollPane.getViewport().add(dataViewTable);
		this.add(dataViewScrollPane, BorderLayout.CENTER);
		this.add(actionPanel, BorderLayout.SOUTH);
		setupActionPanel();
		dataViewTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		this.listeners = new LinkedList<ISentenceSelectionChangeListener>();
	    this.setBorder(BorderFactory.createLineBorder(Color.BLACK));
	    this.sentenceListener = new ISentenceStateChangeListener() {
			
			@Override
			public void stateChanged(ISentenceStateChangeEvent e) {
				updateButtons();
//				if(e.getStateType() == StateType.FOCUS_VALIDITY_STATE){
//					disableButtonListeners();
//					focusCheckBox.setEnabled(e.getCurrentStateValue());
//					enableButtonListeners();
//				}
//				else if(e.getStateType() == StateType.INTERACTING_VALIDITY_STATE){
//					disableButtonListeners();
//					interactingNodeCheckBox.setEnabled(e.getCurrentStateValue());
//					enableButtonListeners();
//				}
//				else if(e.getStateType() == StateType.RELEVANT_STATE){
//					disableButtonListeners();
//					irrelevantCheckBox.setEnabled(!e.getCurrentStateValue());
//					enableButtonListeners();
//				}
			}
		};
	}

	private void notifySentenceSelectionChanged(final int idx, final ISentence oldSentence, final ISentence currentSentence) {
		ISentenceSelectionChangeEvent e = new ISentenceSelectionChangeEvent(){

			@Override
			public ISentence getSelectedSentence() {
				return currentSentence;
			}

			@Override
			public int getCurrentRowIdx() {
				return idx;
			}

			@Override
			public ISentence getOldSentence() {
				return oldSentence;
			}
			
		};
		for(ISentenceSelectionChangeListener l : this.listeners){
			l.sentenceSelectionChangeEvent(e);
		}
	}

	public ISentence getCurrentlySelectedSentence(){
		return this.currentSentence;
	}
	
	public void loadData(Iterator<ISentence> sentenceIter){
	    this.tableModel.deleteAllRows();
	    while(sentenceIter.hasNext()){
//	    for(final String rowData[] : testData){
	    	final ISentence sentence = sentenceIter.next();
	    	this.tableModel.appendRow(new SentenceRow(sentence));
	    }
	    this.tableModel.commitChanges();
	    this.resetSelection();
	    intialise();
	}
	
	
	private void intialise() {
		updateButtons();
		this.currentSentence.addSentenceStateChangeListener(sentenceListener);
		this.selectionModel.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				checkNavigatorButtonEnablement();
				currentSentence.removeSentenceStateChangeListener(sentenceListener);
				ISentence oldSentence = currentSentence;
				currentSentence = ((SentenceRow)tableModel.getRow(getCurrentSelection())).getSentence();
				notifySentenceSelectionChanged(getCurrentSelection(), oldSentence, currentSentence);
				updateButtons();
				currentSentence.addSentenceStateChangeListener(sentenceListener);
			}
		});
	}

	public void addSentenceSelectionChangeListener(ISentenceSelectionChangeListener l){
		this.listeners.add(l);
	}
	
	public void removeSentenceSelectionChangeListener(ISentenceSelectionChangeListener l){
		this.listeners.remove(l);
	}
	
	public List<ISentenceSelectionChangeListener> getSentenceSelectionChangeListeners(){
		return new ArrayList<ISentenceSelectionChangeListener>(this.listeners);
	}
	
	public ISentence getSelectedSentence(){
		return currentSentence;
	}
	
	private void setupActionPanel() {
		irrelevantCheckBox = addCheckBox("Sentence Irrelevant");
		focusCheckBox = addCheckBox("Focus Node OK");
		interactingNodeCheckBox = addCheckBox("Interacting Node OK");
		irrelevantCheckBox.addChangeListener(new ChangeListener(){
			@Override
			public void stateChanged(ChangeEvent e) {
				currentSentence.setSentenceRelevant(!irrelevantCheckBox.isSelected());
			}
			
		});
		focusCheckBox.addChangeListener(new ChangeListener(){
			@Override
			public void stateChanged(ChangeEvent e) {
				currentSentence.setFocusNodeValid(focusCheckBox.isSelected());
			}
		});
		interactingNodeCheckBox.addChangeListener(new ChangeListener(){
			@Override
			public void stateChanged(ChangeEvent e) {
				currentSentence.setInteractingNodeValid(interactingNodeCheckBox.isSelected());
			}
		});
		prevButton = new JButton("Previous");
		this.actionPanel.add(prevButton);
		prevButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int currSelectionIdx = getCurrentSelection()-1;
				selectionModel.setSelectionInterval(currSelectionIdx, currSelectionIdx);
				int scrollOffset = dataViewTable.getRowHeight() * getCurrentSelection();
				dataViewScrollPane.getVerticalScrollBar().setValue(scrollOffset);
			}
		});
		nextButton = new JButton("Next");
		this.actionPanel.add(nextButton);
		nextButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int currSelectionIdx = getCurrentSelection()+1;
				selectionModel.setSelectionInterval(currSelectionIdx, currSelectionIdx);
				int scrollOffset = dataViewTable.getRowHeight() * getCurrentSelection();
				dataViewScrollPane.getVerticalScrollBar().setValue(scrollOffset);
			}
		});
	}

	private void checkNavigatorButtonEnablement(){
		int currSelectionIdx = getCurrentSelection();
		prevButton.setEnabled(currSelectionIdx > 0);
		nextButton.setEnabled(currSelectionIdx < tableModel.getRowCount()-1);
	}
	
	private JCheckBox addCheckBox(String labelText) {
		JLabel label = new JLabel(labelText);
		JCheckBox checkBox = new JCheckBox();
		checkBox.setSelected(false);
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridLayout(1, 2));
		buttonPanel.add(label);
		buttonPanel.add(checkBox);
		this.actionPanel.add(buttonPanel);
		return checkBox;
	}

	private void setColumnDefs() {
		IRowDefn rowDefn = this.tableModel.getRowDefn();
		for (int i = 0; i < rowDefn.getNumColumns(); i++) {
			TableColumn colDefn = new TableColumn();
			colDefn.setHeaderValue(i);
			colDefn.setHeaderValue(rowDefn.getColumnHeader(i));
			colDefn.setResizable(rowDefn.isColumnResizable(i));
			colDefn.setPreferredWidth(rowDefn.getPreferredWidth(i));
			if (rowDefn.getCustomRenderer(i) != null) {
				colDefn.setCellRenderer(rowDefn.getCustomRenderer(i));
			}
			if (rowDefn.getCellEditor(i) != null) {
				colDefn.setCellEditor(rowDefn.getCellEditor(i));
			}
			colDefn.setModelIndex(i);
			tableColumnModel.addColumn(colDefn);
		}
	}

	public void addDataViewListener(DataViewListener l) {
		this.dataViewListeners.add(l);
	}

	public void removeDataViewListener(DataViewListener l) {
		this.dataViewListeners.remove(l);
	}

	protected void fireRowInserted(DataViewEvent event) {
		for (DataViewListener listener : dataViewListeners) {
			listener.rowInserted(event);
		}
	}

	protected void fireRowDeleted(DataViewEvent event) {
		for (DataViewListener listener : dataViewListeners) {
			listener.rowDeleted(event);
		}
	}

	protected void fireViewSaved(DataViewEvent event) {
		for (DataViewListener listener : dataViewListeners) {
			listener.viewSaved(event);
		}
	}

	protected void fireViewReset(DataViewEvent event) {
		for (DataViewListener listener : dataViewListeners) {
			listener.viewReset(event);
		}
	}

	public DataViewTableModel getTableModel() {
		return this.tableModel;
	}

	private int getCurrentSelection(){
		return selectionModel.getAnchorSelectionIndex();
	}
	
	public void resetSelection() {
		selectionModel.setSelectionInterval(0, 0);
		currentSentence = ((SentenceRow)tableModel.getRow(getCurrentSelection())).getSentence();
	}

	private void updateButtons() {
		this.irrelevantCheckBox.setSelected(!this.currentSentence.isSentenceRelevant());
		this.focusCheckBox.setSelected(this.currentSentence.isFocusNodeValid());
		this.interactingNodeCheckBox.setSelected(this.currentSentence.isInteractingNodeValid());
		irrelevantCheckBox.setEnabled(focusCheckBox.isSelected() && interactingNodeCheckBox.isSelected());
	}

}
