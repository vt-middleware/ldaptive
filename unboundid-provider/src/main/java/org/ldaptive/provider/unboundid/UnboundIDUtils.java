/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.provider.unboundid;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import com.unboundid.ldap.sdk.Attribute;
import com.unboundid.ldap.sdk.Entry;
import com.unboundid.ldap.sdk.Modification;
import com.unboundid.ldap.sdk.ModificationType;
import com.unboundid.ldap.sdk.controls.SortKey;
import org.ldaptive.AttributeModification;
import org.ldaptive.AttributeModificationType;
import org.ldaptive.LdapAttribute;
import org.ldaptive.SearchEntry;
import org.ldaptive.SortBehavior;
import org.ldaptive.control.ResponseControl;

/**
 * Provides methods for converting between Unbound ID specific objects and ldaptive specific objects.
 *
 * @author  Middleware Services
 */
public class UnboundIDUtils
{

  /** Ldap result sort behavior. */
  private final SortBehavior sortBehavior;

  /** Attributes that should be treated as binary. */
  private List<String> binaryAttrs;


  /** Default constructor. */
  public UnboundIDUtils()
  {
    sortBehavior = SortBehavior.getDefaultSortBehavior();
  }


  /**
   * Creates a new unboundid util.
   *
   * @param  sb  sort behavior
   */
  public UnboundIDUtils(final SortBehavior sb)
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
   * Returns an unbound id attribute that represents the values in the supplied ldap attribute.
   *
   * @param  la  ldap attribute
   *
   * @return  unbound id attribute
   */
  public Attribute fromLdapAttribute(final LdapAttribute la)
  {
    Attribute attribute;
    if (la.isBinary()) {
      attribute = new Attribute(la.getName(), la.getBinaryValues().toArray(new byte[la.size()][]));
    } else {
      attribute = new Attribute(la.getName(), la.getStringValues());
    }
    return attribute;
  }


  /**
   * Returns an ldap attribute using the supplied unbound id attribute.
   *
   * @param  a  unbound id attribute
   *
   * @return  ldap attribute
   */
  public LdapAttribute toLdapAttribute(final Attribute a)
  {
    boolean isBinary = false;
    if (a.getOptions().contains("binary")) {
      isBinary = true;
    } else if (binaryAttrs != null && binaryAttrs.contains(a.getName())) {
      isBinary = true;
    }

    final LdapAttribute la = new LdapAttribute(sortBehavior, isBinary);
    la.setName(a.getName());
    if (isBinary) {
      la.addBinaryValue(a.getValueByteArrays());
    } else {
      la.addStringValue(a.getValues());
    }
    return la;
  }


  /**
   * Returns a list of unbound id attribute that represents the values in the supplied ldap attributes.
   *
   * @param  c  ldap attributes
   *
   * @return  unbound id attributes
   */
  public Attribute[] fromLdapAttributes(final Collection<LdapAttribute> c)
  {
    return c.stream().map(this::fromLdapAttribute).toArray(Attribute[]::new);
  }


  /**
   * Returns a search entry using the supplied unbound id entry.
   *
   * @param  e  unbound id entry
   * @param  c  response controls
   * @param  id  message id
   *
   * @return  search entry
   */
  public SearchEntry toSearchEntry(final Entry e, final ResponseControl[] c, final int id)
  {
    final SearchEntry se = new SearchEntry(id, c, sortBehavior);
    se.setDn(e.getDN());
    for (Attribute a : e.getAttributes()) {
      se.addAttribute(toLdapAttribute(a));
    }
    return se;
  }


  /**
   * Returns unbound id modifications using the supplied attribute modifications.
   *
   * @param  am  attribute modifications
   *
   * @return  unbound id modifications
   */
  public Modification[] fromAttributeModification(final AttributeModification[] am)
  {
    final Modification[] mods = new Modification[am.length];
    for (int i = 0; i < am.length; i++) {
      final Attribute a = fromLdapAttribute(am[i].getAttribute());
      if (am[i].getAttribute().isBinary()) {
        mods[i] = new Modification(
          getModificationType(am[i].getAttributeModificationType()),
          a.getName(),
          a.getValueByteArrays());
      } else {
        mods[i] = new Modification(
          getModificationType(am[i].getAttributeModificationType()),
          a.getName(),
          a.getValues());
      }
    }
    return mods;
  }


  /**
   * Returns unbound id sort keys using the supplied sort keys.
   *
   * @param  sk  sort keys
   *
   * @return  unbound id sort keys
   */
  public static SortKey[] fromSortKey(final org.ldaptive.control.SortKey[] sk)
  {
    SortKey[] keys = null;
    if (sk != null) {
      keys = new SortKey[sk.length];
      for (int i = 0; i < sk.length; i++) {
        keys[i] = new SortKey(sk[i].getAttributeDescription(), sk[i].getMatchingRuleId(), sk[i].getReverseOrder());
      }
    }
    return keys;
  }


  /**
   * Returns the unbound id modification type for the supplied attribute modification type.
   *
   * @param  am  attribute modification type
   *
   * @return  modification type
   */
  protected static ModificationType getModificationType(final AttributeModificationType am)
  {
    ModificationType type = null;
    if (am == AttributeModificationType.ADD) {
      type = ModificationType.ADD;
    } else if (am == AttributeModificationType.REMOVE) {
      type = ModificationType.DELETE;
    } else if (am == AttributeModificationType.REPLACE) {
      type = ModificationType.REPLACE;
    }
    return type;
  }
}
