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
package demo.layout.genealogy.iohandler;

import org.w3c.dom.Document;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLFilter;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.XMLReaderFactory;
import y.base.DataProvider;
import y.base.Node;
import y.base.NodeCursor;
import y.io.GraphMLIOHandler;
import y.io.IOHandler;
import y.layout.genealogy.FamilyTreeLayouter;
import y.view.Graph2D;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * IOHandler that is able to read arbitrary GEDCOM files.
 * (GEDCOM is a widely used format to store family trees, see http://www.phpgedview.net/ged551-5.pdf
 * for the most recent specifications).
 * <p/>
 * The reader works the following way:
 * <ul>
 * <li>The gedcom format is transformed into XML</li>
 * <li>The generated XML is transformed by an XSL stylesheet (resources/gedml2graphml.xsl) into GraphML</li>
 * <li>The generated GraphML is read by a GraphMLIOHandler which has to be specified
 * in the constructor or set by setReaderDelegate</li>
 * </ul>
 */
public class GedcomHandler extends IOHandler {
  private GraphMLIOHandler readerDelegate;

  /** Default for GedCOM is ANSEL encoding, which does not exist in Java */
  private String encoding = "ANSEL";


  /**
   * Gets the encoding String. Default for GedCOM is ANSEL encoding, which does not exist in Java.
   * @return The encoding string
   */
  public String getEncoding() {
    return encoding;
  }

  /**
   * Sets the encoding String. Default for GedCOM is ANSEL encoding, which does not exist in Java.
   * @param encoding The encoding String
   */
  public void setEncoding(String encoding) {
    this.encoding = encoding;
  }

  /**
   * Creates a new instance of the IOHandler.
   */
  public GedcomHandler() {
  }

  /**
   * Creates a new instance of the IOHandler with the given
   * reader delegate.
   */
  public GedcomHandler(GraphMLIOHandler readerDelegate) {
    setReaderDelegate(readerDelegate);
  }

  /////////////////////////////////////////////////////////////////////////////
  ///////////////////////////   IOHandler Interface  //////////////////////////
  /////////////////////////////////////////////////////////////////////////////

  /**
   * Returns a textual description of the IOHandler.
   */
  public String getFileFormatString() {
    return "GedCOM files";
  }

  /**
   * Returns the file name extension for files this handler can handle.
   */
  public String getFileNameExtension() {
    return "ged";
  }

  /**
   * Sets the IOHandler that will further process the XSL transformed
   * XML input.
   */
  public void setReaderDelegate(GraphMLIOHandler reader) {
    readerDelegate = reader;
  }

  /**
   * Returns the IOHandler that will further process the XSL transformed
   * XML input.
   */
  public IOHandler getReaderDelegate() {
    return readerDelegate;
  }


  /**
   * Writes a graph to a Gedcom file. Uses the Michael H. Kay's GedcomOutputter as ContentHandler:
   * http://homepage.ntlworld.com/michael.h.kay/gedml/index.html
   * <p/>
   * The data provider {@link y.layout.genealogy.FamilyTreeLayouter#DP_KEY_FAMILY_TYPE}
   * has to be registered to the graph.
   * <p/>
   * For INDI entries, only the subentries SEX, NAME, FAMS and FAMC are supported<br/>
   * For FAM entries, only the subentries WIFE, HUSB and CHIL are supported.<br/>
   * @param graph The Graph to write.
   * @param out The OutputStream to write to.
   * @throws java.io.IOException
   * @throws IllegalStateException if the data provider
   * {@link y.layout.genealogy.FamilyTreeLayouter#DP_KEY_FAMILY_TYPE} is not registered.
   */
  public void write(Graph2D graph, OutputStream out) throws IOException {

    DataProvider dpType = graph.getDataProvider(FamilyTreeLayouter.DP_KEY_FAMILY_TYPE);
    if (dpType == null) {
      throw new IllegalStateException("Data Provider " + FamilyTreeLayouter.DP_KEY_FAMILY_TYPE + "not found.");
    }
    ContentHandler content = new GedcomOutputter(out);
    AttributesImpl emptyAttList = new AttributesImpl();
    AttributesImpl attList = new AttributesImpl();
    try {
      /* Creates the Document and the header*/
      content.startDocument();
      content.startElement("", "GED", "GED", emptyAttList);
      content.startElement("", "HEAD", "HEAD", emptyAttList);
      content.startElement("", "SOUR", "SOUR", emptyAttList);
      content.characters("yFiles FamilyTreeDemo".toCharArray(), 0, "yFiles FamilyTreeDemo".length());
      content.endElement("", "SOUR", "SOUR");
      content.endElement("", "HEAD", "HEAD");

      /* Iterates through all nodes of the graph */
      for (NodeCursor nc = graph.nodes(); nc.ok(); nc.next()) {
        Node n = nc.node();
        if (!FamilyTreeLayouter.TYPE_FAMILY.equals(dpType.get(n))) {
          /* Individual: write id, sex and name */
          attList.clear();
          attList.addAttribute("", "ID", "ID", "ID", "I" + n.index());
          content.startElement("", "INDI", "INDI", attList);
          content.startElement("", "SEX", "SEX", emptyAttList);
          if (FamilyTreeLayouter.TYPE_MALE.equals(dpType.get(n))) {
            content.characters("M".toCharArray(), 0, 1);
          } else {
            content.characters("F".toCharArray(), 0, 1);
          }
          content.endElement("", "SEX", "SEX");
          content.startElement("", "NAME", "NAME", emptyAttList);
          String name = graph.getLabelText(n);
          content.characters(name.toCharArray(), 0, name.length());
          content.endElement("", "NAME", "NAME");
          /* For outgoing edges: write the references to the families*/
          for (NodeCursor p = n.predecessors(); p.ok(); p.next()) {
            attList.clear();
            attList.addAttribute("", "REF", "REF", "REF", "F" + p.node().index());
            content.startElement("", "FAMC", "FAMC", attList);
            content.endElement("", "FAMC", "FAMC");
          }
          /* For ingoing edges: write the references to the families */
          for (NodeCursor p = n.successors(); p.ok(); p.next()) {
            attList.clear();
            attList.addAttribute("", "REF", "REF", "REF", "F" + p.node().index());
            content.startElement("", "FAMS", "FAMS", attList);
            content.endElement("", "FAMS", "FAMS");
          }
          content.endElement("", "INDI", "INDI");
        } else {
          /* Family: id */
          attList.clear();
          attList.addAttribute("", "ID", "ID", "ID", "F" + n.index());
          content.startElement("", "FAM", "FAM", attList);
          /* Ingoing edges: write the references to wife and husband */
          for (NodeCursor p = n.predecessors(); p.ok(); p.next()) {
            attList.clear();
            attList.addAttribute("", "REF", "REF", "REF", "I" + p.node().index());
            String tag = "WIFE";
            if (FamilyTreeLayouter.TYPE_MALE.equals(dpType.get(p.node()))) {
              tag = "HUSB";
            }
            content.startElement("", tag, tag, attList);
            content.endElement("", tag, tag);
          }
          /* Outgoing edges: write the references to the children */
          for (NodeCursor p = n.successors(); p.ok(); p.next()) {
            attList.clear();
            attList.addAttribute("", "REF", "REF", "REF", "I" + p.node().index());
            content.startElement("", "CHIL", "CHIL", attList);
            content.endElement("", "CHIL", "CHIL");
          }
          content.endElement("", "FAM", "FAM");
        }
      }
      content.endElement("", "GED", "GED");
      content.endDocument();
    } catch (SAXException e) {
      e.printStackTrace(); //TODO handle
    }
  }

//  /**
//   * Reads a Gedcom file into the given graph.
//   * @param graph The graph to write to.
//   * @param in The InputStream to read from.
//   * @throws java.io.IOException
//   * @throws IllegalStateException if no reader delegate is set.
//   */
//  public void read(Graph2D graph, InputStream in) throws IOException {
//    if (readerDelegate == null) {
//      throw new IllegalStateException("No reader delegate set.");
//    }
//
//    ////// Transform the Gedcom file into XML format //////
//    XMLReader reader = null;
//    Transformer transformer = null;
//    try {
//      reader = XMLReaderFactory.createXMLReader("demo.layout.genealogy.iohandler.GedcomParser");
//      SAXTransformerFactory factory = (SAXTransformerFactory) TransformerFactory.newInstance();
//      transformer = factory.newTransformer();
//
//      InputSource inputSource;
//      if ("ANSEL".equals(getEncoding())) {
//        //hack until Charset is finished
//        inputSource = new InputSource(new AnselInputStreamReader(in));
//      } else {
//        inputSource = new InputSource(in);
//      }
//
//      ///// Transform the generated XML into GML /////
//      String path = "resources/gedml2gml.xsl";
//      InputStream stream = this.getClass().getResourceAsStream(path);
//      if (stream == null) {
//        throw new IOException("Could not find style sheet \"" + path + "\".");
//      }
//      StreamSource xslStream = new StreamSource(stream);
//
//      XMLFilter convert2graphmlFilter = factory.newXMLFilter(xslStream);
//      convert2graphmlFilter.setParent(reader);
//
//      Source saxSource = new SAXSource(convert2graphmlFilter, inputSource);
//      ByteArrayOutputStream baos = new ByteArrayOutputStream();
//      OutputStreamWriter osw = new OutputStreamWriter(baos);
//      Result result = new StreamResult(osw);
//      transformer.transform(saxSource, result);
//
//      ///// Read the generated GML into the given graph /////
//      ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
//      byte[] b = new byte[1];
//      do {
//        bais.read(b);
//      } while (b[0] != ">".getBytes()[0]);
//      readerDelegate.read(graph, bais);
//    }
//    catch (SAXException e) {
//      throw new IOException(e.getMessage());
//    }
//    catch (TransformerConfigurationException e) {
//      throw new IOException(e.getMessage());
//    } catch (TransformerException e) {
//      throw new IOException(e.getMessage());
//    }
//  }

// GraphML users can use the following read method rather than the above
// GraphML offers the possibility to map additional attributes to the graph
// Using the provided XSL sheet, the boolean attributes NodeTypeIndividual and NodeIsMale
// can be used as data providers which return true if the node is an individual and male, respectively.
// Also, the original entry is preserved (as XML) in the GedcomData attribute.

  /**
   * Reads a Gedcom file into the given graph
   * @param graph The graph to write to.
   * @param in The InputStream to read from.
   * @throws IOException
   * @throws IllegalStateException if no reader delegate is set.
   */
  public void read(Graph2D graph, InputStream in) throws IOException {
    if (readerDelegate == null) {
      throw new IllegalStateException("No reader delegate set.");
    }

    ////// Transform the Gedcom file into XML format //////
    XMLReader reader = null;
    Transformer transformer = null;
    try {
      reader = XMLReaderFactory.createXMLReader("demo.layout.genealogy.iohandler.GedcomParser");
      SAXTransformerFactory factory = (SAXTransformerFactory) TransformerFactory.newInstance();
      transformer = factory.newTransformer();

      InputSource inputSource;
      if ("ANSEL".equals(getEncoding())) {
        //hack until Charset is finished
        inputSource = new InputSource(new AnselInputStreamReader(in));
      } else {
        inputSource = new InputSource(in);
      }

      ///// Transform the generated XML into GraphML /////
      String path = "resources/gedml2graphml.xsl";
      InputStream stream = this.getClass().getResourceAsStream(path);
      if (stream == null) {
        throw new IOException("Could not find style sheet \"" + path + "\".");
      }
      StreamSource xslStream = new StreamSource(stream);

      XMLFilter convert2graphmlFilter = factory.newXMLFilter(xslStream);
      convert2graphmlFilter.setParent(reader);

      Source saxSource = new SAXSource(convert2graphmlFilter, inputSource);
      DOMResult xmlResult = new DOMResult();
      transformer.transform(saxSource, xmlResult);

//      StreamResult streamResult = new StreamResult(System.out);
//      DOMSource domSource = new DOMSource((Document) xmlResult.getNode());
//      transformer.setOutputProperty(OutputKeys.METHOD, "xml");
//      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
//      transformer.setOutputProperty(OutputKeys.ENCODING, encoding);
//      try {
//        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
//      }
//      catch (IllegalArgumentException e) {
//        System.out.println("");
//      }
//      transformer.transform(domSource, streamResult);
      ///// Read the generated GraphML into the given graph /////
      readerDelegate.read(graph, (Document) xmlResult.getNode());
    }
    catch (SAXException e) {
      throw new IOException(e.getMessage());
    }
    catch (TransformerConfigurationException e) {
      throw new IOException(e.getMessage());
    } catch (TransformerException e) {
      throw new IOException(e.getMessage());
    }
  }


}
