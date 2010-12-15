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
package demo.base;

import y.base.ListCell;
import y.base.YCursor;
import y.base.YList;

import java.util.Comparator;

/**
 * Demonstrates how to use the linked list data type YList and the YCursor interface.
 * <p>
 * <b>usage:</b> java demo.base.ListDemo
 * </p>
 */
public class ListDemo 
{
  public ListDemo()
  {
    //create new YList instance
    YList list = new YList();
    
    //add numbered String elements to list 
    for(int i = 0; i < 20; i++)
      list.addLast(""+i);
    
    //iterate over list from first to last
    System.out.println("List elements from front to back");
    for(YCursor c = list.cursor(); c.ok(); c.next())
    {
      //output element at cursor position
      System.out.println(c.current());
    }
    
    //iterate over list from last to first
    System.out.println("List elements from back to front");
    YCursor rc = list.cursor();
    for(rc.toLast(); rc.ok(); rc.prev())
    {
      //output element at cursor position
      System.out.println(rc.current());
    }
    
    //sort list lexicografically
    list.sort(new Comparator() 
              {
                public int compare(Object a, Object b)
                  {
                    return ((String)a).compareTo((String)b);
                  }
              });
    
    
    //iterate over list from first to last
    System.out.println("Lexicographically sorted list");
    for(YCursor c = list.cursor(); c.ok(); c.next())
    {
      //output element at cursor position
      System.out.println(c.current());
    }
    
    //low level iteration on list cells (non-const iteration)
    for(ListCell cell = list.firstCell(); cell != null; cell = cell.succ())
    {
      String s = (String)cell.getInfo();
      //remove all Strings from list that have length == 1 
      if(s.length() == 1)
      {
        list.removeCell(cell); 
        //note that cell is still half-valid, i.e it's succ() and pred() 
        //pointers are still unchanged. therefore cell = cell.succ() is
        //valid in the for-statement
      }
    }
    
    System.out.println("list after element removal");
    System.out.println(list);
    
    //initialize list2 with the elements from list
    YList list2 = new YList(list.cursor());
    System.out.println("list2 after creation");
    System.out.println(list2);
    
    //reverse element order in list2
    list2.reverse();
    System.out.println("list2 after reversal");
    System.out.println(list2);
    
    //move all elements of list2 to the end of list
    list.splice(list2);
    
    System.out.println("list after splicing");
    System.out.println(list);
    System.out.println("list2 after splicing");
    System.out.println(list2);
    
  }
  
  public static void main(String[] args)
  {
    new ListDemo();
  }
  
}