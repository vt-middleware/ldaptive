/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.provider.jldap;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import com.novell.ldap.LDAPAttribute;
import com.novell.ldap.LDAPAttributeSet;
import com.novell.ldap.LDAPEntry;
import com.novell.ldap.LDAPModification;
import com.novell.ldap.controls.LDAPSortKey;
import org.ldaptive.AttributeModification;
import org.ldaptive.AttributeModificationType;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.ldaptive.SearchEntry;
import org.ldaptive.SortBehavior;
import org.ldaptive.control.ResponseControl;
import org.ldaptive.control.SortKey;

/**
 * Provides methods for converting between JLDAP specific objects and ldaptive specific objects.
 *
 * @author  Middleware Services
 */
public class JLdapUtils
{

  /** Ldap result sort behavior. */
  private final SortBehavior sortBehavior;

  /** Attributes that should be treated as binary. */
  private List<String> binaryAttrs;


  /** Default constructor. */
  public JLdapUtils()
  {
    sortBehavior = SortBehavior.getDefaultSortBehavior();
  }


  /**
   * Creates a new jldap util.
   *
   * @param  sb  sort behavior
   */
  public JLdapUtils(final SortBehavior sb)
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
   * Returns a jldap attribute that represents the values in the supplied ldap attribute.
   *
   * @param  la  ldap attribute
   *
   * @return  jldap attribute
   */
  public LDAPAttribute fromLdapAttribute(final LdapAttribute la)
  {
    final LDAPAttribute attr = new LDAPAttribute(la.getName());
    if (la.isBinary()) {
      la.getBinaryValues().forEach(attr::addValue);
    } else {
      la.getStringValues().forEach(attr::addValue);
    }
    return attr;
  }


  /**
   * Returns an ldap attribute using the supplied jldap attribute.
   *
   * @param  a  jldap attribute
   *
   * @return  ldap attribute
   */
  public LdapAttribute toLdapAttribute(final LDAPAttribute a)
  {
    boolean isBinary = false;
    if (a.getName().contains(";binary")) {
      isBinary = true;
    } else if (binaryAttrs != null && binaryAttrs.contains(a.getName())) {
      isBinary = true;
    }

    final LdapAttribute la = new LdapAttribute(sortBehavior, isBinary);
    la.setName(a.getName());
    if (isBinary) {
      for (byte[] b : a.getByteValueArray()) {
        la.addBinaryValue(b);
      }
    } else {
      for (String s : a.getStringValueArray()) {
        la.addStringValue(s);
      }
    }
    return la;
  }


  /**
   * Returns a jldap attribute set that represents the values in the supplied ldap attributes.
   *
   * @param  c  ldap attributes
   *
   * @return  jldap attributes
   */
  public LDAPAttributeSet fromLdapAttributes(final Collection<LdapAttribute> c)
  {
    final LDAPAttributeSet attrs = new LDAPAttributeSet();
    for (LdapAttribute la : c) {
      attrs.add(fromLdapAttribute(la));
    }
    return attrs;
  }


  /**
   * Returns a jldap ldap entry that represents the supplied ldap entry.
   *
   * @param  le  ldap entry
   *
   * @return  jldap ldap entry
   */
  public LDAPEntry fromLdapEntry(final LdapEntry le)
  {
    return new LDAPEntry(le.getDn(), fromLdapAttributes(le.getAttributes()));
  }


  /**
   * Returns a search entry using the supplied jldap ldap entry.
   *
   * @param  entry  jldap ldap entry
   * @param  c  response controls
   * @param  id  message id
   *
   * @return  search entry
   */
  @SuppressWarnings("unchecked")
  public SearchEntry toSearchEntry(final LDAPEntry entry, final ResponseControl[] c, final int id)
  {
    final SearchEntry se = new SearchEntry(id, c, sortBehavior);
    se.setDn(entry.getDN());

    for (Object o : entry.getAttributeSet()) {
      se.addAttribute(toLdapAttribute((LDAPAttribute) o));
    }
    return se;
  }


  /**
   * Returns jldap ldap modifications using the supplied attribute modifications.
   *
   * @param  am  attribute modifications
   *
   * @return  jldap ldap modifications
   */
  public LDAPModification[] fromAttributeModification(final AttributeModification[] am)
  {
    final LDAPModification[] mods = new LDAPModification[am.length];
    for (int i = 0; i < am.length; i++) {
      mods[i] = new LDAPModification(
        getAttributeModification(am[i].getAttributeModificationType()),
        fromLdapAttribute(am[i].getAttribute()));
    }
    return mods;
  }


  /**
   * Returns jldap sort keys using the supplied sort keys.
   *
   * @param  sk  sort keys
   *
   * @return  jldap sort keys
   */
  public static LDAPSortKey[] fromSortKey(final SortKey[] sk)
  {
    LDAPSortKey[] keys = null;
    if (sk != null) {
      keys = new LDAPSortKey[sk.length];
      for (int i = 0; i < sk.length; i++) {
        keys[i] = new LDAPSortKey(sk[i].getAttributeDescription(), sk[i].getReverseOrder(), sk[i].getMatchingRuleId());
      }
    }
    return keys;
  }


  /**
   * Returns the jldap modification integer constant for the supplied attribute modification type.
   *
   * @param  am  attribute modification type
   *
   * @return  integer constant
   */
  protected static int getAttributeModification(final AttributeModificationType am)
  {
    int op = -1;
    if (am == AttributeModificationType.ADD) {
      op = LDAPModification.ADD;
    } else if (am == AttributeModificationType.REMOVE) {
      op = LDAPModification.DELETE;
    } else if (am == AttributeModificationType.REPLACE) {
      op = LDAPModification.REPLACE;
    }
    return op;
  }
}
