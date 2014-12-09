/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.schema;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.ldaptive.LdapUtils;

/**
 * Bean that contains the schema definitions in RFC 4512.
 *
 * @author  Middleware Services
 */
public class Schema
{

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 1181;

  /** Attribute types. */
  private Collection<AttributeType> attributeTypes = Collections.emptySet();

  /** DIT content rules. */
  private Collection<DITContentRule> ditContentRules = Collections.emptySet();

  /** DIT structure rules. */
  private Collection<DITStructureRule> ditStructureRules =
    Collections.emptySet();

  /** Syntaxes. */
  private Collection<Syntax> syntaxes = Collections.emptySet();

  /** Matching rules. */
  private Collection<MatchingRule> matchingRules = Collections.emptySet();

  /** Matching rule uses. */
  private Collection<MatchingRuleUse> matchingRuleUses = Collections.emptySet();

  /** Name forms. */
  private Collection<NameForm> nameForms = Collections.emptySet();

  /** Object classes. */
  private Collection<ObjectClass> objectClasses = Collections.emptySet();


  /** Default constructor. */
  public Schema() {}


  /**
   * Creates a new schema.
   *
   * @param  attributeTypes  attribute types
   * @param  ditContentRules  DIT content rules
   * @param  ditStructureRules  DIT structure rules
   * @param  syntaxes  syntaxes
   * @param  matchingRules  matching rules
   * @param  matchingRuleUses  matching rule uses
   * @param  nameForms  name forms
   * @param  objectClasses  object classses
   */
  // CheckStyle:ParameterNumber|HiddenField OFF
  public Schema(
    final Collection<AttributeType> attributeTypes,
    final Collection<DITContentRule> ditContentRules,
    final Collection<DITStructureRule> ditStructureRules,
    final Collection<Syntax> syntaxes,
    final Collection<MatchingRule> matchingRules,
    final Collection<MatchingRuleUse> matchingRuleUses,
    final Collection<NameForm> nameForms,
    final Collection<ObjectClass> objectClasses)
  {
    setAttributeTypes(attributeTypes);
    setDitContentRules(ditContentRules);
    setDitStructureRules(ditStructureRules);
    setSyntaxes(syntaxes);
    setMatchingRules(matchingRules);
    setMatchingRuleUses(matchingRuleUses);
    setNameForms(nameForms);
    setObjectClasses(objectClasses);
  }
  // CheckStyle:ParameterNumber|HiddenField ON


  /**
   * Returns the attribute types.
   *
   * @return  attribute types
   */
  public Collection<AttributeType> getAttributeTypes()
  {
    return attributeTypes;
  }


  /**
   * Returns the attribute type with the supplied OID or name.
   *
   * @param  name  OID or name
   *
   * @return  attribute type or null if name does not exist
   */
  public AttributeType getAttributeType(final String name)
  {
    for (AttributeType at : attributeTypes) {
      if (at.getOID().equals(name) || at.hasName(name)) {
        return at;
      }
    }
    return null;
  }


  /**
   * Sets the attribute types.
   *
   * @param  c  attribute types
   */
  public void setAttributeTypes(final Collection<AttributeType> c)
  {
    attributeTypes = c;
  }


  /**
   * Returns the attribute names in this schema that represent binary data. This
   * includes attributes with a syntax OID of '1.3.6.1.4.1.1466.115.121.1.5' and
   * any syntax with the 'X-NOT-HUMAN-READABLE' extension.
   *
   * @return  binary attribute names
   */
  public String[] getBinaryAttributeNames()
  {
    final List<String> binaryAttrs = new ArrayList<>();
    for (AttributeType type : attributeTypes) {
      boolean isBinary = false;
      final String syntaxOid = type.getSyntaxOID(false);
      if ("1.3.6.1.4.1.1466.115.121.1.5".equals(syntaxOid)) {
        isBinary = true;
      } else {
        final Syntax syntax = getSyntax(syntaxOid);
        if (syntax != null &&
            Syntax.containsBooleanExtension(syntax, "X-NOT-HUMAN-READABLE")) {
          isBinary = true;
        }
      }
      if (isBinary) {
        Collections.addAll(binaryAttrs, type.getNames());
      }
    }
    return binaryAttrs.toArray(new String[binaryAttrs.size()]);
  }


  /**
   * Returns the DIT content rules.
   *
   * @return  DIT content rules
   */
  public Collection<DITContentRule> getDitContentRules()
  {
    return ditContentRules;
  }


  /**
   * Returns the DIT content rule with the supplied OID or name.
   *
   * @param  name  OID or name
   *
   * @return  DIT content rule or null if name does not exist
   */
  public DITContentRule getDITContentRule(final String name)
  {
    for (DITContentRule rule : ditContentRules) {
      if (rule.getOID().equals(name) || rule.hasName(name)) {
        return rule;
      }
    }
    return null;
  }


  /**
   * Sets the DIT content rules.
   *
   * @param  c  DIT content rules
   */
  public void setDitContentRules(final Collection<DITContentRule> c)
  {
    ditContentRules = c;
  }


  /**
   * Returns the DIT structure rules.
   *
   * @return  DIT structure rules
   */
  public Collection<DITStructureRule> getDitStructureRules()
  {
    return ditStructureRules;
  }


  /**
   * Returns the DIT structure rule with the supplied ID.
   *
   * @param  id  rule ID
   *
   * @return  DIT structure rule or null if id does not exist
   */
  public DITStructureRule getDITStructureRule(final int id)
  {
    for (DITStructureRule rule : ditStructureRules) {
      if (rule.getID() == id) {
        return rule;
      }
    }
    return null;
  }


  /**
   * Returns the DIT structure rule with the supplied name.
   *
   * @param  name  rule name
   *
   * @return  DIT structure rule or null if name does not exist
   */
  public DITStructureRule getDITStructureRule(final String name)
  {
    for (DITStructureRule rule : ditStructureRules) {
      if (rule.hasName(name)) {
        return rule;
      }
    }
    return null;
  }


  /**
   * Sets the DIT structure rules.
   *
   * @param  c  DIT structure rules
   */
  public void setDitStructureRules(final Collection<DITStructureRule> c)
  {
    ditStructureRules = c;
  }


  /**
   * Returns the syntaxes.
   *
   * @return  syntaxes
   */
  public Collection<Syntax> getSyntaxes()
  {
    return syntaxes;
  }


  /**
   * Returns the syntax with the supplied OID.
   *
   * @param  oid  OID
   *
   * @return  syntax or null if OID does not exist
   */
  public Syntax getSyntax(final String oid)
  {
    for (Syntax syntax : syntaxes) {
      if (syntax.getOID().equals(oid)) {
        return syntax;
      }
    }
    return null;
  }


  /**
   * Sets the syntaxes.
   *
   * @param  c  syntaxes
   */
  public void setSyntaxes(final Collection<Syntax> c)
  {
    syntaxes = c;
  }


  /**
   * Returns the matching rules.
   *
   * @return  matching rules
   */
  public Collection<MatchingRule> getMatchingRules()
  {
    return matchingRules;
  }


  /**
   * Returns the matching rule with the supplied OID or name.
   *
   * @param  name  OID or name
   *
   * @return  matching rule or null if name does not exist
   */
  public MatchingRule getMatchingRule(final String name)
  {
    for (MatchingRule rule : matchingRules) {
      if (rule.getOID().equals(name) || rule.hasName(name)) {
        return rule;
      }
    }
    return null;
  }


  /**
   * Sets the matching rules.
   *
   * @param  c  matching rules
   */
  public void setMatchingRules(final Collection<MatchingRule> c)
  {
    matchingRules = c;
  }


  /**
   * Returns the matching rule uses.
   *
   * @return  matching rule ueses
   */
  public Collection<MatchingRuleUse> getMatchingRuleUses()
  {
    return matchingRuleUses;
  }


  /**
   * Returns the matching rule use with the supplied OID or name.
   *
   * @param  name  OID or name
   *
   * @return  matching rule use or null if name does not exist
   */
  public MatchingRuleUse getMatchingRuleUse(final String name)
  {
    for (MatchingRuleUse rule : matchingRuleUses) {
      if (rule.getOID().equals(name) || rule.hasName(name)) {
        return rule;
      }
    }
    return null;
  }


  /**
   * Sets the matching rule uses.
   *
   * @param  c  matching rule uses
   */
  public void setMatchingRuleUses(final Collection<MatchingRuleUse> c)
  {
    matchingRuleUses = c;
  }


  /**
   * Returns the name forms.
   *
   * @return  name forms
   */
  public Collection<NameForm> getNameForms()
  {
    return nameForms;
  }


  /**
   * Returns the name form with the supplied OID or name.
   *
   * @param  name  OID or name
   *
   * @return  name form or null if name does not exist
   */
  public NameForm getNameForm(final String name)
  {
    for (NameForm form : nameForms) {
      if (form.getOID().equals(name) || form.hasName(name)) {
        return form;
      }
    }
    return null;
  }


  /**
   * Sets the name forms.
   *
   * @param  c  name forms
   */
  public void setNameForms(final Collection<NameForm> c)
  {
    nameForms = c;
  }


  /**
   * Returns the object classes.
   *
   * @return  object classes
   */
  public Collection<ObjectClass> getObjectClasses()
  {
    return objectClasses;
  }


  /**
   * Returns the object class with the supplied OID or name.
   *
   * @param  name  OID or name
   *
   * @return  object class or null if name does not exist
   */
  public ObjectClass getObjectClass(final String name)
  {
    for (ObjectClass oc : objectClasses) {
      if (oc.getOID().equals(name) || oc.hasName(name)) {
        return oc;
      }
    }
    return null;
  }


  /**
   * Sets the object classes.
   *
   * @param  c  object classes
   */
  public void setObjectClasses(final Collection<ObjectClass> c)
  {
    objectClasses = c;
  }


  @Override
  public int hashCode()
  {
    return
      LdapUtils.computeHashCode(
        HASH_CODE_SEED,
        attributeTypes,
        ditContentRules,
        ditStructureRules,
        syntaxes,
        matchingRules,
        matchingRuleUses,
        nameForms,
        objectClasses);
  }


  @Override
  public boolean equals(final Object o)
  {
    return LdapUtils.areEqual(this, o);
  }


  @Override
  public String toString()
  {
    return
      String.format(
        "[%s@%d::attributeTypes=%s, ditContentRules=%s, " +
        "ditStructureRules=%s, syntaxes=%s, matchingRules=%s, " +
        "matchingRuleUses=%s, nameForms=%s, objectClasses=%s]",
        getClass().getName(),
        hashCode(),
        attributeTypes,
        ditContentRules,
        ditStructureRules,
        syntaxes,
        matchingRules,
        matchingRuleUses,
        nameForms,
        objectClasses);
  }
}
