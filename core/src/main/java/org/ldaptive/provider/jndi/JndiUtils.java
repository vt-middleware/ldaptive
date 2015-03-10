/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.provider.jndi;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapContext;
import org.ldaptive.AttributeModification;
import org.ldaptive.AttributeModificationType;
import org.ldaptive.LdapAttribute;
import org.ldaptive.SearchEntry;
import org.ldaptive.SortBehavior;
import org.ldaptive.control.SortKey;
import org.ldaptive.sasl.Mechanism;
import org.ldaptive.sasl.QualityOfProtection;
import org.ldaptive.sasl.SecurityStrength;

/**
 * Provides methods for converting between JNDI specific objects and ldaptive specific objects.
 *
 * @author  Middleware Services
 */
public class JndiUtils
{

  /** Whether to ignore case when creating basic attributes. */
  public static final boolean DEFAULT_IGNORE_CASE = true;

  /** Ldap result sort behavior. */
  private final SortBehavior sortBehavior;


  /** Default constructor. */
  public JndiUtils()
  {
    sortBehavior = SortBehavior.getDefaultSortBehavior();
  }


  /**
   * Creates a new jndi util.
   *
   * @param  sb  sort behavior
   */
  public JndiUtils(final SortBehavior sb)
  {
    sortBehavior = sb;
  }


  /**
   * Returns a jndi attribute that represents the values in the supplied ldap attribute.
   *
   * @param  attr  ldap attribute
   *
   * @return  jndi attribute
   */
  public Attribute fromLdapAttribute(final LdapAttribute attr)
  {
    final Attribute attribute = new BasicAttribute(attr.getName());
    if (attr.isBinary()) {
      for (byte[] value : attr.getBinaryValues()) {
        attribute.add(value);
      }
    } else {
      for (String value : attr.getStringValues()) {
        attribute.add(value);
      }
    }
    return attribute;
  }


  /**
   * Returns an ldap attribute using the supplied jndi attribute.
   *
   * @param  attr  jndi attribute
   *
   * @return  ldap attribute
   *
   * @throws  NamingException  if the attribute values cannot be read
   */
  public LdapAttribute toLdapAttribute(final Attribute attr)
    throws NamingException
  {
    final Set<Object> values = new HashSet<>();
    final NamingEnumeration<?> ne = attr.getAll();
    while (ne.hasMore()) {
      values.add(ne.next());
    }
    return LdapAttribute.createLdapAttribute(sortBehavior, attr.getID(), values);
  }


  /**
   * Returns a jndi attributes that represents the values in the supplied ldap attributes.
   *
   * @param  attrs  ldap attributes
   *
   * @return  jndi attributes
   */
  public Attributes fromLdapAttributes(final Collection<LdapAttribute> attrs)
  {
    final Attributes attributes = new BasicAttributes(DEFAULT_IGNORE_CASE);
    for (LdapAttribute a : attrs) {
      attributes.put(fromLdapAttribute(a));
    }
    return attributes;
  }


  /**
   * Returns a jndi search result that represents the supplied search entry.
   *
   * @param  entry  search entry
   *
   * @return  jndi search result
   */
  public SearchResult fromSearchEntry(final SearchEntry entry)
  {
    return new SearchResult(entry.getDn(), null, fromLdapAttributes(entry.getAttributes()));
  }


  /**
   * Returns a search entry using the supplied jndi search result.
   *
   * @param  result  jndi search result
   *
   * @return  search entry
   *
   * @throws  NamingException  if the search result cannot be read
   */
  public SearchEntry toSearchEntry(final SearchResult result)
    throws NamingException
  {
    final SearchEntry se = new SearchEntry(-1, null, sortBehavior);
    se.setDn(result.getName());

    final Attributes a = result.getAttributes();
    final NamingEnumeration<? extends Attribute> ne = a.getAll();
    while (ne.hasMore()) {
      se.addAttribute(toLdapAttribute(ne.next()));
    }
    return se;
  }


  /**
   * Returns jndi modification items using the supplied attribute modifications.
   *
   * @param  mods  attribute modifications
   *
   * @return  jndi modification items
   */
  public ModificationItem[] fromAttributeModification(final AttributeModification[] mods)
  {
    final ModificationItem[] mi = new ModificationItem[mods.length];
    for (int i = 0; i < mods.length; i++) {
      mi[i] = new ModificationItem(
        getAttributeModification(mods[i].getAttributeModificationType()),
        fromLdapAttribute(mods[i].getAttribute()));
    }
    return mi;
  }


  /**
   * Returns jndi sort keys using the supplied sort keys.
   *
   * @param  keys  sort keys
   *
   * @return  jndi sort keys
   */
  public static javax.naming.ldap.SortKey[] fromSortKey(final SortKey[] keys)
  {
    javax.naming.ldap.SortKey[] sk = null;
    if (keys != null) {
      sk = new javax.naming.ldap.SortKey[keys.length];
      for (int i = 0; i < keys.length; i++) {
        sk[i] = new javax.naming.ldap.SortKey(
          keys[i].getAttributeDescription(),
          !keys[i].getReverseOrder(),
          keys[i].getMatchingRuleId());
      }
    }
    return sk;
  }


  /**
   * Returns the jndi modification integer constant for the supplied attribute modification type.
   *
   * @param  type  attribute modification type
   *
   * @return  integer constant
   */
  protected static int getAttributeModification(final AttributeModificationType type)
  {
    int op = -1;
    if (type == AttributeModificationType.ADD) {
      op = LdapContext.ADD_ATTRIBUTE;
    } else if (type == AttributeModificationType.REMOVE) {
      op = LdapContext.REMOVE_ATTRIBUTE;
    } else if (type == AttributeModificationType.REPLACE) {
      op = LdapContext.REPLACE_ATTRIBUTE;
    }
    return op;
  }


  /**
   * Returns the SASL quality of protection string for the supplied enum.
   *
   * @param  qop  quality of protection enum
   *
   * @return  SASL quality of protection string
   */
  public static String getQualityOfProtection(final QualityOfProtection qop)
  {
    String s;
    switch (qop) {

    case AUTH:
      s = "auth";
      break;

    case AUTH_INT:
      s = "auth-int";
      break;

    case AUTH_CONF:
      s = "auth-conf";
      break;

    default:
      throw new IllegalArgumentException("Unknown SASL quality of protection: " + qop);
    }
    return s;
  }


  /**
   * Returns the SASL security strength string for the supplied enum.
   *
   * @param  ss  security strength enum
   *
   * @return  SASL security strength string
   */
  public static String getSecurityStrength(final SecurityStrength ss)
  {
    String s;
    switch (ss) {

    case HIGH:
      s = "high";
      break;

    case MEDIUM:
      s = "medium";
      break;

    case LOW:
      s = "low";
      break;

    default:
      throw new IllegalArgumentException("Unknown SASL security strength: " + ss);
    }
    return s;
  }


  /**
   * Returns the JNDI authentication string for the supplied authentication type.
   *
   * @param  m  sasl mechanism
   *
   * @return  JNDI authentication string
   */
  public static String getAuthenticationType(final Mechanism m)
  {
    String s;
    switch (m) {

    case EXTERNAL:
      s = "EXTERNAL";
      break;

    case DIGEST_MD5:
      s = "DIGEST-MD5";
      break;

    case CRAM_MD5:
      s = "CRAM-MD5";
      break;

    case GSSAPI:
      s = "GSSAPI";
      break;

    default:
      throw new IllegalArgumentException("Unknown SASL authentication mechanism: " + m);
    }
    return s;
  }
}
