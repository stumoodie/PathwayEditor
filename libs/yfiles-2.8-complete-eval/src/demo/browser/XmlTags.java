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

/**
 * TODO: add documentation
 *
 */
public final class XmlTags
{
  public static final String DEFAULT_NS_URI = "http://www.w3.org/1999/xhtml";
  public static final String YWORKS_NS_URI = "http://www.yworks.com/demo";
  public static final String YWORKS_NS_PREFIX = "y";

  public static final String ATTRIBUTE_NAME = "javaname";
  public static final String ATTRIBUTE_SOURCE = "source";
  public static final String ATTRIBUTE_BROWSER = "browser";
  public static final String ATTRIBUTE_EXECUTABLE = "executable";
  public static final String ATTRIBUTE_DEFAULT_NS = "xmlns";
  public static final String ATTRIBUTE_YWORKS_NS = "xmlns:" + YWORKS_NS_PREFIX;

  public static final String ELEMENT_NAME = "displayname";
  public static final String ELEMENT_PACKAGE = "package";
  public static final String ELEMENT_DESCRIPTION = "description";
  public static final String ELEMENT_SUMMARY = "summary";
  public static final String ELEMENT_DEMO = "demo";
  public static final String ELEMENT_HTML = "html";
  public static final String ELEMENT_PRIORITY = "displaypriority";

  private XmlTags()
  {
  }
}
