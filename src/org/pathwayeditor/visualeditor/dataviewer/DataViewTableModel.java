package org.pathwayeditor.visualeditor.dataviewer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.table.AbstractTableModel;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: DSTT</p>
 *
 * @author Stuart L. Moodie
 * @version 1.0
 */
public class DataViewTableModel extends AbstractTableModel implements IDataViewTableModel {
  /**
	 * 
	 */
	private static final long serialVersionUID = -6397496979721622024L;
private List<ITableRow> initialData = new ArrayList<ITableRow>();
  private List<ITableRow> tableData = new ArrayList<ITableRow>();
  private Set<ITableRow> deletedRows = new TreeSet<ITableRow>();
  private Set<ITableRow> insertedRows = new TreeSet<ITableRow>();
  private Set<ITableRow> updatedRows = new TreeSet<ITableRow>();

  private final IRowDefn rowDefn;


  DataViewTableModel(IRowDefn rowDefn) {
    super();
    this.rowDefn = rowDefn;
  }

  public synchronized void commitChanges() {
    this.initialData.clear();
    for(ITableRow row : this.tableData){
      this.initialData.add(row.getCopy());
    }
    this.insertedRows.clear();
    this.updatedRows.clear();
    this.deletedRows.clear();
  }

  /*public synchronized void loadRow(ITableRow row){
   tableData.add(row.getCopy());
   this.initialData.add(row.getCopy());
 }*/

 // row is appended to end of table
  public synchronized void appendRow(ITableRow row) {
    ITableRow localRow = row.getCopy();
    this.tableData.add(localRow);
    int rowPos = tableData.size()-1;
    this.insertedRows.add(localRow);
    this.fireTableRowsInserted(rowPos, rowPos);
  }

  public synchronized void insertAll(Collection<ITableRow> rows) {
    int firstRow = tableData.size()-1;
    for(ITableRow extlRow : rows){
      ITableRow localRow = extlRow.getCopy();
      this.tableData.add(localRow);
      this.insertedRows.add(localRow);
    }
    int lastRow = tableData.size()-1;
    // only fire if you have actually added some rows
    // the rows collection could be empty
    if(lastRow > firstRow){
      this.fireTableRowsInserted(firstRow, lastRow);
    }
  }

  // deletes row from table
  public synchronized void deleteRow(int rowId){
    this.deletedRows.add(tableData.remove(rowId));
    this.fireTableRowsDeleted(rowId, rowId);
  }

  /**
   * deleteAllRows
   */
  public synchronized void deleteAllRows() {
    int lastRow = tableData.size() - 1;
    // copy all of table to deleted list
    this.deletedRows.addAll(tableData);
    // clear table
    tableData.clear();
    // only fire if you have actually deleted some rows
    // the rows collection could be empty
    if(lastRow >= 0){
      this.fireTableRowsDeleted(0, lastRow);
    }
  }


  public synchronized void rollbackChanges() {
//    int origRowCount = tableData.size();
    tableData.clear();
    for(ITableRow row : this.initialData){
      this.tableData.add(row.getCopy());
    }
    this.insertedRows.clear();
    this.updatedRows.clear();
    this.deletedRows.clear();
//    int resetRowCount = tableData.size();
    this.fireTableDataChanged();
    /*if(origRowCount > resetRowCount){
      this.fireTableRowsDeleted(resetRowCount, origRowCount -1);
    }
    else if(origRowCount < resetRowCount){
      this.fireTableRowsInserted(origRowCount, resetRowCount -1);
    }
    fireTableRowsUpdated(0, resetRowCount - 1);*/
  }

  /**
   * Returns the number of columns in the model.
   *
   * @return the number of columns in the model
   */
  public synchronized int getColumnCount() {
    return rowDefn.getNumColumns();
  }


  public synchronized Class<?> getColumnClass(int col) {
    return rowDefn.getColumnClass(col);
  }

  public synchronized boolean isCellEditable(int row, int col){
    return this.tableData.get(row).getRowDefn().isColumnEditable(col);
  }


  /**
   * Returns the number of rows in the model.
   *
   * @return the number of rows in the model
   */
  public synchronized int getRowCount() {
    return this.tableData.size();
  }

  /**
   * Returns the value for the cell at <code>columnIndex</code> and
   * <code>rowIndex</code>.
   *
   * @param rowIndex the row whose value is to be queried
   * @param columnIndex the column whose value is to be queried
   * @return the value Object at the specified cell
   */
  public synchronized Object getValueAt(int rowIndex, int columnIndex) {
    return this.tableData.get(rowIndex).getColumnValue(columnIndex);
  }


  public synchronized void setValueAt(Object newValue, int row, int col){
    ITableRow currRow = this.tableData.get(row);
    currRow.setColumnValue(col, newValue);
    this.updatedRows.add(currRow);
    this.fireTableRowsUpdated(row, row);
    this.fireTableCellUpdated(row, col);
  }

  /**
   * getRowDefn
   *
   * @return IRowDefn
   */
  public synchronized IRowDefn getRowDefn() {
    return this.rowDefn;
  }


  public synchronized Set<ITableRow> getUpdateRows() {
    Set<ITableRow> retVal = new TreeSet<ITableRow>(this.updatedRows);
    // need to remove rows that were inserted since this will be handled
    // as an insertion - there and the updates will be present in the inserted row.
    retVal.removeAll(this.insertedRows);
    // if the row has been deleted then this takes precedence over the update
    retVal.removeAll(this.deletedRows);
    return retVal;
  }

  public synchronized Set<ITableRow> getDeletedRows() {
    Set<ITableRow> retVal = new TreeSet<ITableRow>(this.deletedRows);
    // need to remove rows that were inserted since they are not already
    // stored, i.e. there has been no overall change to the table from its initial
    // state
    retVal.removeAll(this.insertedRows);
    return retVal;
  }

  public synchronized Set<ITableRow> getInsertedRows() {
    Set<ITableRow> retVal = new TreeSet<ITableRow>(this.insertedRows);
    // need to remove rows that were deleted since they are not already
    // stored, i.e. there has been no overall change to the table from its initial
    // state
    retVal.removeAll(this.deletedRows);
    return retVal;
  }

  public synchronized ITableRow getRow(int rowId) {
    return this.tableData.get(rowId).getCopy();
  }

}
