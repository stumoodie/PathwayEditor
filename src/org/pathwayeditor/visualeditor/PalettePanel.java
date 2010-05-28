package org.pathwayeditor.visualeditor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.Iterator;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import org.pathwayeditor.businessobjects.notationsubsystem.INotationSubsystem;
import org.pathwayeditor.businessobjects.typedefn.ILinkObjectType;
import org.pathwayeditor.businessobjects.typedefn.IShapeObjectType;
import org.pathwayeditor.figure.geometry.Envelope;
import org.pathwayeditor.visualeditor.behaviour.IMouseBehaviourController;

public class PalettePanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private JScrollPane palettePane;
	private JScrollPane linkScrollPanel;
	private JSplitPane splitPane;

	public PalettePanel(INotationSubsystem notationSubsystem, final IMouseBehaviourController editBehaviourController) {
		final ButtonGroup paletteGroup = new ButtonGroup();
		JPanel selectionButtonPanel = new JPanel();
		selectionButtonPanel.setLayout(new BoxLayout(selectionButtonPanel, BoxLayout.PAGE_AXIS));
		final JButton selectionButton = new JButton("Selection");
		ImageIcon selectionIcon = createImageIcon("images/cursor_arrow16.png");
		selectionButton.setIcon(selectionIcon);
		paletteGroup.add(selectionButton);
		selectionButton.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				editBehaviourController.setSelectionMode();
				paletteGroup.setSelected(selectionButton.getModel(), true);
			}
			
		});
//		selectionButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		selectionButtonPanel.add(selectionButton);
		this.add(selectionButtonPanel);
		
		JPanel shapeButtonPanel = new JPanel();
		this.palettePane = new JScrollPane(shapeButtonPanel);
		this.palettePane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		shapeButtonPanel.setLayout(new BoxLayout(shapeButtonPanel, BoxLayout.PAGE_AXIS));
		ShapeIconGenerator iconGenerator = new ShapeIconGenerator();
		iconGenerator.setBounds(new Envelope(0, 0, 16, 16));
		Iterator<IShapeObjectType> shapeTypeIterator = notationSubsystem.getSyntaxService().shapeTypeIterator();
		while(shapeTypeIterator.hasNext()){
			final IShapeObjectType shapeType = shapeTypeIterator.next();
			iconGenerator.setObjectType(shapeType);
			iconGenerator.generateImage();
			iconGenerator.generateIcon();
			final JButton shapeButton = new JButton(iconGenerator.getIcon());
			shapeButton.setText(shapeType.getName());
			shapeButton.setToolTipText(shapeType.getDescription());
//			shapeButton.setAlignmentX(Component.CENTER_ALIGNMENT);
			shapeButtonPanel.add(shapeButton);
			paletteGroup.add(shapeButton);
			shapeButton.addActionListener(new ActionListener(){

				@Override
				public void actionPerformed(ActionEvent e) {
					editBehaviourController.setShapeCreationMode(shapeType);
					paletteGroup.setSelected(shapeButton.getModel(), true);
				}
				
			});
		}
		
		JPanel linkButtonPanel = new JPanel();
		linkButtonPanel.setLayout(new BoxLayout(linkButtonPanel, BoxLayout.PAGE_AXIS));
		
		this.linkScrollPanel = new JScrollPane(linkButtonPanel);
		this.linkScrollPanel.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		LinkIconGenerator linkIconGenerator = new LinkIconGenerator();
		Iterator<ILinkObjectType> linkTypeIterator = notationSubsystem.getSyntaxService().linkTypeIterator();
		linkIconGenerator.setBounds(new Envelope(0.0, 0.0, 64.0, 16.0));
		while(linkTypeIterator.hasNext()){
			final ILinkObjectType linkType = linkTypeIterator.next();
			linkIconGenerator.setObjectType(linkType);
			linkIconGenerator.generateImage();
			linkIconGenerator.generateIcon();
			final JButton linkButton = new JButton(linkIconGenerator.getIcon());
			linkButton.setText(linkType.getName());
			linkButton.setToolTipText(linkType.getDescription());
//			linkButton.setAlignmentX(Component.CENTER_ALIGNMENT);
			linkButtonPanel.add(linkButton);
			linkButton.addActionListener(new ActionListener(){

				@Override
				public void actionPerformed(ActionEvent e) {
					editBehaviourController.setLinkCreationMode(linkType);
					paletteGroup.setSelected(linkButton.getModel(), true);
				}
				
			});
		}
		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		this.splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, this.palettePane, this.linkScrollPanel);
		this.splitPane.setOneTouchExpandable(true);
		this.splitPane.setDividerLocation(250);
		this.add(this.splitPane);
		paletteGroup.setSelected(selectionButton.getModel(), true);
//		this.add(this.palettePane);
//		this.add(this.linkScrollPanel);
//		this.add(this.palettePane, BorderLayout.LINE_START);
//		this.add(this.linkPalette);
	}

    private ImageIcon createImageIcon(String path) {
        URL imgURL = this.getClass().getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            throw new IllegalArgumentException("Couldn't find file: " + path);
        }
    }
}
