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
package demo.browser;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.io.PrintStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.awt.Frame;
import java.awt.Window;
import java.awt.EventQueue;
import java.util.Map;
import java.util.HashMap;
import javax.swing.JRootPane;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * TODO: add documentation
 *
 */
public class DriverFactory
{
  private static final Class[] EMPTY_ARG_TYPES =
          new Class[0];
  private static final Object[] EMPTY_ARGS =
          new Object[0];
  private static final Class[] START_ARG_TYPES =
          {JRootPane.class};
  private static final Class[] MAIN_ARG_TYPES =
          {String[].class};
  private static final Object[] EMPTY_MAIN_ARGS =
          {new String[0]};

  private static final Map drivers = new HashMap();


  private final Frame applicationFrame;
  private final JRootPane rootPane;
  private ExceptionHandler exceptionHandler;

  public DriverFactory( final Frame applicationFrame,
                        final JRootPane rootPane )
  {
    this.applicationFrame = applicationFrame;
    this.rootPane = rootPane;
    this.exceptionHandler = new ExceptionHandler()
    {
      public void handleException( final Exception ex )
      {
        ex.printStackTrace();
      }
    };
  }

  public ExceptionHandler getExceptionHandler()
  {
    return exceptionHandler;
  }

  public void setExceptionHandler( final ExceptionHandler exceptionHandler )
  {
    this.exceptionHandler = exceptionHandler;
  }

  public Driver createDriverForClass( final String className )
          throws DriverInstantiationException
  {
    Driver driver = (Driver)drivers.get(className);
    if (driver == null) {
      try {
        final Class demo = Class.forName(className);

        Method dispose = null;
        try {
          dispose = demo.getMethod("dispose", EMPTY_ARG_TYPES);
        } catch (NoSuchMethodException nsme) {
          dispose = null;
        }

        try {
          final Method start = demo.getMethod("addContentTo", START_ARG_TYPES);
          driver = new StartDriver(demo, start, dispose);
          drivers.put(className, driver);
        } catch (NoSuchMethodException noStart) {
          // just look for a main method
          try {
            final Method main = demo.getMethod("main", MAIN_ARG_TYPES);
            driver = new MainDriver(main);
            drivers.put(className, driver);
          } catch (NoSuchMethodException noMain) {
            throw new DriverInstantiationException(noMain);
          }
        }
      } catch (ClassNotFoundException cnfe) {
        throw new DriverInstantiationException(cnfe);
      }
    }
    return driver;
  }

  private abstract class AbstractDriver implements Driver
  {
    AbstractDriver()
    {
    }

    void restoreRootpane()
    {
      if (!EventQueue.isDispatchThread()) {
        EventQueue.invokeLater(new Runnable()
        {
          public void run()
          {
            _restoreRootpane();
          }
        });
      } else {
        _restoreRootpane();
      }
    }

    private void _restoreRootpane()
    {
      final JRootPane backup = new JRootPane();
      rootPane.setContentPane(backup.getContentPane());
      rootPane.setGlassPane(backup.getGlassPane());
      rootPane.setJMenuBar(backup.getJMenuBar());
      rootPane.setLayeredPane(backup.getLayeredPane());
      rootPane.validate();
      rootPane.repaint();
    }
  }

  private final class StartDriver extends AbstractDriver
  {
    private final Class demo;
    private final Method start;
    private final Method dispose;
    private Object demoInstance;

    StartDriver( final Class demo, final Method start, final Method dispose )
    {
      this.demo = demo;
      this.start = start;
      this.dispose = dispose;
    }

    public void start()
    {
      if (!EventQueue.isDispatchThread()) {
        EventQueue.invokeLater(new Runnable()
        {
          public void run()
          {
            _start();
          }
        });
      } else {
        _start();
      }
    }

    private void _start()
    {
      try {
        demoInstance = demo.newInstance();
        start.invoke(demoInstance, new Object[]{rootPane});
        rootPane.validate();
        rootPane.repaint();
      } catch (Exception ex) {
        exceptionHandler.handleException(ex);
      }
    }

    public void dispose()
    {
      try {
        if (demoInstance != null) {
          if (dispose != null) {
            try {
              dispose.invoke(demoInstance, EMPTY_ARGS);
            } catch (Exception ex) {
              exceptionHandler.handleException(ex);
            }
          }
          demoInstance = null;
        }
      } finally {
        restoreRootpane();
      }
    }
  }

  private final class MainDriver extends AbstractDriver
  {
    private final Method main;
    private Console console;
    private Thread mainThread;

    MainDriver( final Method main )
    {
      this.main = main;
    }

    public void start()
    {
      try {
        if (!EventQueue.isDispatchThread()) {
          EventQueue.invokeLater(new Runnable()
          {
            public void run()
            {
              prepare();
            }
          });
        } else {
          prepare();
        }
        mainThread = new Thread(new Runnable()
        {
          public void run()
          {
            try {
              main.invoke(null, EMPTY_MAIN_ARGS);
            } catch (InvocationTargetException ite) {
              if (ite.getCause() instanceof ThreadDeath) {
                // do nothing
              } else {
                exceptionHandler.handleException(ite);
              }
            } catch (Exception ex) {
              exceptionHandler.handleException(ex);
            }
          }
        });
        mainThread.start();
      } catch (Exception ex) {
        exceptionHandler.handleException(ex);
      }
    }

    private void prepare()
    {
      console = new Console();
      rootPane.setContentPane(console.getComponent());
      rootPane.validate();
      rootPane.repaint();
    }

    public void dispose()
    {
      try {
        try {
          try {
            if (!EventQueue.isDispatchThread()) {
              EventQueue.invokeLater(new Runnable()
              {
                public void run()
                {
                  closeWindows();
                }
              });
            } else {
              closeWindows();
            }
          } finally {
            if (console != null) {
              console.dispose();
            }
          }
        } finally {
          restoreRootpane();
        }
      } finally {
        if (mainThread != null) {
          mainThread.stop();
        }
      }
    }

    private void closeWindows()
    {
      final Frame[] frames = Frame.getFrames();
      for (int i = 0; i < frames.length; ++i) {
        if (applicationFrame == frames[i]) {
          continue;
        }
        final Window[] windows = frames[i].getOwnedWindows();
        for (int j = 0; j < windows.length; ++j) {
          windows[j].setVisible(false);
          windows[j].dispose();
        }
        frames[i].setVisible(false);
        frames[i].dispose();
      }
    }
  }

  private static final class Console
  {
    private final PrintStream oldErr;
    private final PrintStream oldOut;

    private final JTextArea textArea;

    Console()
    {
      this.oldErr = System.err;
      this.oldOut = System.out;
      this.textArea = new JTextArea();
      this.textArea.setEditable(false);
      System.setErr(new PrintStream(new CustomOutputStream("err: "), true));
      System.setOut(new PrintStream(new CustomOutputStream("out: "), true));
    }

    public JComponent getComponent()
    {
      return new JScrollPane(textArea);
    }

    public void dispose()
    {
      System.setErr(oldErr);
      System.setOut(oldOut);
    }

    private final class CustomOutputStream extends ByteArrayOutputStream
    {
      private final String prefix;

      CustomOutputStream( final String prefix )
      {
        this.prefix = prefix;
      }

      public void flush() throws IOException
      {
        super.flush();
        if (!EventQueue.isDispatchThread()) {
          EventQueue.invokeLater(new Runnable()
          {
            private final String text = CustomOutputStream.this.toString();

            public void run()
            {
              textArea.append(text);
            }
          });
        } else {
          textArea.append(this.toString());
        }
        reset();
      }
    }
  }
}