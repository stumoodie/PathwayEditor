package org.pathwayeditor.curationtool.sentences;

import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import org.pathwayeditor.curationtool.dataviewer.IRowDefn;

public class SentenceRowDefinition implements IRowDefn {
	private static final String HEADER_ROW[] = { "Name", "Sentence", "PMID", "Score" }; 
	private static final TableCellRenderer CUSTOM_RENDERER[] =  { null, null, null, null };
	private static final Class<?> COLUMN_CLASS[] =  { String.class, String.class, Integer.class, Number.class };
	private static final int PREF_WIDTH[] =  { 200, 600, 100, 100 };

	
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
