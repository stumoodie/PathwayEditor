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
package demo.view.orgchart;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * TreeModel that uses {@link Employee}s as TreeNodes.
 */
public class OrgChartTreeModel extends DefaultTreeModel {
  
  /**
   * Creates a tree model with the given root.
   */
  public OrgChartTreeModel(Employee root) {
    super(root);
  }

  /**
   * A TreeNode implementation that represents an Employee in an Organization.  
   */
  public static class Employee extends DefaultMutableTreeNode {
    public String name;
    public String email;
    public String phone;
    public String fax;
    public String businessUnit;
    public String position;
    public String status;
    public Icon icon;
    public String assistant;    
    public String layout;    
  }
  
  /**
   * Creates an instance of this class from an XML stream. 
   * A sample XML file is located at resources/orgchartmodel.xml. 
   */
  public static OrgChartTreeModel create(InputSource input) {
    OrgChartReader reader = new OrgChartReader();
    return reader.read(input);    
  }
  
  /**
   * A reader for XML-formatted XML-files.
   */
  static class OrgChartReader {

    OrgChartTreeModel model;
    
    static Map userIcons;
    static {    
      userIcons = new HashMap();
      for(int type = 0; type <= 1; type++) {
        String gender = type == 0 ? "male" : "female";
        for(int user = 1; user <= 3; user++) {
          String key = "usericon_" + gender + user;
          ArrayList urls = new ArrayList(4);
          int size = 256;
          //for(int size = 256; size <= 256; size *= 2) { 
            urls.add(OrgChartReader.class.getResource("resources/icons/" + key + "_" + size + ".png"));
          //}
          userIcons.put(key,new MultiResolutionImageIcon(urls, 55,64));
        }
      };      
    }
    
    public OrgChartTreeModel read(InputSource input) {
      Document doc;
      
      try {
        doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(input);
        Employee treeRoot = visit(doc);
        OrgChartTreeModel model = new OrgChartTreeModel(treeRoot);
        return model;
      } catch (SAXException e) {
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      } catch (ParserConfigurationException e) {
        e.printStackTrace();
      }    
      return null;
    }
    
    public Employee visit(Node node)
    {
      String nodeName = node.getNodeName();
      Employee employee = null;
      if("employee".equals(nodeName)) {
        employee = new Employee();        
        
        NamedNodeMap attributes = node.getAttributes();
        Node attr = attributes.getNamedItem("name");      
        if(attr != null) {
          employee.name = attr.getNodeValue();
        }
        attr = attributes.getNamedItem("layout");      
        if(attr != null) {
          employee.layout = attr.getNodeValue();
        }
        attr = attributes.getNamedItem("email");      
        if(attr != null) {
          employee.email = attr.getNodeValue();
        }
        attr = attributes.getNamedItem("phone");      
        if(attr != null) {
          employee.phone = attr.getNodeValue();
        }
        attr = attributes.getNamedItem("position");      
        if(attr != null) {
          employee.position = attr.getNodeValue();
        }    
        attr = attributes.getNamedItem("fax");      
        if(attr != null) {
          employee.fax = attr.getNodeValue();
        }
        attr = attributes.getNamedItem("businessUnit");      
        if(attr != null) {
          employee.businessUnit = attr.getNodeValue();
        }
        attr = attributes.getNamedItem("status");      
        if(attr != null) {
          String status = attr.getNodeValue();
          employee.status = status;                 
        }
        attr = attributes.getNamedItem("icon");      
        if(attr != null) {
          String iconName = attr.getNodeValue();
          employee.icon = (Icon) userIcons.get(iconName);                
        }
        attr = attributes.getNamedItem("assistant");      
        if(attr != null) {
          employee.assistant = attr.getNodeValue();
        }
      }    
      NodeList nl = node.getChildNodes();      
      for(int i=0, cnt=nl.getLength(); i<cnt; i++)
      {         
        Node n = nl.item(i);
        Employee childNode = visit(n);
        if(childNode != null && employee != null) {
          employee.add(childNode);        
        }      
        if(childNode != null && employee == null) {
          return childNode;
        }
      }
      return employee;
    }
  }
  
  /**
   * Icon implementation that renders an image at a size that is 
   * different than the image dimensions. 
   */
  static class FixedSizeImageIcon extends ImageIcon {
        
    int width;
    int height;
    
    public FixedSizeImageIcon(URL imageURL, int width, int height) {    
      super(imageURL);    
      this.width = width;
      this.height = height;
    }
    
    public int getIconHeight() {
      return height;
    }

    public int getIconWidth() {
      return width;
    }

    public void paintIcon(Component c, Graphics gfx, int x, int y) {
      Image image = getImage();    
      Graphics2D g2d = (Graphics2D) gfx.create();
      Object hint = g2d.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
      g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);    
      gfx.drawImage(image, x, y, getIconWidth(), getIconHeight(), c);     
    }
  }

  /**
   * Icon implementation that displays one of multiple images depending 
   * on the original image size and the current zoom applied to the
   * graphics context. The idea is to pick an image whose original size 
   * fits the screen size of the image best. 
   */
  static class MultiResolutionImageIcon implements Icon {
    
    int width;
    int height;  
    ArrayList entries;
    
    static class Entry {
      double ratio;
      Icon icon;    
      URL url;
    }
    
    public MultiResolutionImageIcon(URL[] imageURLs, int width, int height) {
      this(Arrays.asList(imageURLs), width, height);
    }
    
    public MultiResolutionImageIcon(Collection imageURLs, int width, int height) {    
      entries = new ArrayList(imageURLs.size());
      for(Iterator iter = imageURLs.iterator(); iter.hasNext(); ) {
        URL imageURL = (URL) iter.next();
        ImageIcon icon = new FixedSizeImageIcon(imageURL, width,height);      
        Image image = icon.getImage();
        double w = image.getWidth(null);
        double h = image.getHeight(null);
        double ratio = Math.min(w/width, h/height);
        Entry entry = new Entry();
        entry.ratio = ratio;
        entry.icon = icon;
        entry.url = imageURL;
        entries.add(entry);
      }        
      this.width = width;
      this.height = height;
    }
    
    public int getIconHeight() {
      return height;
    }

    public int getIconWidth() {
      return width;
    }

    public void paintIcon(Component c, Graphics gfx, int x, int y) {
      Graphics2D g2d = (Graphics2D) gfx;
      double scale = g2d.getTransform().getScaleX();
      Entry bestEntry = null;
      double bestDelta = Double.MAX_VALUE;
      for(int i = 0; i < entries.size(); i++) {
        Entry entry = (Entry) entries.get(i);
        double ratio = entry.ratio;
        double delta = Math.abs(ratio-scale);
        if(delta < bestDelta) {
          bestDelta = delta;
          bestEntry = entry;
        }
      }
      if(bestEntry != null) {
        bestEntry.icon.paintIcon(c, gfx, x,y);     
      }
    }
  }

}
