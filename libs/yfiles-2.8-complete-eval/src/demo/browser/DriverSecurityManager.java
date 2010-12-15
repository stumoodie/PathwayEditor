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

import java.security.Permission;
import java.io.FileDescriptor;
import java.net.InetAddress;

/**
 * TODO: add documentation
 *
 */
public class DriverSecurityManager extends SecurityManager
{
  public static final String HANDLE_EXIT_VM = "com.yworks.demo.exitVM";

  private static final SecurityManager DEFAULT_SECURITY_MANAGER =
  new SecurityManager()
  {
    public void checkPermission( final Permission perm )
    {
    }
  };


  private final SecurityManager delegate;
  private final Exception stacktrace;

  public DriverSecurityManager( final SecurityManager delegate )
  {
    this.delegate = delegate != null ? delegate : DEFAULT_SECURITY_MANAGER;
    this.stacktrace = new Exception();
  }

  public SecurityManager getDelegate()
  {
    return delegate != DEFAULT_SECURITY_MANAGER ? delegate : null;
  }

  public void checkAwtEventQueueAccess()
  {
    delegate.checkAwtEventQueueAccess();
  }

  public void checkCreateClassLoader()
  {
    delegate.checkCreateClassLoader();
  }

  public void checkPrintJobAccess()
  {
    delegate.checkPrintJobAccess();
  }

  public void checkPropertiesAccess()
  {
    delegate.checkPropertiesAccess();
  }

  public void checkSetFactory()
  {
    delegate.checkSetFactory();
  }

  public void checkSystemClipboardAccess()
  {
    delegate.checkSystemClipboardAccess();
  }

  public boolean getInCheck()
  {
    return delegate.getInCheck();
  }

  public void checkExit( final int status )
  {
    if (!isJFrameSetDefaultCloseOperation()) {
      throw new SecurityException(HANDLE_EXIT_VM);
    }
  }

  public void checkListen( final int port )
  {
    delegate.checkListen(port);
  }

  public void checkRead( final FileDescriptor fd )
  {
    delegate.checkRead(fd);
  }

  public void checkWrite( final FileDescriptor fd )
  {
    delegate.checkWrite(fd);
  }

  public void checkMemberAccess( final Class clazz, final int which )
  {
    delegate.checkMemberAccess(clazz, which);
  }

  public Object getSecurityContext()
  {
    return delegate.getSecurityContext();
  }

  public boolean checkTopLevelWindow( final Object window )
  {
    return delegate.checkTopLevelWindow(window);
  }

  public void checkDelete( final String file )
  {
    delegate.checkDelete(file);
  }

  public void checkExec( final String cmd )
  {
    delegate.checkExec(cmd);
  }

  public void checkLink( final String lib )
  {
    delegate.checkLink(lib);
  }

  public void checkPackageAccess( final String pkg )
  {
    delegate.checkPackageAccess(pkg);
  }

  public void checkPackageDefinition( final String pkg )
  {
    delegate.checkPackageDefinition(pkg);
  }

  public void checkPropertyAccess( final String key )
  {
    delegate.checkPropertyAccess(key);
  }

  public void checkRead( final String file )
  {
    delegate.checkRead(file);
  }

  public void checkSecurityAccess( final String target )
  {
    delegate.checkSecurityAccess(target);
  }

  public void checkWrite( final String file )
  {
    delegate.checkWrite(file);
  }

  public void checkAccept( final String host, final int port )
  {
    delegate.checkAccept(host, port);
  }

  public void checkConnect( final String host, final int port )
  {
    delegate.checkConnect(host, port);
  }

  public void checkAccess( final Thread t )
  {
    delegate.checkAccess(t);
  }

  public ThreadGroup getThreadGroup()
  {
    return delegate.getThreadGroup();
  }

  public void checkAccess( final ThreadGroup g )
  {
    delegate.checkAccess(g);
  }

  public void checkMulticast( final InetAddress maddr )
  {
    delegate.checkMulticast(maddr);
  }

  public void checkMulticast( final InetAddress maddr, final byte ttl )
  {
    delegate.checkMulticast(maddr, ttl);
  }

  public void checkPermission( final Permission perm )
  {
    if ("exitVM".equals(perm.getName())) {
      if (!isJFrameSetDefaultCloseOperation()) {
        throw new SecurityException(HANDLE_EXIT_VM);
      }
    }
    delegate.checkPermission(perm);
  }

  public void checkConnect( final String host, final int port, final Object context )
  {
    delegate.checkConnect(host, port, context);
  }

  public void checkRead( final String file, final Object context )
  {
    delegate.checkRead(file, context);
  }

  public void checkPermission( final Permission perm, final Object context )
  {
    delegate.checkPermission(perm, context);
  }

  private boolean isJFrameSetDefaultCloseOperation()
  {
    stacktrace.fillInStackTrace();
    final StackTraceElement[] ste = stacktrace.getStackTrace();
    for (int i = 0; i < ste.length; ++i) {
      if ("javax.swing.JFrame".equals(ste[i].getClassName()) &&
          "setDefaultCloseOperation".equals(ste[i].getMethodName())) {
        return true;
      }
    }
    return false;
  }
}
