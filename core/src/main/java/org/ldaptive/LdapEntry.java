/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Simple bean representing an ldap entry. Contains a DN and ldap attributes.
 *
 * @author  Middleware Services
 */
public class LdapEntry extends AbstractLdapBean
{

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 331;

  /** serial version uid. */
  private static final long serialVersionUID = 7819007625501406463L;

  /** Distinguished name for this entry. */
  private String entryDn;

  /** Attributes contained in this bean. */
  private final Map<String, LdapAttribute> entryAttributes;


  /** Default constructor. */
  public LdapEntry()
  {
    this(SortBehavior.getDefaultSortBehavior());
  }


  /**
   * Creates a new ldap entry.
   *
   * @param  sb  sort behavior
   */
  public LdapEntry(final SortBehavior sb)
  {
    super(sb);
    if (SortBehavior.UNORDERED == sb) {
      entryAttributes = new HashMap<>();
    } else if (SortBehavior.ORDERED == sb) {
      entryAttributes = new LinkedHashMap<>();
    } else if (SortBehavior.SORTED == sb) {
      entryAttributes = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    } else {
      throw new IllegalArgumentException("Unknown sort behavior: " + sb);
    }
  }


  /**
   * Creates a new ldap entry.
   *
   * @param  dn  dn for this entry
   */
  public LdapEntry(final String dn)
  {
    this();
    setDn(dn);
  }


  /**
   * Creates a new ldap entry.
   *
   * @param  dn  dn for this entry
   * @param  attr  ldap attribute for this entry
   */
  public LdapEntry(final String dn, final LdapAttribute... attr)
  {
    this();
    setDn(dn);
    for (LdapAttribute a : attr) {
      addAttribute(a);
    }
  }


  /**
   * Creates a new ldap entry.
   *
   * @param  dn  dn for this entry
   * @param  attrs  collection of attributes to add
   */
  public LdapEntry(final String dn, final Collection<LdapAttribute> attrs)
  {
    this();
    setDn(dn);
    addAttributes(attrs);
  }


  /**
   * Returns the DN.
   *
   * @return  entry DN
   */
  public String getDn()
  {
    return entryDn;
  }


  /**
   * Sets the DN.
   *
   * @param  dn  dn to set
   */
  public void setDn(final String dn)
  {
    entryDn = dn;
  }


  /**
   * Returns a collection of ldap attribute.
   *
   * @return  collection of ldap attribute
   */
  public Collection<LdapAttribute> getAttributes()
  {
    return entryAttributes.values();
  }


  /**
   * Returns a single attribute of this attributes. If multiple attributes exist the first attribute returned by the
   * underlying iterator is used. If no attributes exist null is returned.
   *
   * @return  single attribute
   */
  public LdapAttribute getAttribute()
  {
    if (entryAttributes.isEmpty()) {
      return null;
    }
    return entryAttributes.values().iterator().next();
  }


  /**
   * Returns the attribute with the supplied name.
   *
   * @param  name  of the attribute to return
   *
   * @return  ldap attribute
   */
  public LdapAttribute getAttribute(final String name)
  {
    if (name != null) {
      return entryAttributes.get(name.toLowerCase());
    }
    return null;
  }


  /**
   * Returns the attribute names in this entry.
   *
   * @return  string array of attribute names
   */
  public String[] getAttributeNames()
  {
    final String[] names = new String[entryAttributes.size()];
    int i = 0;
    for (LdapAttribute la : entryAttributes.values()) {
      names[i++] = la.getName();
    }
    return names;
  }


  /**
   * Adds an attribute to this ldap attributes.
   *
   * @param  attr  attribute to add
   */
  public void addAttribute(final LdapAttribute... attr)
  {
    for (LdapAttribute a : attr) {
      entryAttributes.put(a.getName().toLowerCase(), a);
    }
  }


  /**
   * Adds attribute(s) to this ldap attributes.
   *
   * @param  attrs  collection of attributes to add
   */
  public void addAttributes(final Collection<LdapAttribute> attrs)
  {
    attrs.forEach(this::addAttribute);
  }


  /**
   * Removes an attribute from this ldap attributes.
   *
   * @param  attr  attribute to remove
   */
  public void removeAttribute(final LdapAttribute... attr)
  {
    for (LdapAttribute a : attr) {
      entryAttributes.remove(a.getName().toLowerCase());
    }
  }


  /**
   * Removes the attribute of the supplied name from this ldap attributes.
   *
   * @param  name  of attribute to remove
   */
  public void removeAttribute(final String name)
  {
    entryAttributes.remove(name.toLowerCase());
  }


  /**
   * Removes the attribute(s) from this ldap attributes.
   *
   * @param  attrs  collection of ldap attributes to remove
   */
  public void removeAttributes(final Collection<LdapAttribute> attrs)
  {
    attrs.forEach(this::removeAttribute);
  }


  /**
   * Changes the name of an attribute in this entry. The old attribute is removed from this entry, the name is changed
   * with {@link LdapAttribute#setName(String)}, and the attribute is added back to this entry. If oldName does not
   * exist, this method does nothing.
   *
   * @param  oldName  attribute name to change from
   * @param  newName  attribute name to change to
   */
  public void renameAttribute(final String oldName, final String newName)
  {
    final LdapAttribute la = getAttribute(oldName);
    if (la != null) {
      removeAttribute(oldName);
      la.setName(newName);
      addAttribute(la);
    }
  }


  /**
   * Returns the number of attributes in this ldap attributes.
   *
   * @return  number of attributes in this ldap attributes
   */
  public int size()
  {
    return entryAttributes.size();
  }


  /** Removes all the attributes in this ldap attributes. */
  public void clear()
  {
    entryAttributes.clear();
  }


  @Override
  public int hashCode()
  {
    return
      LdapUtils.computeHashCode(
        HASH_CODE_SEED,
        entryDn != null ? entryDn.toLowerCase() : null,
        entryAttributes.values());
  }


  @Override
  public String toString()
  {
    return String.format("[dn=%s%s]", entryDn, entryAttributes.values());
  }


  /**
   * Returns the list of attribute modifications needed to change the supplied target entry into the supplied source
   * entry.
   *
   * @param  source  ldap entry containing new data
   * @param  target  ldap entry containing existing data
   *
   * @return  attribute modifications needed to change target into source or an empty array
   */
  public static AttributeModification[] computeModifications(final LdapEntry source, final LdapEntry target)
  {
    final List<AttributeModification> mods = new ArrayList<>();
    for (LdapAttribute sourceAttr : source.getAttributes()) {
      final LdapAttribute targetAttr = target.getAttribute(sourceAttr.getName());
      if (targetAttr == null) {
        final AttributeModification mod = new AttributeModification(AttributeModificationType.ADD, sourceAttr);
        mods.add(mod);
      } else if (!targetAttr.equals(sourceAttr)) {
        final AttributeModification mod = new AttributeModification(AttributeModificationType.REPLACE, sourceAttr);
        mods.add(mod);
      }
    }
    for (LdapAttribute targetAttr : target.getAttributes()) {
      final LdapAttribute sourceAttr = source.getAttribute(targetAttr.getName());
      if (sourceAttr == null) {
        final AttributeModification mod = new AttributeModification(AttributeModificationType.REMOVE, targetAttr);
        mods.add(mod);
      }
    }
    return mods.toArray(new AttributeModification[mods.size()]);
  }
}
