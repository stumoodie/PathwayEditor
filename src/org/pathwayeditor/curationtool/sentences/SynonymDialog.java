package org.pathwayeditor.curationtool.sentences;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;

import javax.swing.JDialog;
import javax.swing.JTextPane;

import org.pathwayeditor.notations.annotator.ndom.IEntityNode;

public class SynonymDialog extends JDialog {
	private static final long serialVersionUID = 1L;
	private final JTextPane synonymBox = new JTextPane();

	public SynonymDialog(Dialog synonymDialog) {
		super(synonymDialog);
		this.setLayout(new BorderLayout());
		this.synonymBox.setEditable(false);
		this.synonymBox.setPreferredSize(new Dimension(200, 500));
		this.add(synonymBox, BorderLayout.CENTER);
	}

	public void setSynonyms(IEntityNode focusNode) {
		final StringBuilder buf = new StringBuilder("<html>");
		for(String synonym : focusNode.getSynonyms()){
			buf.append(synonym);
			buf.append("<br>");
		}
		buf.append("</html>");
		synonymBox.setContentType("text/html");
		synonymBox.setText(buf.toString());
	}

}
