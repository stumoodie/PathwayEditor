package org.pathwayeditor.curationtool.dataviewer;

import java.util.Collection;
import java.util.Set;

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
public interface IDataViewTableModel {

  void commitChanges();

 // row is appended to end of table
  void appendRow(ITableRow row);

  void insertAll(Collection<ITableRow> rows);

  // deletes row from table
  void deleteRow(int rowId);

  ITableRow getRow(int rowId);

  /**
   * deleteAllRows
   */
  void deleteAllRows();


  void rollbackChanges();

  /**
   * Returns the number of rows in the model.
   *
   * @return the number of rows in the model
   */
  int getRowCount();

  Object getValueAt(int rowIndex, int columnIndex);

  void setValueAt(Object newValue, int row, int col);

  Set<ITableRow> getUpdateRows();

  Set<ITableRow> getDeletedRows();

  Set<ITableRow> getInsertedRows();
}
