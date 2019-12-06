/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.beans.reflect;

import java.lang.reflect.InvocationTargetException;
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
public class IntegerCustomObject implements CustomObject
{

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 41;

  // CheckStyle:JavadocVariable OFF
  // CheckStyle:DeclarationOrder OFF
  private String integerDn;
  private Integer type1;
  protected Integer type2;
  private Integer type3;
  private Integer[] typeArray1;
  protected Integer[] typeArray2;
  private Collection<Integer> typeCol1;
  protected Collection<Integer> typeCol2;
  private Set<Integer> typeSet1;
  protected Set<Integer> typeSet2;
  private List<Integer> typeList1;
  protected List<Integer> typeList2;
  // CheckStyle:DeclarationOrder ON
  // CheckStyle:JavadocVariable ON


  // CheckStyle:JavadocMethod OFF
  // CheckStyle:LeftCurly OFF
  public IntegerCustomObject() {}
  public IntegerCustomObject(final String s) { setIntegerDn(s); }


  public String getIntegerDn() { return integerDn; }
  public void setIntegerDn(final String s) { integerDn = s; }
  public Integer getType1() { return type1; }
  public void setType1(final Integer t) { type1 = t; }
  public void writeType2(final Integer t) { type2 = t; }
  public Integer getType3() { return type3; }
  public void setType3(final Integer t) { type3 = t; }
  public Integer[] getTypeArray1() { return typeArray1; }
  public void setTypeArray1(final Integer[] t) { typeArray1 = t; }
  public void writeTypeArray2(final Integer[] t) { typeArray2 = t; }
  public Collection<Integer> getTypeCol1() { return typeCol1; }
  public void setTypeCol1(final Collection<Integer> c) { typeCol1 = c; }
  public void writeTypeCol2(final Collection<Integer> c) { typeCol2 = c; }
  public Set<Integer> getTypeSet1() { return typeSet1; }
  public void setTypeSet1(final Set<Integer> s) { typeSet1 = s; }
  public void writeTypeSet2(final Set<Integer> s) { typeSet2 = s; }
  public List<Integer> getTypeList1() { return typeList1; }
  public void setTypeList1(final List<Integer> l) { typeList1 = l; }
  public void writeTypeList2(final List<Integer> l) { typeList2 = l; }
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
        integerDn,
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
        integerDn,
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
   * Creates an integer custom object for testing.
   *
   * @param  <T>  type of integer custom object
   * @param  type  of integer custom object
   *
   * @return  instance of integer custom object
   */
  public static <T extends IntegerCustomObject> T createCustomObject(final Class<T> type)
  {
    // CheckStyle:MagicNumber OFF
    final Set<Integer> s1 = new HashSet<>();
    s1.add(601);
    s1.add(602);

    final T o1;
    try {
      o1 = type.getDeclaredConstructor().newInstance();
    } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
      throw new IllegalStateException(e);
    }
    o1.setIntegerDn("cn=Integer Entry,ou=people,dc=ldaptive,dc=org");
    o1.setType1(100);
    o1.writeType2(200);
    o1.setType3(300);
    o1.setTypeArray1(new Integer[] {301, 302});
    o1.writeTypeArray2(new Integer[] {301, 302});
    o1.setTypeCol1(Arrays.asList(501, 502));
    o1.writeTypeCol2(Arrays.asList(501, 502));
    o1.setTypeSet1(s1);
    o1.writeTypeSet2(s1);
    o1.setTypeList1(Arrays.asList(701, 702));
    o1.writeTypeList2(Arrays.asList(701, 702));

    return o1;
    // CheckStyle:MagicNumber ON
  }


  /**
   * Creates an ldap entry containing integer based string values.
   *
   * @return  ldap entry
   */
  public static LdapEntry createLdapEntry()
  {
    final LdapAttribute typeArray1 = new LdapAttribute();
    typeArray1.setName("typeArray1");
    typeArray1.addStringValues("301", "302");

    final LdapAttribute typeArray2 = new LdapAttribute();
    typeArray2.setName("typeArray2");
    typeArray2.addStringValues("301", "302");

    final LdapAttribute typeCol1 = new LdapAttribute();
    typeCol1.setName("typeCol1");
    typeCol1.addStringValues("501", "502");

    final LdapAttribute typeCol2 = new LdapAttribute();
    typeCol2.setName("typeCol2");
    typeCol2.addStringValues("501", "502");

    final LdapAttribute typeSet1 = new LdapAttribute();
    typeSet1.setName("typeSet1");
    typeSet1.addStringValues("601", "602");

    final LdapAttribute typeSet2 = new LdapAttribute();
    typeSet2.setName("typeSet2");
    typeSet2.addStringValues("601", "602");

    final LdapAttribute typeList1 = new LdapAttribute();
    typeList1.setName("typeList1");
    typeList1.addStringValues("701", "702");

    final LdapAttribute typeList2 = new LdapAttribute();
    typeList2.setName("typeList2");
    typeList2.addStringValues("701", "702");

    final LdapEntry entry = new LdapEntry();
    entry.setDn("cn=Integer Entry,ou=people,dc=ldaptive,dc=org");
    entry.addAttributes(
      new LdapAttribute("type1", "100"),
      new LdapAttribute("type2", "200"),
      new LdapAttribute("numberthree", "300"),
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
    dn = "integerDn", attributes = {
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
  public static class Default extends IntegerCustomObject {}


  /** Test class for the spring ldap entry mapper. */
  @Entry(
    dn = "integerDn", attributes = {
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
  public static class Spring extends IntegerCustomObject
  {
    // CheckStyle:JavadocMethod OFF
    // CheckStyle:LeftCurly OFF
    public Integer getType2() { return type2; }
    public void setType2(final Integer t) { type2 = t; }
    public Integer[] getTypeArray2() { return typeArray2; }
    public void setTypeArray2(final Integer[] t) { typeArray2 = t; }
    public Collection<Integer> getTypeCol2() { return typeCol2; }
    public void setTypeCol2(final Collection<Integer> c) { typeCol2 = c; }
    public Set<Integer> getTypeSet2() { return typeSet2; }
    public void setTypeSet2(final Set<Integer> s) { typeSet2 = s; }
    public List<Integer> getTypeList2() { return typeList2; }
    public void setTypeList2(final List<Integer> l) { typeList2 = l; }
    // CheckStyle:LeftCurly ON
    // CheckStyle:JavadocMethod ON
  }
}
