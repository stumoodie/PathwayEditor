package org.pathwayeditor.visualeditor.query;

import java.awt.Dialog;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.log4j.Logger;

public class SearchDialog extends JDialog implements ActionListener {
	private final Logger logger = Logger.getLogger(this.getClass());
	private static final long serialVersionUID = 1L;
	private static final String QUERY_CMD = "query";
	private static final String CLOSE_CMD = "close";
	private static final String TERM1_CMD = "term1";
	private static final String TERM2_CMD = "term2";
	private static final String CONF_SCORE_CMD = "confScore";
	private final JTextField queryValue = new JTextField();
	private final JPanel queryPanel = new JPanel();
	private final JTextField nodeSearchTerm = new JTextField();
	private final JButton queryButton = new JButton("Search");
	private final JButton dismissButton = new JButton("Dismiss");
	private final IPathwayQueryController queryController;
	private QueryData queryData;

	public SearchDialog(Dialog dialog, IPathwayQueryController pathwayQuery){
		super(dialog);
		this.queryController = pathwayQuery;
		layoutQueryPanel();
		this.queryButton.addActionListener(this);
		this.queryButton.setActionCommand(QUERY_CMD);
		this.dismissButton.addActionListener(this);
		this.dismissButton.setActionCommand(CLOSE_CMD);
		this.nodeSearchTerm.addActionListener(this);
		this.nodeSearchTerm.setActionCommand(TERM1_CMD);
		this.add(queryPanel);
		this.pack();
	}

	private void layoutQueryPanel(){
		queryValue.setColumns(15);
		GridBagLayout gbl_queryPanel = new GridBagLayout();
		gbl_queryPanel.rowHeights = new int[]{0, 0, 0};
		this.queryPanel.setLayout(gbl_queryPanel);
		JLabel nodeSearchLabel = new JLabel("Node Search Term");
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.EAST;
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		this.queryPanel.add(nodeSearchLabel, c);
		GridBagConstraints c_1 = new GridBagConstraints();
		c_1.fill = GridBagConstraints.HORIZONTAL;
		c_1.gridx = 1;
		c_1.gridy = 0;
		c_1.gridwidth = 2;
		this.queryPanel.add(this.nodeSearchTerm, c_1);
		JLabel cutoffLabel = new JLabel("Score Threshold");
		GridBagConstraints c_3 = new GridBagConstraints();
		c_3.anchor = GridBagConstraints.EAST;
		c_3.gridx = 0;
		c_3.gridy = 1;
		c_3.gridwidth = 1;
		this.queryPanel.add(cutoffLabel, c_3);
		GridBagConstraints c_2 = new GridBagConstraints();
		c_2.fill = GridBagConstraints.HORIZONTAL;
		c_2.gridx = 1;
		c_2.gridy = 1;
		c_2.gridwidth = 2;
		this.queryPanel.add(queryValue, c_2);
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 2;
		c.gridwidth = 1;
		this.queryPanel.add(queryButton, c);
		c = new GridBagConstraints();
		c.gridx = 2;
		c.gridy = 2;
		c.gridwidth = 1;
		this.queryPanel.add(dismissButton, c);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getActionCommand().equals(QUERY_CMD)){
			logger.debug("Running Query");
			queryController.setQueryData(queryData);
			queryController.runQuery();
			logger.debug("Query finished");
		}
		else if(e.getActionCommand().equals(CLOSE_CMD)){
			setVisible(false);
		}
		else if(e.getActionCommand().equals(TERM1_CMD) || e.getActionCommand().equals(TERM2_CMD)
				|| e.getActionCommand().endsWith(CONF_SCORE_CMD)){
			updateData();
		}
	}

	public QueryData getQueryData() {
		return queryData;
	}

	public void setQueryData(QueryData queryData) {
		this.queryData = queryData;
		updateData();
	}
	
	private void updateData(){
		updateField(this.nodeSearchTerm, this.queryData.getTerm1());
		String confScoreTxt = "";
		if(this.queryData.getConfScoreCutoff() != null){
			confScoreTxt = this.queryData.getConfScoreCutoff().toString();
		}
		if(!this.queryValue.getText().equals(confScoreTxt)){
			this.queryValue.setText(confScoreTxt);
		}
	}

	private void updateField(JTextField termField, String term) {
		String fieldValue = "";
		if(term != null){
			fieldValue = term;
		}
		if(!termField.getText().equals(fieldValue)){
			termField.setText(fieldValue);
		}
	}

}
