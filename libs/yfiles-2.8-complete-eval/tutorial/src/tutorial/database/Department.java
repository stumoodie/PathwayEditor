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
package tutorial.database;

import java.util.ArrayList;
import java.util.List;

public class Department {
  private String name;
  private List employees;

  public Department(String name) {
    this.name = name;
    employees = new ArrayList();
  }

  public void addEmployee(Employee employee) {
    employees.add(employee);
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List getEmployees() {
    return employees;
  }

  public void setEmployees(List employees) {
    this.employees = employees;
  }
}
