/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.beans.reflect;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.ldaptive.LdapUtils;
import org.ldaptive.beans.Attribute;
import org.ldaptive.beans.Entry;

/**
 * Class for testing bean annotations.
 *
 * @author  Middleware Services
 */
public class CharCustomObject implements CustomObject
{

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 31;

  // CheckStyle:JavadocVariable OFF
  // CheckStyle:DeclarationOrder OFF
  private String customDn;
  private char[] type1;
  protected char[] type2;
  private char[] type3;
  private Collection<char[]> typeCol1;
  protected Collection<char[]> typeCol2;
  private Set<char[]> typeSet1;
  protected Set<char[]> typeSet2;
  private List<char[]> typeList1;
  protected List<char[]> typeList2;
  // CheckStyle:DeclarationOrder ON
  // CheckStyle:JavadocVariable ON


  // CheckStyle:JavadocMethod OFF
  // CheckStyle:LeftCurly OFF
  public CharCustomObject() {}
  public CharCustomObject(final String s) { setCustomDn(s); }


  public String getCustomDn() { return customDn; }
  public void setCustomDn(final String s) { customDn = s; }
  public char[] getType1() { return type1; }
  public void setType1(final char[] t) { type1 = t; }
  public void writeType2(final char[] t) { type2 = t; }
  public char[] getType3() { return type3; }
  public void setType3(final char[] t) { type3 = t; }
  public Collection<char[]> getTypeCol1() { return typeCol1; }
  public void setTypeCol1(final Collection<char[]> c) { typeCol1 = c; }
  public void writeTypeCol2(final Collection<char[]> c) { typeCol2 = c; }
  public Set<char[]> getTypeSet1() { return typeSet1; }
  public void setTypeSet1(final Set<char[]> s) { typeSet1 = s; }
  public void writeTypeSet2(final Set<char[]> s) { typeSet2 = s; }
  public List<char[]> getTypeList1() { return typeList1; }
  public void setTypeList1(final List<char[]> l) { typeList1 = l; }
  public void writeTypeList2(final List<char[]> l) { typeList2 = l; }
  // CheckStyle:LeftCurly ON
  // CheckStyle:JavadocMethod ON


  @Override
  public void initialize() {}


  @Override
  public boolean equals(final Object o)
  {
    return o != null && hashCode() == o.hashCode();
  }


  @Override
  public int hashCode()
  {
    return
      LdapUtils.computeHashCode(
        HASH_CODE_SEED,
        getCustomDn(),
        type1,
        type2,
        type3,
        typeCol1,
        typeCol2,
        typeSet1,
        typeSet2,
        typeList1,
        typeList2);
  }


  @Override
  public String toString()
  {
    return
      String.format(
        "[%s@%d::" +
        "customDn=%s, " +
        "type1=%s, type2=%s, type3=%s, " +
        "typeCol1=%s, typeCol2=%s, " +
        "typeSet1=%s, typeSet2=%s, " +
        "typeList1=%s, typeList2=%s]",
        getClass().getSimpleName(),
        hashCode(),
        getCustomDn(),
        Arrays.toString(type1),
        Arrays.toString(type2),
        Arrays.toString(type3),
        toString(typeCol1),
        toString(typeCol2),
        toString(typeSet1),
        toString(typeSet2),
        toString(typeList1),
        toString(typeList2));
  }


  /**
   * Returns a string representation of the supplied collection.
   *
   * @param  c  collection to represent as a string
   *
   * @return  collection as a string
   */
  private String toString(final Collection<char[]> c)
  {
    String s = null;
    if (c != null) {
      s = "";
      for (char[] t : c) {
        s += Arrays.toString(t);
      }
    }
    return s;
  }


  /**
   * Creates a char custom object for testing.
   *
   * @param  <T>  type of char custom object
   * @param  type  of char custom object
   *
   * @return  instance of char custom object
   */
  public static <T extends CharCustomObject> T createCustomObject(final Class<T> type)
  {
    final Set<char[]> s1 = new HashSet<>();
    s1.add(new char[] {'t', 's', 'v', '1'});
    s1.add(new char[] {'t', 's', 'v', '2'});

    final T o1;
    try {
      o1 = type.getDeclaredConstructor().newInstance();
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
    o1.setCustomDn("cn=String Entry,ou=people,dc=ldaptive,dc=org");
    o1.setType1(new char[] {'t', 'v', '1'});
    o1.writeType2(new char[] {'t', 'v', '2'});
    o1.setType3(new char[] {'t', 'v', '3'});
    o1.setTypeCol1(Arrays.asList(new char[] {'t', 'c', 'v', '1'}, new char[] {'t', 'c', 'v', '2'}));
    o1.writeTypeCol2(Arrays.asList(new char[] {'t', 'c', 'v', '1'}, new char[] {'t', 'c', 'v', '2'}));
    o1.setTypeSet1(s1);
    o1.writeTypeSet2(s1);
    o1.setTypeList1(Arrays.asList(new char[] {'t', 'l', 'v', '1'}, new char[] {'t', 'l', 'v', '2'}));
    o1.writeTypeList2(Arrays.asList(new char[] {'t', 'l', 'v', '1'}, new char[] {'t', 'l', 'v', '2'}));

    return o1;
  }


  /** Test class for the default ldap entry mapper. */
  @Entry(
    dn = "customDn", attributes = {
      @Attribute(name = "customname1", values = "customvalue1"),
      @Attribute(name = "customname2", values = {"customvalue1", "customvalue2"}),
      @Attribute(name = "type1", property = "type1"),
      @Attribute(name = "type2", property = "type2"),
      @Attribute(name = "stringthree", property = "type3"),
      @Attribute(name = "typeCol1", property = "typeCol1"),
      @Attribute(name = "typeCol2", property = "typeCol2"),
      @Attribute(name = "typeSet1", property = "typeSet1"),
      @Attribute(name = "typeSet2", property = "typeSet2"),
      @Attribute(name = "typeList1", property = "typeList1"),
      @Attribute(name = "typeList2", property = "typeList2")})
  public static class Default extends CharCustomObject {}


  /** Test class for the spring ldap entry mapper. */
  @Entry(
    dn = "customDn", attributes = {
      @Attribute(name = "customname1", values = "customvalue1"),
      @Attribute(name = "customname2", values = {"customvalue1", "customvalue2"}),
      @Attribute(name = "type1", property = "type1"),
      @Attribute(name = "type2", property = "type2"),
      @Attribute(name = "stringthree", property = "type3"),
      @Attribute(name = "typeCol1", property = "typeCol1"),
      @Attribute(name = "typeCol2", property = "typeCol2"),
      @Attribute(name = "typeSet1", property = "typeSet1"),
      @Attribute(name = "typeSet2", property = "typeSet2"),
      @Attribute(name = "typeList1", property = "typeList1"),
      @Attribute(name = "typeList2", property = "typeList2")})
  public static class Spring extends CharCustomObject
  {
    // CheckStyle:JavadocMethod OFF
    // CheckStyle:LeftCurly OFF
    public char[] getType2() { return type2; }
    public void setType2(final char[] t) { type2 = t; }
    public Collection<char[]> getTypeCol2() { return typeCol2; }
    public void setTypeCol2(final Collection<char[]> c) { typeCol2 = c; }
    public Set<char[]> getTypeSet2() { return typeSet2; }
    public void setTypeSet2(final Set<char[]> s) { typeSet2 = s; }
    public List<char[]> getTypeList2() { return typeList2; }
    public void setTypeList2(final List<char[]> l) { typeList2 = l; }
    // CheckStyle:LeftCurly ON
    // CheckStyle:JavadocMethod ON
  }
}
