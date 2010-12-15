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

public class Organization {
  private String name;
  private List departments;
  private List employees;

  public Organization(String name) {
    this.name = name;
    departments = new ArrayList();
    employees = new ArrayList();
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List getDepartments() {
    return departments;
  }

  public void setDepartments(List departments) {
    this.departments = departments;
  }

  public List getEmployees() {
    return employees;
  }

  public void setEmployees(List employees) {
    this.employees = employees;
  }

  void addDepartment(Department department) {
    departments.add(department);
  }

  void addEmployee(Employee employee) {
    employees.add(employee);
  }
}
