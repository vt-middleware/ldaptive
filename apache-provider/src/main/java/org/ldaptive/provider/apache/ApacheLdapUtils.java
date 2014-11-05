/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.provider.apache;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.apache.directory.api.ldap.model.entry.Attribute;
import org.apache.directory.api.ldap.model.entry.BinaryValue;
import org.apache.directory.api.ldap.model.entry.DefaultAttribute;
import org.apache.directory.api.ldap.model.entry.DefaultEntry;
import org.apache.directory.api.ldap.model.entry.DefaultModification;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.entry.Modification;
import org.apache.directory.api.ldap.model.entry.ModificationOperation;
import org.apache.directory.api.ldap.model.entry.StringValue;
import org.apache.directory.api.ldap.model.entry.Value;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.ldaptive.AttributeModification;
import org.ldaptive.AttributeModificationType;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.ldaptive.SearchEntry;
import org.ldaptive.SortBehavior;
import org.ldaptive.control.ResponseControl;

/**
 * Provides methods for converting between Apache Ldap specific objects and
 * ldaptive specific objects.
 *
 * @author  Middleware Services
 */
public class ApacheLdapUtils
{

  /** Default binary attributes. */
  protected static final String[] DEFAULT_BINARY_ATTRS = new String[] {
    "userPassword",
    "jpegPhoto",
    "userCertificate",
  };

  /** Ldap result sort behavior. */
  private final SortBehavior sortBehavior;

  /** Attributes that should be treated as binary. */
  private List<String> binaryAttrs = Arrays.asList(DEFAULT_BINARY_ATTRS);


  /** Default constructor. */
  public ApacheLdapUtils()
  {
    sortBehavior = SortBehavior.getDefaultSortBehavior();
  }


  /**
   * Creates a new apache ldap util.
   *
   * @param  sb  sort behavior
   */
  public ApacheLdapUtils(final SortBehavior sb)
  {
    sortBehavior = sb;
  }


  /**
   * Returns the list of binary attributes.
   *
   * @return  list of binary attributes
   */
  public List<String> getBinaryAttributes()
  {
    return binaryAttrs;
  }


  /**
   * Sets the list of binary attributes.
   *
   * @param  s  binary attributes
   */
  public void setBinaryAttributes(final String[] s)
  {
    if (s != null) {
      binaryAttrs = Arrays.asList(s);
    }
  }


  /**
   * Returns an apache ldap value for the supplied object.
   *
   * @param  o  object value
   *
   * @return  apache ldap value
   */
  public static Value<?> createValue(final Object o)
  {
    if (o instanceof String) {
      return new StringValue((String) o);
    } else if (o instanceof byte[]) {
      return new BinaryValue((byte[]) o);
    } else {
      throw new IllegalArgumentException(
        "Unsupported attribute value type " + o.getClass());
    }
  }


  /**
   * Returns an apache ldap attribute that represents the values in the supplied
   * ldap attribute.
   *
   * @param  la  ldap attribute
   *
   * @return  apache ldap attribute
   */
  public Attribute fromLdapAttribute(final LdapAttribute la)
  {
    final DefaultAttribute attribute = new DefaultAttribute(la.getName());
    if (la.isBinary()) {
      for (byte[] value : la.getBinaryValues()) {
        attribute.add(createValue(value));
      }
    } else {
      for (String value : la.getStringValues()) {
        attribute.add(createValue(value));
      }
    }
    return attribute;
  }


  /**
   * Returns an ldap attribute using the supplied apache ldap attribute.
   *
   * @param  a  apache ldap attribute
   *
   * @return  ldap attribute
   */
  public LdapAttribute toLdapAttribute(final Attribute a)
  {
    boolean isBinary = false;
    if (a.getId().contains(";binary")) {
      isBinary = true;
    } else if (binaryAttrs != null && binaryAttrs.contains(a.getUpId())) {
      isBinary = true;
    } else if (!a.isHumanReadable() && a.get() != null) {
      isBinary = true;
    }

    final LdapAttribute la = new LdapAttribute(sortBehavior, isBinary);
    la.setName(a.getUpId());
    for (Value<?> v : a) {
      if (isBinary) {
        la.addBinaryValue(v.getBytes());
      } else {
        la.addStringValue(v.getString());
      }
    }
    return la;
  }


  /**
   * Returns a list of apache ldap attribute that represents the values in the
   * supplied ldap attributes.
   *
   * @param  c  ldap attributes
   *
   * @return  apache ldap attributes
   */
  public Attribute[] fromLdapAttributes(final Collection<LdapAttribute> c)
  {
    final List<Attribute> attributes = new ArrayList<>();
    for (LdapAttribute a : c) {
      attributes.add(fromLdapAttribute(a));
    }
    return attributes.toArray(new Attribute[attributes.size()]);
  }


  /**
   * Returns an apache ldap entry that represents the supplied ldap entry.
   *
   * @param  le  ldap entry
   *
   * @return  apache ldap entry
   *
   * @throws  LdapException  if the apache object cannot be created
   */
  public Entry fromLdapEntry(final LdapEntry le)
    throws LdapException
  {
    final DefaultEntry entry = new DefaultEntry(le.getDn());
    entry.add(fromLdapAttributes(le.getAttributes()));
    return entry;
  }


  /**
   * Returns a search entry using the supplied apache ldap entry.
   *
   * @param  e  apache ldap entry
   * @param  c  response controls
   * @param  id  message id
   *
   * @return  search entry
   */
  public SearchEntry toSearchEntry(
    final Entry e,
    final ResponseControl[] c,
    final int id)
  {
    final SearchEntry se = new SearchEntry(id, c, sortBehavior);
    se.setDn(e.getDn().getName());
    for (Attribute a : e) {
      se.addAttribute(toLdapAttribute(a));
    }
    return se;
  }


  /**
   * Returns apache ldap modifications using the supplied attribute
   * modifications.
   *
   * @param  am  attribute modifications
   *
   * @return  apache ldap modifications
   */
  public Modification[] fromAttributeModification(
    final AttributeModification[] am)
  {
    final Modification[] mods = new Modification[am.length];
    for (int i = 0; i < am.length; i++) {
      mods[i] = new DefaultModification(
        getAttributeModification(am[i].getAttributeModificationType()),
        fromLdapAttribute(am[i].getAttribute()));
    }
    return mods;
  }


  /**
   * Returns the apache ldap modification operation for the supplied attribute
   * modification type.
   *
   * @param  am  attribute modification type
   *
   * @return  modification operation
   */
  protected static ModificationOperation getAttributeModification(
    final AttributeModificationType am)
  {
    ModificationOperation op = null;
    if (am == AttributeModificationType.ADD) {
      op = ModificationOperation.ADD_ATTRIBUTE;
    } else if (am == AttributeModificationType.REMOVE) {
      op = ModificationOperation.REMOVE_ATTRIBUTE;
    } else if (am == AttributeModificationType.REPLACE) {
      op = ModificationOperation.REPLACE_ATTRIBUTE;
    }
    return op;
  }
}
