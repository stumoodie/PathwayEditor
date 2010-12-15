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

public class Employee {
  int id;
  String firstname;
  String lastname;
  boolean isMale;
  String jobTitle;
  Department department;
  String room;
  int phoneExtension;
  int supervisor;

  public Employee() {
  }

  public Employee(int id, String firstname, String lastname, boolean male, String jobTitle, Department department,
      String room, int phoneExtension, int supervisor) {
    this.id = id;
    this.firstname = firstname;
    this.lastname = lastname;
    isMale = male;
    this.jobTitle = jobTitle;
    this.department = department;
    this.room = room;
    this.phoneExtension = phoneExtension;
    this.supervisor = supervisor;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getFirstname() {
    return firstname;
  }

  public void setFirstname(String firstname) {
    this.firstname = firstname;
  }

  public String getLastname() {
    return lastname;
  }

  public void setLastname(String lastname) {
    this.lastname = lastname;
  }

  public boolean isMale() {
    return isMale;
  }

  public void setMale(boolean male) {
    isMale = male;
  }

  public String getJobTitle() {
    return jobTitle;
  }

  public void setJobTitle(String jobTitle) {
    this.jobTitle = jobTitle;
  }

  public Department getDepartment() {
    return department;
  }

  public void setDepartment(Department department) {
    this.department = department;
  }

  public String getRoom() {
    return room;
  }

  public void setRoom(String room) {
    this.room = room;
  }

  public int getPhoneExtension() {
    return phoneExtension;
  }

  public void setPhoneExtension(int phoneExtension) {
    this.phoneExtension = phoneExtension;
  }

  public int getSupervisor() {
    return supervisor;
  }

  public void setSupervisor(int supervisor) {
    this.supervisor = supervisor;
  }
}
