package org.pathwayeditor.curationtool.dataviewer;

import java.awt.Component;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JLabel;
import javax.swing.JTable;
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
public class MonthYearRenderer implements TableCellRenderer {
  private static final DateFormat DATE_FORMAT = new SimpleDateFormat("MMM yyyy");
  JLabel label = new JLabel();

  /**
   * Returns the component used for drawing the cell.
   *
   * @param table the <code>JTable</code> that is asking the renderer to draw;
   *   can be <code>null</code>
   * @param value the value of the cell to be rendered. It is up to the
   *   specific renderer to interpret and draw the value. For example, if
   *   <code>value</code> is the string "true", it could be rendered as a
   *   string or it could be rendered as a check box that is checked.
   *   <code>null</code> is a valid value
   * @param isSelected true if the cell is to be rendered with the selection
   *   highlighted; otherwise false
   * @param hasFocus if true, render cell appropriately. For example, put a
   *   special border on the cell, if the cell can be edited, render in the
   *   color used to indicate editing
   * @param row the row index of the cell being drawn. When drawing the
   *   header, the value of <code>row</code> is -1
   * @param column the column index of the cell being drawn
   * @return Component
   */
  public Component getTableCellRendererComponent(JTable table, Object value,
                                                 boolean isSelected,
                                                 boolean hasFocus, int row,
                                                 int column) {
    if(value != null){
      label.setText(DATE_FORMAT.format( (Date) value));
    }
    else{
      label.setText("");
    }
    return label;
  }
}
