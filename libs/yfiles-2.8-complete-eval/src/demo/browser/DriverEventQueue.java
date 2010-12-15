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

import java.awt.AWTEvent;
import java.awt.EventQueue;

/**
 * TODO: add documentation
 *
 */
public class DriverEventQueue extends EventQueue
{
  private Driver driver;
  private DriverSecurityManager securityManager;

  public Driver getDriver()
  {
    return driver;
  }

  public void setDriver( final Driver driver )
  {
    this.driver = driver;
    if (driver != null) {
      if (securityManager != null) {
        securityManager = new DriverSecurityManager(securityManager.getDelegate());
      } else {
        securityManager = new DriverSecurityManager(System.getSecurityManager());
      }
      System.setSecurityManager(securityManager);
    } else {
      if (securityManager != null) {
        System.setSecurityManager(securityManager.getDelegate());
        securityManager = null;
      }
    }
  }

  protected void dispatchEvent( final AWTEvent event )
  {
    try {
      super.dispatchEvent(event);
    } catch (SecurityException se) {
      if (DriverSecurityManager.HANDLE_EXIT_VM.equals(se.getMessage())) {
        if (driver != null) {
          driver.dispose();
        }
        if (securityManager != null) {
          System.setSecurityManager(securityManager.getDelegate());
          securityManager = null;
        }
      } else {
        throw se;
      }
    }
  }
}
