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
package demo.view.flowchart.painters;

/**
 * Constants for flowchart painters.
 */
public interface FlowchartRealizerConstants {

  //Style properties used in several flowchart painters:
  
  public static final String PROPERTY_RADIUS = "com.yworks.flowchart.style.radius";
  public static final String PROPERTY_INCLINATION = "com.yworks.flowchart.style.inclination";
  public static final String PROPERTY_BORDER_DISTANCE = "com.yworks.flowchart.style.borderDistance";
  public static final String PROPERTY_SHADOW = "shadow";

  public static final String PROPERTY_ORIENTATION = "com.yworks.flowchart.style.orientation";
  public static final byte PROPERTY_ORIENTATION_VALUE_AUTO = 0;
  public static final byte PROPERTY_ORIENTATION_VALUE_DOWN = 1;
  public static final byte PROPERTY_ORIENTATION_VALUE_RIGHT = 2;
  public static final byte PROPERTY_ORIENTATION_VALUE_TOP = 3;
  public static final byte PROPERTY_ORIENTATION_VALUE_LEFT = 4;

  //For Factory
  public static final String FLOWCHART_PROCESS_CONFIG_NAME = "com.yworks.flowchart.process";
  public static final String FLOWCHART_DIRECT_DATA_CONFIG_NAME = "com.yworks.flowchart.directData";
  public static final String FLOWCHART_DATABASE_CONFIG_NAME = "com.yworks.flowchart.dataBase";
  public static final String FLOWCHART_DECISION_CONFIG_NAME = "com.yworks.flowchart.decision";
  public static final String FLOWCHART_DOCUMENT_CONFIG_NAME = "com.yworks.flowchart.document";
  public static final String FLOWCHART_DATA_CONFIG_NAME = "com.yworks.flowchart.data";
  public static final String FLOWCHART_START1_CONFIG_NAME = "com.yworks.flowchart.start1";
  public static final String FLOWCHART_START2_CONFIG_NAME = "com.yworks.flowchart.start2";
  public static final String FLOWCHART_PREDEFINED_PROCESS_CONFIG_NAME = "com.yworks.flowchart.predefinedProcess";
  public static final String FLOWCHART_STORED_DATA_CONFIG_NAME = "com.yworks.flowchart.storedData";
  public static final String FLOWCHART_INTERNAL_STORAGE_CONFIG_NAME = "com.yworks.flowchart.internalStorage";
  public static final String FLOWCHART_SEQUENTIAL_DATA_CONFIG_NAME = "com.yworks.flowchart.sequentialData";
  public static final String FLOWCHART_MANUAL_INPUT_CONFIG_NAME = "com.yworks.flowchart.manualInput";
  public static final String FLOWCHART_CARD_CONFIG_NAME = "com.yworks.flowchart.card";
  public static final String FLOWCHART_PAPER_TYPE_CONFIG_NAME = "com.yworks.flowchart.paperType";
  public static final String FLOWCHART_CLOUD_TYPE_CONFIG_NAME = "com.yworks.flowchart.cloud";
  public static final String FLOWCHART_DELAY_CONFIG_NAME = "com.yworks.flowchart.delay";
  public static final String FLOWCHART_DISPLAY_CONFIG_NAME = "com.yworks.flowchart.display";
  public static final String FLOWCHART_MANUAL_OPERATION_CONFIG_NAME = "com.yworks.flowchart.manualOperation";
  public static final String FLOWCHART_PREPARATION_CONFIG_NAME = "com.yworks.flowchart.preparation";
  public static final String FLOWCHART_LOOP_LIMIT_CONFIG_NAME = "com.yworks.flowchart.loopLimit";
  public static final String FLOWCHART_TERMINATOR_CONFIG_NAME = "com.yworks.flowchart.terminator";
  public static final String FLOWCHART_ON_PAGE_REFERENCE_CONFIG_NAME = "com.yworks.flowchart.onPageReference";
  public static final String FLOWCHART_OFF_PAGE_REFERENCE_CONFIG_NAME = "com.yworks.flowchart.offPageReference";
  public static final String FLOWCHART_ANNOTATION_CONFIG_NAME = "com.yworks.flowchart.annotation";

  public static final double FLOWCHART_DEFAULT_DIRECT_DATA_RADIUS = 0.125;
  public static final double FLOWCHART_DEFAULT_DOCUMENT_RADIUS = 0.125;
  public static final double FLOWCHART_DEFAULT_DATA_INCLINATION = 0.255;
  public static final double FLOWCHART_DEFAULT_PREDEFINED_PROCESS_BORDER_DISTANCE = 10;
  public static final double FLOWCHART_DEFAULT_STORED_DATA_RADIUS = 0.125;
  public static final double FLOWCHART_DEFAULT_INTERNAL_STORAGE_BORDER_DISTANCE = 10;
  public static final double FLOWCHART_DEFAULT_MANUAL_INPUT_BORDER_DISTANCE = 10;
  public static final double FLOWCHART_DEFAULT_CARD_BORDER_DISTANCE = 10;
  public static final double FLOWCHART_DEFAULT_PAPER_TAPE_RADIUS = 0.125;
  public static final double FLOWCHART_DEFAULT_DELAY_RADIUS = 0.125;
  public static final double FLOWCHART_DEFAULT_DISPLAY_RADIUS = 0.125;
  public static final double FLOWCHART_DEFAULT_MANUAL_OPERATION_BORDER_DISTANCE = 10;

}
