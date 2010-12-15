package org.pathwayeditor.visualeditor;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.SplashScreen;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import org.pathwayeditor.notations.annotator.services.AnnotatorNotationSubsystem;
import org.pathwayeditor.visualeditor.query.IPathwayQueryController;
import org.pathwayeditor.visualeditor.query.IQueryCompletedEvent;
import org.pathwayeditor.visualeditor.query.IQueryEventListener;
import org.pathwayeditor.visualeditor.query.IQueryVisualisationController;
import org.pathwayeditor.visualeditor.query.QueryVisualisationController;
import org.pathwayeditor.visualeditor.query.SearchDialog;

public class VisualEditor extends JFrame {
	private static final long serialVersionUID = 1L;

	private static final int WIDTH = 1200;
	private static final int HEIGHT = 800;
	private final JMenuBar menuBar;
	private final PathwayEditor insp;
	private final SearchDialog searchDialog;
	private final IPathwayQueryController pathwayController;
	
	public VisualEditor(String title, IPathwayQueryController pathwayController){
		super(title);
		this.pathwayController = pathwayController;
		doSplashScreen();
		this.setLayout(new BorderLayout());
		this.menuBar = new JMenuBar();
		initFileMenu();
		this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
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
		Dialog dialog = new Dialog(this, "Search Dialog");
		dialog.setAlwaysOnTop(true);
		searchDialog = new SearchDialog(dialog, this.pathwayController);
		this.insp = new PathwayEditor();
		this.insp.setPreferredSize(new Dimension(WIDTH, HEIGHT));
		this.add(this.insp, BorderLayout.CENTER);
		this.pack();
		final IQueryVisualisationController queryVisualisationController = new QueryVisualisationController(new AnnotatorNotationSubsystem(), insp);
		pathwayController.addQueryEventListener(new IQueryEventListener() {
			
			@Override
			public void queryCompleted(IQueryCompletedEvent e) {
				queryVisualisationController.setQueryResult(e.getQueryResult());
				queryVisualisationController.visualiseQueryResults();
			}
		});
		this.setLocationByPlatform(true);
		this.setVisible(true);
	}

	private void initFileMenu(){
		JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic(KeyEvent.VK_F);
		menuBar.add(fileMenu);
		//a group of JMenuItems
//		JMenuItem fileMenuOpenItem = new JMenuItem("Open", KeyEvent.VK_O);
//		fileMenuOpenItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.ALT_MASK));
//		fileMenuOpenItem.getAccessibleContext().setAccessibleDescription("This doesn't really do anything");
//		fileMenuOpenItem.addActionListener(new ActionListener() {
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				JFileChooser chooser = new JFileChooser();
////				chooser.setCurrentDirectory(new File("/Users/smoodie/Documents/workspace351_64/GraphicsEngine"));
//				chooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
//				chooser.setFileFilter(new FileFilter(){
//					
//					@Override
//					public boolean accept(File f) {
//						String fileName = f.getName();
//						return Pattern.matches(".*\\.pwe$", fileName);
//					}
//
//					@Override
//					public String getDescription() {
//						return "Pathway Editor files";
//					}
//					
//				});
//				int response = chooser.showOpenDialog(VisualEditor.this);
//				if(response == JFileChooser.APPROVE_OPTION){
//					File openFile = chooser.getSelectedFile();
//					openFile(openFile);
//				}
//			}
//		});
//		fileMenu.add(fileMenuOpenItem);
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
		JMenu searchMenu = new JMenu("Search");
		searchMenu.setMnemonic(KeyEvent.VK_S);
		menuBar.add(searchMenu);
		searchMenu.addMenuListener(new MenuListener(){

			@Override
			public void menuCanceled(MenuEvent arg0) {
			}

			@Override
			public void menuDeselected(MenuEvent arg0) {
			}

			@Override
			public void menuSelected(MenuEvent arg0) {
				initiateSearch();
			}
			
		});
//		addActionListener(new ActionListener(){
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				initiateSearch();
//			}
//		});
	}

	
	private void initiateSearch(){
		this.searchDialog.setVisible(true);
	}

//	public void openFile(File file){
//		try{
//			INotationSubsystemPool subsystemPool = new NotationSubsystemPool();
//			IXmlPersistenceManager canvasPersistenceManager = new FileXmlCanvasPersistenceManager(subsystemPool);
//			InputStream in = new FileInputStream(file);
//			canvasPersistenceManager.readCanvasFromStream(in);
//			in.close();
//			insp.loadCanvas(canvasPersistenceManager.getCurrentCanvas());
////			this.getRootPane().revalidate();
//			
//		}
//		catch(IOException ex){
//			System.err.println("Error opening file!");
//			System.err.println();
//		}
//	}

	private void doSplashScreen(){
		final SplashScreen splash = SplashScreen.getSplashScreen();
		if (splash == null) {
			System.out.println("SplashScreen.getSplashScreen() returned null");
			return;
		}
		Graphics2D g = splash.createGraphics();
		if (g == null) {
			System.out.println("g is null");
			return;
		}
		renderSplashFrame(g, 10);
		this.pathwayController.initialise();
		renderSplashFrame(g, 100);
		splash.close();
	}
	
    static void renderSplashFrame(Graphics2D g, int frame) {
        final String[] comps = {"foo", "bar", "baz"};
        g.setComposite(AlphaComposite.Clear);
        g.fillRect(120,140,200,40);
        g.setPaintMode();
        g.setColor(Color.BLACK);
        g.drawString("Loading "+comps[(frame/5)%3]+"...", 120, 150);
    }
}
