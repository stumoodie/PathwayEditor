package org.pathwayeditor.visualeditor.sentences;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import org.pathwayeditor.visualeditor.dataviewer.DataViewPanel;
import org.pathwayeditor.visualeditor.dataviewer.IRowDefn;
import org.pathwayeditor.visualeditor.dataviewer.ITableRow;

public class SentencesPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	
	private final String testData[][] = {
			{ "Foo", "This is a sentence", "18362653", "122.22" },
			{ "Bar", "This is 2nd sentence", "18962653", "2.22" },
			{ "taa", "This is 3rd sentence", "2653", "225.22" },
			{ "Bar", "This is 2nd sentence", "18962653", "2.22" },
			{ "taa", "This is 3rd sentence", "2653", "225.22" },
			{ "Bar", "This is 2nd sentence", "18962653", "2.22" },
			{ "taa", "This is 3rd sentence", "2653", "225.22" },
			{ "Bar", "This is 2nd sentence", "18962653", "2.22" },
			{ "taa", "This is 3rd sentence", "2653", "225.22" },
			{ "Bar", "This is 2nd sentence", "18962653", "2.22" },
			{ "taa", "This is 3rd sentence", "2653", "225.22" },
			{ "taa", "This is 3rd sentence", "2653", "225.22" },
			{ "Bar", "This is 2nd sentence", "18962653", "2.22" },
			{ "taa", "This is 3rd sentence", "2653", "225.22" },
			{ "taa", "This is 3rd sentence", "2653", "225.22" },
			{ "Bar", "This is 2nd sentence", "18962653", "2.22" },
			{ "taa", "This is 3rd sentence", "2653", "225.22" },
			{ "taa", "This is 3rd sentence", "2653", "225.22" },
			{ "Bar", "This is 2nd sentence", "18962653", "2.22" },
			{ "taa", "This is 3rd sentence", "2653", "225.22" },
			{ "taa", "This is 3rd sentence", "2653", "225.22" },
			{ "Bar", "This is 2nd sentence", "18962653", "2.22" },
			{ "taa", "This is 3rd sentence", "2653", "225.22" },
	};

	private final DataViewPanel dataViewPanel;
	
	public SentencesPanel(){
		this.setLayout(new BorderLayout());
		this.dataViewPanel = new DataViewPanel(new SentenceRowDefinition());
	    this.add(this.dataViewPanel, BorderLayout.CENTER);
	}
	
	public void loadData(){
	    this.dataViewPanel.getTableModel().deleteAllRows();
	    for(final String rowData[] : testData){
	      this.dataViewPanel.getTableModel().appendRow(new ITableRow(){

			@Override
			public Object getColumnValue(int col) {
				return rowData[col];
			}

			@Override
			public ITableRow getCopy() {
				return this;
			}

			@Override
			public IRowDefn getRowDefn() {
				return dataViewPanel.getTableModel().getRowDefn();
			}

			@Override
			public void setColumnValue(int col, Object newValue) {
				// do nothing here!
			}

			@Override
			public int compareTo(Object o) {
				ITableRow otherRow = (ITableRow)o;
				return rowData[0].compareTo((String)otherRow.getColumnValue(0));
			}
	      });
	    }
	    this.dataViewPanel.getTableModel().commitChanges();
	    this.dataViewPanel.resetSelection();
	}
	
}
