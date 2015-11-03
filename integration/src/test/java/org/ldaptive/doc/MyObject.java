/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.doc;

import org.ldaptive.beans.Attribute;
import org.ldaptive.beans.Entry;

/**
 * Sample object used in docs.
 */
@Entry(
  dn = "dn",
  attributes = {
    @Attribute(name = "name"),
    @Attribute(name = "email"),
    @Attribute(name = "phoneNumber")})
public class MyObject {
  private String dn;
  private String name;
  private String email;
  private String phoneNumber;

  public MyObject() {}
  public MyObject(String s) { setDn(s); }
  public String getDn() { return dn; }
  public void setDn(String s) { dn = s; }
  public String getName() { return name; }
  public void setName(String s) { name = s; }
  public String getEmail() { return email; }
  public void setEmail(String s) { email = s; }
  public String getPhoneNumber() { return phoneNumber; }
  public void setPhoneNumber(String s) { phoneNumber = s; }
}