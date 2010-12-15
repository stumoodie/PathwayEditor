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

import java.net.URL;

/**
 * todo TBE: add documentation
 *
 */
abstract class AbstractDemoDisplayable implements Displayable {
  String qualifiedName;
  String displayName;
  String summary;
  String description;
  URL base;
  int displayPriority;

  AbstractDemoDisplayable() {
    this.qualifiedName = "";
    this.displayName = "";
    this.description = "";
  }

  public String getQualifiedName() {
    return qualifiedName;
  }

  public String getDisplayName() {
    return displayName;
  }

  public String getSummary() {
    return summary;
  }

  public String getDescription() {
    return description;
  }

  public URL getBase() {
    return base;
  }
}
