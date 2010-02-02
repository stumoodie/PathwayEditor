package org.pathwayeditor.curationtool;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Pattern;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileFilter;

import org.pathwayeditor.businessobjects.drawingprimitives.ICanvas;
import org.pathwayeditor.businessobjects.exchange.FileXmlCanvasPersistenceManager;
import org.pathwayeditor.businessobjects.exchange.IXmlPersistenceManager;
import org.pathwayeditor.businessobjects.management.INotationSubsystemPool;
import org.pathwayeditor.curationtool.sentences.ISentenceSelectionChangeEvent;
import org.pathwayeditor.curationtool.sentences.ISentenceSelectionChangeListener;
import org.pathwayeditor.curationtool.sentences.SentenceFromSInterationIterator;
import org.pathwayeditor.curationtool.sentences.SentencesPanel;
import org.pathwayeditor.notations.annotator.ndom.IMapDiagram;
import org.pathwayeditor.notations.annotator.ndom.ISentence;
import org.pathwayeditor.notations.annotator.ndom.parser.BoParser;
import org.pathwayeditor.notations.annotator.ndom.parser.BoTreeLexer;
import org.pathwayeditor.notations.annotator.ndom.parser.NdomBuilder;
import org.pathwayeditor.notations.annotator.ndom.parser.TreeParseException;
import org.pathwayeditor.visualeditor.NotationSubsystemPool;
import org.pathwayeditor.visualeditor.PathwayEditor;

public class CurationToolUI extends JFrame {
	private static final long serialVersionUID = 1L;

	private static final int WIDTH = 1200;
	private static final int HEIGHT = 800;
	private JMenuBar menuBar;
	private SentencesPanel sentencesPanel;

	private PathwayEditor insp;
	
	public CurationToolUI(String title){
		super(title);
		this.setLayout(new BorderLayout());
		this.menuBar = new JMenuBar();
		initFileMenu();
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		this.addWindowListener(new WindowListener(){

			@Override
			public void windowActivated(WindowEvent e) {
				
			}

			@Override
			public void windowClosed(WindowEvent e) {
			}

			@Override
			public void windowClosing(WindowEvent e) {
				int reply = JOptionPane.showConfirmDialog(CurationToolUI.this, "Are you sure you want to exit?", "Exit Confirmation", JOptionPane.YES_NO_OPTION);
				if(reply == JOptionPane.YES_OPTION){
					System.exit(0);
				}
			}

			@Override
			public void windowDeactivated(WindowEvent e) {
				
			}

			@Override
			public void windowDeiconified(WindowEvent e) {
				
			}

			@Override
			public void windowIconified(WindowEvent e) {
				
			}

			@Override
			public void windowOpened(WindowEvent e) {
				
			}
			
		});
		this.setJMenuBar(menuBar);
		this.sentencesPanel = new SentencesPanel();
		this.sentencesPanel.setPreferredSize(new Dimension(WIDTH, HEIGHT/3));
		this.insp = new PathwayEditor();
		this.insp.setPreferredSize(new Dimension(WIDTH, 2*HEIGHT/3));
		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, this.sentencesPanel, this.insp);
		splitPane.setDividerLocation(0.33);
		splitPane.setOneTouchExpandable(true);
		this.add(splitPane);
		this.pack();
		this.setVisible(true);
	}

	private void initComponentLinkage() {
		this.sentencesPanel.addSentenceSelectionChangeListener(new ISentenceSelectionChangeListener() {
			
			@Override
			public void sentenceSelectionChangeEvent(ISentenceSelectionChangeEvent e) {
				ISentence selectedSentence = e.getSelectedSentence();
				insp.selectAndFocusOnElement(selectedSentence.getArc().getLinkEdge());
			}
		});
	}

	private void initFileMenu(){
		JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic(KeyEvent.VK_F);
		fileMenu.getAccessibleContext().setAccessibleDescription(
		        "The only menu in this program that has menu items");
		menuBar.add(fileMenu);
		//a group of JMenuItems
		JMenuItem fileMenuOpenItem = new JMenuItem("Open", KeyEvent.VK_O);
		fileMenuOpenItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.ALT_MASK));
		fileMenuOpenItem.getAccessibleContext().setAccessibleDescription("This doesn't really do anything");
		fileMenuOpenItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
//				chooser.setCurrentDirectory(new File("/Users/smoodie/Documents/workspace351_64/GraphicsEngine/test/org/pathwayeditor/graphicsengine"));
				chooser.setCurrentDirectory(new File("/Users/smoodie/Documents/workspace351_64/GraphicsEngine"));
				chooser.setFileFilter(new FileFilter(){
					
					@Override
					public boolean accept(File f) {
						String fileName = f.getName();
						return Pattern.matches(".*\\.pwe$", fileName);
					}

					@Override
					public String getDescription() {
						return "Pathway Editor files";
					}
					
				});
				int response = chooser.showOpenDialog(CurationToolUI.this);
				if(response == JFileChooser.APPROVE_OPTION){
					File openFile = chooser.getSelectedFile();
					openFile(openFile);
				}
			}
		});
		fileMenu.add(fileMenuOpenItem);
		JMenuItem fileMenuExitItem = new JMenuItem("Exit", KeyEvent.VK_X);
		fileMenuExitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.ALT_MASK));
		fileMenuExitItem.getAccessibleContext().setAccessibleDescription("This doesn't really do anything");
		fileMenuExitItem.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				processEvent(new WindowEvent(CurationToolUI.this, WindowEvent.WINDOW_CLOSING));
			}
		});
		fileMenu.add(fileMenuExitItem);
	}
	

	public void openFile(File file){
		try{
			INotationSubsystemPool subsystemPool = new NotationSubsystemPool();
			IXmlPersistenceManager canvasPersistenceManager = new FileXmlCanvasPersistenceManager(subsystemPool);
			InputStream in = new FileInputStream(file);
			canvasPersistenceManager.readCanvasFromStream(in);
			in.close();
			ICanvas canvas = canvasPersistenceManager.getCurrentCanvas();
			NdomBuilder builder = new NdomBuilder();
			BoParser parser = new BoParser(builder);
			BoTreeLexer lexer = new BoTreeLexer(canvas);
			parser.parse(lexer);
			IMapDiagram ndom = builder.getNdom();
			sentencesPanel.loadData(new SentenceFromSInterationIterator(ndom.sInteractionIterator()));
			insp.loadCanvas(canvas);
			this.validate();
			initComponentLinkage();
			insp.selectAndFocusOnElement(sentencesPanel.getSelectedSentence().getArc().getLinkEdge());
		}
		catch(IOException ex){
			JOptionPane.showMessageDialog(this, "Error message: " + ex.getLocalizedMessage(), "Error opening file", JOptionPane.ERROR_MESSAGE);
		} catch (TreeParseException e) {
			JOptionPane.showMessageDialog(this, "Error message: " + e.getLocalizedMessage(), "Bug detected loading file", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	
	public void startup(){
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
//				sentencesPanel.loadData();
//				openFile(new File(TEST_FILE));
			}
		});
	}
	
	public static final void main(String argv[]){
		CurationToolUI visualEditor = new CurationToolUI("Pathway Editor");
//		visualEditor.openFile(new File(TEST_FILE));
		visualEditor.startup();
	}
}
