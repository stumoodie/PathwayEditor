package org.pathwayeditor.visualeditor;

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
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileFilter;

import org.pathwayeditor.businessobjects.exchange.FileXmlCanvasPersistenceManager;
import org.pathwayeditor.businessobjects.exchange.IXmlPersistenceManager;
import org.pathwayeditor.businessobjects.management.INotationSubsystemPool;

public class VisualEditor extends JFrame {
	private static final long serialVersionUID = 1L;

	private static final int WIDTH = 1200;
	private static final int HEIGHT = 800;
	private JMenuBar menuBar;
	private PathwayEditor insp;
	
	public VisualEditor(String title){
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
				int reply = JOptionPane.showConfirmDialog(VisualEditor.this, "Are you sure you want to exit?", "Exit Confirmation", JOptionPane.YES_NO_OPTION);
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
		this.insp = new PathwayEditor();
		this.insp.setPreferredSize(new Dimension(WIDTH, HEIGHT));
		this.add(this.insp, BorderLayout.CENTER);
		this.pack();
		this.setLocationByPlatform(true);
		this.setVisible(true);
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
				int response = chooser.showOpenDialog(VisualEditor.this);
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
				processEvent(new WindowEvent(VisualEditor.this, WindowEvent.WINDOW_CLOSING));
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
			insp.loadCanvas(canvasPersistenceManager.getCurrentCanvas());
//			this.getRootPane().revalidate();
			
		}
		catch(IOException ex){
			System.err.println("Error opening file!");
			System.err.println();
		}
	}
}
