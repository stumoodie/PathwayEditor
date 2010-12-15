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

import java.util.Map;
import java.util.HashMap;
import java.net.URL;
import java.io.IOException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

/**
 * TODO: add documentation
 *
 */
class XmlTransformerFactory
{
  private static final String CODE_2_HTML = "resource/code2html.xsl";
  private static final String DEMO_2_HTML = "resource/demo2html.xsl";
  private static final String DESC_2_TOOLTIP = "resource/desc2tooltip.xsl";
  private static final String PACKAGE_2_HTML = "resource/package2html.xsl";
  private static final String PLAIN = "demo.browser.XmlTransformerFactory.PLAIN";
  private static final Map transformers = new HashMap();

  static synchronized Transformer tooltip() throws TransformerConfigurationException
  {
    Transformer t = (Transformer)transformers.get(DESC_2_TOOLTIP);
    if (t == null) {
      final TransformerFactory tf = TransformerFactory.newInstance();
      t = tf.newTransformer(source(DESC_2_TOOLTIP));
      transformers.put(DESC_2_TOOLTIP, t);
    }
    return t;
  }

  static synchronized Transformer code() throws TransformerConfigurationException
  {
    Transformer t = (Transformer)transformers.get(CODE_2_HTML);
    if (t == null) {
      final TransformerFactory tf = TransformerFactory.newInstance();
      t = tf.newTransformer(source(CODE_2_HTML));
      transformers.put(CODE_2_HTML, t);
    }
    return t;
  }

  static synchronized Transformer demo() throws TransformerConfigurationException
  {
    Transformer t = (Transformer)transformers.get(DEMO_2_HTML);
    if (t == null) {
      final TransformerFactory tf = TransformerFactory.newInstance();
      t = tf.newTransformer(source(DEMO_2_HTML));
      transformers.put(DEMO_2_HTML, t);
    }
    return t;
  }

  static synchronized Transformer pkg() throws TransformerConfigurationException
  {
    Transformer t = (Transformer)transformers.get(PACKAGE_2_HTML);
    if (t == null) {
      final TransformerFactory tf = TransformerFactory.newInstance();
      t = tf.newTransformer(source(PACKAGE_2_HTML));
      transformers.put(PACKAGE_2_HTML, t);
    }
    return t;
  }

  static synchronized Transformer plain() throws TransformerConfigurationException
  {
    Transformer t = (Transformer)transformers.get(PLAIN);
    if (t == null) {
      final TransformerFactory tf = TransformerFactory.newInstance();
      t = tf.newTransformer();
      transformers.put(PLAIN, t);
    }
    return t;
  }

  private static Source source( final String key ) throws TransformerConfigurationException
  {
    URL resource = XmlTransformerFactory.class.getResource(key);
    if (resource == null) {
      String message = "Cannot locate resource in classpath: " + XmlTransformerFactory.class.getPackage().getName().replace( '.', '/' ) + '/' + key;
      throw new TransformerConfigurationException(message );
    }
    try {
      return new StreamSource( resource.openStream() );
    } catch (IOException ioe) {
      throw new TransformerConfigurationException(ioe);
    }
  }

  private XmlTransformerFactory()
  {
  }
}