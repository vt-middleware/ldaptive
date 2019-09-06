/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.beans.reflect;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.ldaptive.LdapUtils;
import org.ldaptive.beans.Attribute;
import org.ldaptive.beans.Entry;

/**
 * Class for testing bean annotations.
 *
 * @author  Middleware Services
 */
public class FloatCustomObject implements CustomObject
{

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 41;

  // CheckStyle:JavadocVariable OFF
  // CheckStyle:DeclarationOrder OFF
  private String floatDn;
  private Float type1;
  protected Float type2;
  private Float type3;
  private Float[] typeArray1;
  protected Float[] typeArray2;
  private Collection<Float> typeCol1;
  protected Collection<Float> typeCol2;
  private Set<Float> typeSet1;
  protected Set<Float> typeSet2;
  private List<Float> typeList1;
  protected List<Float> typeList2;
  // CheckStyle:DeclarationOrder ON
  // CheckStyle:JavadocVariable ON


  // CheckStyle:JavadocMethod OFF
  // CheckStyle:LeftCurly OFF
  public FloatCustomObject() {}
  public FloatCustomObject(final String s) { setFloatDn(s); }


  public String getFloatDn() { return floatDn; }
  public void setFloatDn(final String s) { floatDn = s; }
  public Float getType1() { return type1; }
  public void setType1(final Float t) { type1 = t; }
  public void writeType2(final Float t) { type2 = t; }
  public Float getType3() { return type3; }
  public void setType3(final Float t) { type3 = t; }
  public Float[] getTypeArray1() { return typeArray1; }
  public void setTypeArray1(final Float[] t) { typeArray1 = t; }
  public void writeTypeArray2(final Float[] t) { typeArray2 = t; }
  public Collection<Float> getTypeCol1() { return typeCol1; }
  public void setTypeCol1(final Collection<Float> c) { typeCol1 = c; }
  public void writeTypeCol2(final Collection<Float> c) { typeCol2 = c; }
  public Set<Float> getTypeSet1() { return typeSet1; }
  public void setTypeSet1(final Set<Float> s) { typeSet1 = s; }
  public void writeTypeSet2(final Set<Float> s) { typeSet2 = s; }
  public List<Float> getTypeList1() { return typeList1; }
  public void setTypeList1(final List<Float> l) { typeList1 = l; }
  public void writeTypeList2(final List<Float> l) { typeList2 = l; }
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
        floatDn,
        type1,
        type2,
        type3,
        typeArray1,
        typeArray2,
        typeCol1 != null ? Collections.unmodifiableCollection(typeCol1) : null,
        typeCol2 != null ? Collections.unmodifiableCollection(typeCol2) : null,
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
        "integerDn=%s, " +
        "type1=%s, type2=%s, type3=%s, " +
        "typeArray1=%s, typeArray2=%s, " +
        "typeCol1=%s, typeCol2=%s, " +
        "typeSet1=%s, typeSet2=%s, " +
        "typeList1=%s, typeList2=%s]",
        getClass().getSimpleName(),
        hashCode(),
        floatDn,
        type1,
        type2,
        type3,
        Arrays.toString(typeArray1),
        Arrays.toString(typeArray2),
        typeCol1,
        typeCol2,
        typeSet1,
        typeSet2,
        typeList1,
        typeList2);
  }


  /**
   * Creates a float custom object for testing.
   *
   * @param  <T>  type of float custom object
   * @param  type  of float custom object
   *
   * @return  instance of float custom object
   */
  public static <T extends FloatCustomObject> T createCustomObject(final Class<T> type)
  {
    // CheckStyle:MagicNumber OFF
    final Set<Float> s1 = new HashSet<>();
    s1.add(601.6f);
    s1.add(602.6f);

    final T o1;
    try {
      o1 = type.getDeclaredConstructor().newInstance();
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
    o1.setFloatDn("cn=Float Entry,ou=people,dc=ldaptive,dc=org");
    o1.setType1(100.1f);
    o1.writeType2(200.2f);
    o1.setType3(300.3f);
    o1.setTypeArray1(new Float[] {301.1f, 302.2f});
    o1.writeTypeArray2(new Float[] {301.1f, 302.2f});
    o1.setTypeCol1(Arrays.asList(501.5f, 502.5f));
    o1.writeTypeCol2(Arrays.asList(501.5f, 502.5f));
    o1.setTypeSet1(s1);
    o1.writeTypeSet2(s1);
    o1.setTypeList1(Arrays.asList(701.7f, 702.7f));
    o1.writeTypeList2(Arrays.asList(701.7f, 702.7f));

    return o1;
    // CheckStyle:MagicNumber ON
  }


  /**
   * Creates an ldap entry containing float based string values.
   *
   * @return  ldap entry
   */
  public static LdapEntry createLdapEntry()
  {
    final LdapAttribute typeArray1 = new LdapAttribute();
    typeArray1.setName("typeArray1");
    typeArray1.addStringValues("301.1", "302.2");

    final LdapAttribute typeArray2 = new LdapAttribute();
    typeArray2.setName("typeArray2");
    typeArray2.addStringValues("301.1", "302.2");

    final LdapAttribute typeCol1 = new LdapAttribute();
    typeCol1.setName("typeCol1");
    typeCol1.addStringValues("501.5", "502.5");

    final LdapAttribute typeCol2 = new LdapAttribute();
    typeCol2.setName("typeCol2");
    typeCol2.addStringValues("501.5", "502.5");

    final LdapAttribute typeSet1 = new LdapAttribute();
    typeSet1.setName("typeSet1");
    typeSet1.addStringValues("601.6", "602.6");

    final LdapAttribute typeSet2 = new LdapAttribute();
    typeSet2.setName("typeSet2");
    typeSet2.addStringValues("601.6", "602.6");

    final LdapAttribute typeList1 = new LdapAttribute();
    typeList1.setName("typeList1");
    typeList1.addStringValues("701.7", "702.7");

    final LdapAttribute typeList2 = new LdapAttribute();
    typeList2.setName("typeList2");
    typeList2.addStringValues("701.7", "702.7");

    final LdapEntry entry = new LdapEntry();
    entry.setDn("cn=Float Entry,ou=people,dc=ldaptive,dc=org");
    entry.addAttributes(
      new LdapAttribute("type1", "100.1"),
      new LdapAttribute("type2", "200.2"),
      new LdapAttribute("numberthree", "300.3"),
      typeArray1,
      typeArray2,
      typeCol1,
      typeCol2,
      typeSet1,
      typeSet2,
      typeList1,
      typeList2);
    return entry;
  }


  /** Test class for the default ldap entry mapper. */
  @Entry(
    dn = "floatDn", attributes = {
      @Attribute(name = "type1", property = "type1"),
      @Attribute(name = "type2", property = "type2"),
      @Attribute(name = "numberthree", property = "type3"),
      @Attribute(name = "typeArray1", property = "typeArray1"),
      @Attribute(name = "typeArray2", property = "typeArray2"),
      @Attribute(name = "typeCol1", property = "typeCol1"),
      @Attribute(name = "typeCol2", property = "typeCol2"),
      @Attribute(name = "typeSet1", property = "typeSet1"),
      @Attribute(name = "typeSet2", property = "typeSet2"),
      @Attribute(name = "typeList1", property = "typeList1"),
      @Attribute(name = "typeList2", property = "typeList2")})
  public static class Default extends FloatCustomObject {}


  /** Test class for the spring ldap entry mapper. */
  @Entry(
    dn = "floatDn", attributes = {
      @Attribute(name = "type1", property = "type1"),
      @Attribute(name = "type2", property = "type2"),
      @Attribute(name = "numberthree", property = "type3"),
      @Attribute(name = "typeArray1", property = "typeArray1"),
      @Attribute(name = "typeArray2", property = "typeArray2"),
      @Attribute(name = "typeCol1", property = "typeCol1"),
      @Attribute(name = "typeCol2", property = "typeCol2"),
      @Attribute(name = "typeSet1", property = "typeSet1"),
      @Attribute(name = "typeSet2", property = "typeSet2"),
      @Attribute(name = "typeList1", property = "typeList1"),
      @Attribute(name = "typeList2", property = "typeList2")})
  public static class Spring extends FloatCustomObject
  {
    // CheckStyle:JavadocMethod OFF
    // CheckStyle:LeftCurly OFF
    public Float getType2() { return type2; }
    public void setType2(final Float t) { type2 = t; }
    public Float[] getTypeArray2() { return typeArray2; }
    public void setTypeArray2(final Float[] t) { typeArray2 = t; }
    public Collection<Float> getTypeCol2() { return typeCol2; }
    public void setTypeCol2(final Collection<Float> c) { typeCol2 = c; }
    public Set<Float> getTypeSet2() { return typeSet2; }
    public void setTypeSet2(final Set<Float> s) { typeSet2 = s; }
    public List<Float> getTypeList2() { return typeList2; }
    public void setTypeList2(final List<Float> l) { typeList2 = l; }
    // CheckStyle:LeftCurly ON
    // CheckStyle:JavadocMethod ON
  }
}
