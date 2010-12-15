/****************************************************************************
 **
 ** This file is part of yFiles-2.8. 
 ** 
 ** yWorks proprietary/confidential. Use is subject to license terms.
 **
 ** Redistribution of this file or of an unauthorized byte-code version
 ** of this file is strictly forbidden.
 **
 ** Copyright (c) 2000-2010 by yWorks GmbH, Vor dem Kreuzberg 28, 
 ** 72070 Tuebingen, Germany. All rights reserved.
 **
 ***************************************************************************/

package demo.layout.module;

import demo.layout.withoutview.DiagonalLayouter;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Properties;
import y.module.LayoutModule;
import y.module.YModule;
import y.option.OptionHandler;
import y.option.PropertiesIOHandler;
import y.option.ResourceBundleGuiFactory;

/**
 * This module represents an interactive configurator and launcher for the demo
 * Layouter {@link demo.layout.withoutview.DiagonalLayouter}.
 * <br>
 * Additionally, this class can be executed separately. In this case it shows off
 * the internationalization and serialization features of the
 * {@link y.option.OptionHandler} class.
 * By launching the module class using a two letter language constant as an
 * argument, the dialog will be internationalized in that language if the
 * corresponding localized properties file is available. Try either 'en' for
 * English or 'de' for German.
 *
 */
public class DiagonalLayoutModule extends LayoutModule
{
  public DiagonalLayoutModule()
  {
    super("DIAGONAL", "yWorks Layout Team", "Wrapper for DiagonalLayouter");
  }
  
  protected OptionHandler createOptionHandler()
  {
    DiagonalLayouter layouter = new DiagonalLayouter();
    OptionHandler op = new OptionHandler(getModuleName());
    op.addDouble("MINIMAL_NODE_DISTANCE", layouter.getMinimalNodeDistance());
    return op;
  }
  
  protected void mainrun()
  {
    DiagonalLayouter layouter = new DiagonalLayouter();
    OptionHandler op = getOptionHandler();
    layouter.setMinimalNodeDistance(op.getDouble("MINIMAL_NODE_DISTANCE"));
    launchLayouter(layouter);
  }
  
  
  /**
   * Display the option handler
   */
  public static void main(String[] args)
  {
    // set the locale as given from the arguments
    if (args.length > 1){
      Locale.setDefault(new Locale(args[0],args[1]));
    } else if (args.length > 0){
      Locale.setDefault(new Locale(args[0],""));
    }
    
    System.out.println("Executing using Locale "+Locale.getDefault().getDisplayName());
    
    // initialize the module
    YModule module = new DiagonalLayoutModule();
    OptionHandler oh = module.getOptionHandler();

    // setup a guifactory
    try{
      ResourceBundleGuiFactory gf = new ResourceBundleGuiFactory();
      
      //this is the globally used information (for the buttons etc.)
      gf.addBundle("demo.layout.module.OptionHandler");
      
      //this is the bundle specific information
      gf.addBundle(module.getClass().getName());
      oh.setGuiFactory(gf);
    } catch (MissingResourceException mre){
      System.err.println("Could not find resources! "+mre);
    }
    
    // try to read in last session
    
    //create properties store
    Properties p = new Properties();
    try{
      // initialize it from file
      FileInputStream fis = new FileInputStream(module.getClass().getName()+"Settings.properties");
      p.load(fis);
      fis.close();
    } catch (IOException ioe){
      System.err.println(ioe);
    }
    // install an IOHandler
    oh.setOptionsIOHandler(new PropertiesIOHandler(p));
    
    // read the values in
    oh.read();
    
    // display the editor
    oh.showEditor();

    // store the properties
    try{
      FileOutputStream fos = new FileOutputStream(module.getClass().getName()+"Settings.properties");
      p.store(fos, "Saved Settings for "+module.getClass().getName());
      fos.flush();
      fos.close();
    } catch (IOException ioe){
      System.err.println(ioe);
    }
    
    //goodbye
    System.exit(0);
  }

}

