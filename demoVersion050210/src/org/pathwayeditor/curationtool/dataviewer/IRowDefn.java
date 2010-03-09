package org.pathwayeditor.curationtool.dataviewer;

import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

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
public interface IRowDefn {
  /**
   * getColumnClass
   *
   * @param col int
   * @return Class
   */
  Class<?> getColumnClass(int col);

  /**
   * isColumnEditable
   *
   * @param col int
   * @return boolean
   */
  boolean isColumnEditable(int col);

  /**
   * getNumColumns
   *
   * @return int
   */
  int getNumColumns();

  /**
   * getColumnHeader
   *
   * @param col int
   * @return Object
   */
  Object getColumnHeader(int col);

  /**
   * isColumnResizable
   *
   * @param col int
   * @return boolean
   */
  boolean isColumnResizable(int col);

  /**
   * getCustomRenderer
   *
   * @param col int
   * @return boolean
   */
  TableCellRenderer getCustomRenderer(int col);

  /**
   * getCellEditor
   *
   * @param col int
   * @return TableCellEditor
   */
  TableCellEditor getCellEditor(int col);

  int getPreferredWidth(int i);
}
