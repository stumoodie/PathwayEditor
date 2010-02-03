package org.pathwayeditor.curationtool.sentences;

import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import org.pathwayeditor.curationtool.dataviewer.IRowDefn;

public class SentenceRowDefinition implements IRowDefn {
	private static final String HEADER_ROW[] = { "Status", "PMID", "Sentence #", "Interacting Node Name", "Score", "Sentence" }; 
	private static final TableCellRenderer CUSTOM_RENDERER[] =  { new ValidityRenderer(), null, null, null, null, null };
	private static final Class<?> COLUMN_CLASS[] =  { Boolean.class, String.class, Integer.class, String.class, Number.class, String.class };
	private static final int PREF_WIDTH[] =  { 40, 90, 70, 130, 80, 600 };

	
	@Override
	public TableCellEditor getCellEditor(int col) {
		return null;
	}

	@Override
	public Class<?> getColumnClass(int col) {
		return COLUMN_CLASS[col];
	}

	@Override
	public Object getColumnHeader(int col) {
		return HEADER_ROW[col];
	}

	@Override
	public TableCellRenderer getCustomRenderer(int col) {
		return CUSTOM_RENDERER[col];
	}

	@Override
	public int getNumColumns() {
		return HEADER_ROW.length;
	}

	@Override
	public boolean isColumnEditable(int col) {
		return false;
	}

	@Override
	public boolean isColumnResizable(int col) {
		return true;
	}

	@Override
	public int getPreferredWidth(int i) {
		return PREF_WIDTH[i];
	}

}
