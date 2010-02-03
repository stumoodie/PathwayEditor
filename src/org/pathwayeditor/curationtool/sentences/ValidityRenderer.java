package org.pathwayeditor.curationtool.sentences;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

public class ValidityRenderer extends JLabel implements TableCellRenderer {
	private static final long serialVersionUID = 1L;
	private static final char VALIDATED_CHAR = 'V';
	
	public ValidityRenderer() {
		this.setOpaque(true);
	}
	
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		String statusFlagText = (String)value;
        if (statusFlagText.charAt(0) == VALIDATED_CHAR) {
        	if(statusFlagText.contains("R")){
        		this.setBackground(Color.GREEN);
        		this.setForeground(Color.BLACK);
        	} else {
        		this.setBackground(Color.RED);
        		this.setForeground(Color.BLACK);
        	}
        }
        else{
    		this.setBackground(Color.WHITE);
    		this.setForeground(Color.BLACK);
        }
        this.setText(statusFlagText);
        return this;
	}

}
