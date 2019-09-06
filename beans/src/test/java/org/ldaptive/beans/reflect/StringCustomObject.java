/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.beans.reflect;

import java.util.ArrayList;
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
 * Class for testing beans annotations.
 *
 * @author  Middleware Services
 */
public class StringCustomObject implements CustomObject
{

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 11;

  // CheckStyle:JavadocVariable OFF
  // CheckStyle:DeclarationOrder OFF
  private String type1;
  protected String type2;
  private String type3;
  private String[] typeArray1;
  protected String[] typeArray2;
  private Collection col1;
  protected Collection col2;
  private Collection<String> typeCol1;
  protected Collection<String> typeCol2;
  private Set<String> typeSet1;
  protected Set<String> typeSet2;
  private List<String> typeList1;
  protected List<String> typeList2;
  private String transcoded;
  private final String[] nullArray1 = new String[] {null};
  private List<String> nullList1;
  // CheckStyle:DeclarationOrder ON
  // CheckStyle:JavadocVariable ON


  // CheckStyle:JavadocMethod OFF
  // CheckStyle:LeftCurly OFF
  public String getType1() { return type1; }
  public void setType1(final String t) { type1 = t; }
  public void writeType2(final String t) { type2 = t; }
  public String getType3() { return type3; }
  public void setType3(final String t) { type3 = t; }
  public String[] getTypeArray1() { return typeArray1; }
  public void setTypeArray1(final String[] t) { typeArray1 = t; }
  public void writeTypeArray2(final String[] t) { typeArray2 = t; }
  public Collection getCol1() { return col1; }
  public void setCol1(final Collection c) { col1 = c; }
  public void writeCol2(final Collection c) { col2 = c; }
  public Collection<String> getTypeCol1() { return typeCol1; }
  public void setTypeCol1(final Collection<String> c) { typeCol1 = c; }
  public void writeTypeCol2(final Collection<String> c) { typeCol2 = c; }
  public Set<String> getTypeSet1() { return typeSet1; }
  public void setTypeSet1(final Set<String> s) { typeSet1 = s; }
  public void writeTypeSet2(final Set<String> s) { typeSet2 = s; }
  public List<String> getTypeList1() { return typeList1; }
  public void setTypeList1(final List<String> l) { typeList1 = l; }
  public void writeTypeList2(final List<String> l) { typeList2 = l; }
  public String getTranscoded() { return transcoded; }
  public void setTranscoded(final String t) { transcoded = t; }
  public String[] getNullArray1() { return nullArray1; }
  public List<String> getNullList1() { return nullList1; }
  // CheckStyle:LeftCurly ON
  // CheckStyle:JavadocMethod ON


  @Override
  public void initialize()
  {
    nullList1 = new ArrayList<>(1);
    nullList1.add(null);
  }


  @Override
  public boolean equals(final Object o)
  {
    return o != null && hashCode() == o.hashCode();
  }


  @Override
  @SuppressWarnings("unchecked")
  public int hashCode()
  {
    return
      LdapUtils.computeHashCode(
        HASH_CODE_SEED,
        type1,
        type2,
        type3,
        typeArray1,
        typeArray2,
        col1 != null ? Collections.unmodifiableCollection(col1) : null,
        col2 != null ? Collections.unmodifiableCollection(col2) : null,
        typeCol1 != null ? Collections.unmodifiableCollection(typeCol1) : null,
        typeCol2 != null ? Collections.unmodifiableCollection(typeCol2) : null,
        typeSet1,
        typeSet2,
        typeList1,
        typeList2,
        transcoded);
  }


  @Override
  public String toString()
  {
    return
      String.format(
        "[%s@%d::type1=%s, type2=%s, type3=%s, typeArray1=%s, typeArray2=%s, " +
        "col1=%s, col2=%s, typeCol1=%s, typeCol2=%s, " +
        "typeSet1=%s, typeSet2=%s, typeList1=%s, typeList2=%s, transcoded=%s]",
        getClass().getSimpleName(),
        hashCode(),
        type1,
        type2,
        type3,
        Arrays.toString(typeArray1),
        Arrays.toString(typeArray2),
        col1,
        col2,
        typeCol1,
        typeCol2,
        typeSet1,
        typeSet2,
        typeList1,
        typeList2,
        transcoded);
  }


  /**
   * Creates a string custom object for testing.
   *
   * @param  <T>  type of string custom object
   * @param  type  of string custom object
   *
   * @return  instance of string custom object
   */
  public static <T extends StringCustomObject> T createCustomObject(final Class<T> type)
  {
    final Set<String> s1 = new HashSet<>();
    s1.add("tsv1");
    s1.add("tsv2");

    final T o1;
    try {
      o1 = type.getDeclaredConstructor().newInstance();
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
    o1.setType1("tv1");
    o1.writeType2("tv2");
    o1.setType3("tv3");
    o1.setTypeArray1(new String[] {"tav1", "tav2"});
    o1.writeTypeArray2(new String[] {"tav1", "tav2"});
    o1.setCol1(Arrays.asList("cv1", "cv2"));
    o1.writeCol2(Arrays.asList("cv1", "cv2"));
    o1.setTypeCol1(Arrays.asList("tcv1", "tcv2"));
    o1.writeTypeCol2(Arrays.asList("tcv1", "tcv2"));
    o1.setTypeSet1(s1);
    o1.writeTypeSet2(s1);
    o1.setTypeList1(Arrays.asList("tlv1", "tlv2"));
    o1.writeTypeList2(Arrays.asList("tlv1", "tlv2"));
    o1.setTranscoded("transcoded");

    return o1;
  }


  /**
   * Creates an ldap entry containing string values.
   *
   * @return  ldap entry
   */
  public static LdapEntry createLdapEntry()
  {
    final LdapAttribute typeArray1 = new LdapAttribute();
    typeArray1.setName("typeArray1");
    typeArray1.addStringValues("tav1", "tav2");

    final LdapAttribute typeArray2 = new LdapAttribute();
    typeArray2.setName("typeArray2");
    typeArray2.addStringValues("tav1", "tav2");

    final LdapAttribute col1 = new LdapAttribute();
    col1.setName("col1");
    col1.addStringValues("cv1", "cv2");

    final LdapAttribute col2 = new LdapAttribute();
    col2.setName("col2");
    col2.addStringValues("cv1", "cv2");

    final LdapAttribute typeCol1 = new LdapAttribute();
    typeCol1.setName("typeCol1");
    typeCol1.addStringValues("tcv1", "tcv2");

    final LdapAttribute typeCol2 = new LdapAttribute();
    typeCol2.setName("typeCol2");
    typeCol2.addStringValues("tcv1", "tcv2");

    final LdapAttribute typeSet1 = new LdapAttribute();
    typeSet1.setName("typeSet1");
    typeSet1.addStringValues("tsv1", "tsv2");

    final LdapAttribute typeSet2 = new LdapAttribute();
    typeSet2.setName("typeSet2");
    typeSet2.addStringValues("tsv1", "tsv2");

    final LdapAttribute typeList1 = new LdapAttribute();
    typeList1.setName("typeList1");
    typeList1.addStringValues("tlv1", "tlv2");

    final LdapAttribute typeList2 = new LdapAttribute();
    typeList2.setName("typeList2");
    typeList2.addStringValues("tlv1", "tlv2");

    final LdapEntry entry = new LdapEntry();
    entry.setDn("cn=String Entry,ou=people,dc=ldaptive,dc=org");
    entry.addAttributes(
      new LdapAttribute("customname1", "customvalue1"),
      new LdapAttribute("customname2", "customvalue1", "customvalue2"),
      new LdapAttribute("type1", "tv1"),
      new LdapAttribute("type2", "tv2"),
      new LdapAttribute("stringthree", "tv3"),
      typeArray1,
      typeArray2,
      col1,
      col2,
      typeCol1,
      typeCol2,
      typeSet1,
      typeSet2,
      typeList1,
      typeList2,
      new LdapAttribute("transcoded", "prefix-transcoded"));

    return entry;
  }


  /** Test class for the default ldap entry mapper. */
  @Entry(
    dn = "cn=String Entry,ou=people,dc=ldaptive,dc=org", attributes = {
      @Attribute(name = "customname1", values = "customvalue1"),
      @Attribute(name = "customname2", values = {"customvalue1", "customvalue2"}),
      @Attribute(name = "type1", property = "type1"),
      @Attribute(name = "type2", property = "type2"),
      @Attribute(name = "stringthree", property = "type3"),
      @Attribute(name = "typeArray1", property = "typeArray1"),
      @Attribute(name = "typeArray2", property = "typeArray2"),
      @Attribute(name = "col1", property = "col1"),
      @Attribute(name = "col2", property = "col2"),
      @Attribute(name = "typeCol1", property = "typeCol1"),
      @Attribute(name = "typeCol2", property = "typeCol2"),
      @Attribute(name = "typeSet1", property = "typeSet1"),
      @Attribute(name = "typeSet2", property = "typeSet2"),
      @Attribute(name = "typeList1", property = "typeList1"),
      @Attribute(name = "typeList2", property = "typeList2"),
      @Attribute(
        name = "transcoded", property = "transcoded", transcoder = "org.ldaptive.beans.reflect.CustomObject$" +
          "PrefixStringValueTranscoder"
      ),
      @Attribute(name = "nullArray1", property = "nullArray1"),
      @Attribute(name = "nullList1", property = "nullList1")})
  public static class Default extends StringCustomObject {}


  /** Test class for the spring ldap entry mapper. */
  @Entry(
    dn = "cn=String Entry,ou=people,dc=ldaptive,dc=org", attributes = {
      @Attribute(name = "customname1", values = "customvalue1"),
      @Attribute(name = "customname2", values = {"customvalue1", "customvalue2"}),
      @Attribute(name = "type1", property = "type1"),
      @Attribute(name = "type2", property = "type2"),
      @Attribute(name = "stringthree", property = "type3"),
      @Attribute(name = "typeArray1", property = "typeArray1"),
      @Attribute(name = "typeArray2", property = "typeArray2"),
      @Attribute(name = "col1", property = "col1"),
      @Attribute(name = "col2", property = "col2"),
      @Attribute(name = "typeCol1", property = "typeCol1"),
      @Attribute(name = "typeCol2", property = "typeCol2"),
      @Attribute(name = "typeSet1", property = "typeSet1"),
      @Attribute(name = "typeSet2", property = "typeSet2"),
      @Attribute(name = "typeList1", property = "typeList1"),
      @Attribute(name = "typeList2", property = "typeList2"),
      @Attribute(
        name = "transcoded", property = "transcoded", transcoder = "new org.ldaptive.beans.reflect.CustomObject$" +
          "PrefixStringValueTranscoder(1)"
      ),
      @Attribute(name = "nullArray1", property = "nullArray1"),
      @Attribute(name = "nullList1", property = "nullList1")})
  public static class Spring extends StringCustomObject
  {
    // CheckStyle:JavadocMethod OFF
    // CheckStyle:LeftCurly OFF
    public String getType2() { return type2; }
    public void setType2(final String t) { type2 = t; }
    public String[] getTypeArray2() { return typeArray2; }
    public void setTypeArray2(final String[] t) { typeArray2 = t; }
    public Collection getCol2() { return col2; }
    public void setCol2(final Collection c) { col2 = c; }
    public Collection<String> getTypeCol2() { return typeCol2; }
    public void setTypeCol2(final Collection<String> c) { typeCol2 = c; }
    public Set<String> getTypeSet2() { return typeSet2; }
    public void setTypeSet2(final Set<String> s) { typeSet2 = s; }
    public List<String> getTypeList2() { return typeList2; }
    public void setTypeList2(final List<String> l) { typeList2 = l; }
    // CheckStyle:LeftCurly ON
    // CheckStyle:JavadocMethod ON
  }
}
