/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.schema;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.ldaptive.AbstractFreezable;
import org.ldaptive.LdapUtils;

/**
 * Bean that contains the schema definitions in RFC 4512.
 *
 * @author  Middleware Services
 */
public final class Schema extends AbstractFreezable
{

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 1181;

  /** Binary syntax oid. */
  private static final String BINARY_SYNTAX = "1.3.6.1.4.1.1466.115.121.1.5";

  /** Attribute types. */
  private final SchemaElementRegistry<String, AttributeType> attributeTypes = new SchemaElementRegistry<>();

  /** DIT content rules. */
  private final SchemaElementRegistry<String, DITContentRule> ditContentRules = new SchemaElementRegistry<>();

  /** DIT structure rules. */
  private final SchemaElementRegistry<Integer, DITStructureRule> ditStructureRules = new SchemaElementRegistry<>();

  /** Syntaxes. */
  private final SchemaElementRegistry<String, Syntax> syntaxes = new SchemaElementRegistry<>();

  /** Matching rules. */
  private final SchemaElementRegistry<String, MatchingRule> matchingRules = new SchemaElementRegistry<>();

  /** Matching rule uses. */
  private final SchemaElementRegistry<String, MatchingRuleUse> matchingRuleUses = new SchemaElementRegistry<>();

  /** Name forms. */
  private final SchemaElementRegistry<String, NameForm> nameForms = new SchemaElementRegistry<>();

  /** Object classes. */
  private final SchemaElementRegistry<String, ObjectClass> objectClasses = new SchemaElementRegistry<>();


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
   * @param  objectClasses  object classes
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
    setDITContentRules(ditContentRules);
    setDITStructureRules(ditStructureRules);
    setSyntaxes(syntaxes);
    setMatchingRules(matchingRules);
    setMatchingRuleUses(matchingRuleUses);
    setNameForms(nameForms);
    setObjectClasses(objectClasses);
  }
  // CheckStyle:ParameterNumber|HiddenField ON


  @Override
  public void freeze()
  {
    super.freeze();
    attributeTypes.freeze();
    ditContentRules.freeze();
    ditStructureRules.freeze();
    syntaxes.freeze();
    matchingRules.freeze();
    matchingRuleUses.freeze();
    nameForms.freeze();
    objectClasses.freeze();
  }


  /**
   * Returns the attribute types.
   *
   * @return  attribute types
   */
  public Collection<AttributeType> getAttributeTypes()
  {
    return attributeTypes.getElements();
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
    final AttributeType attributeType = attributeTypes.getElementByKey(name);
    if (attributeType == null) {
      return attributeTypes.getElementByName(name);
    }
    return attributeType;
  }


  /**
   * Sets the attribute types.
   *
   * @param  c  attribute types
   */
  public void setAttributeTypes(final Collection<AttributeType> c)
  {
    assertMutable();
    attributeTypes.setElements(c);
  }


  /**
   * Returns the attribute names in this schema that represent binary data. This includes attributes with a syntax OID
   * of '1.3.6.1.4.1.1466.115.121.1.5' and any syntax with the 'X-NOT-HUMAN-READABLE' extension.
   *
   * @return  binary attribute names
   */
  public String[] getBinaryAttributeNames()
  {
    final List<String> binaryAttrs = new ArrayList<>();
    for (AttributeType type : attributeTypes.getElements()) {
      boolean isBinary = false;
      final String syntaxOid = type.getSyntaxOID(false);
      if (BINARY_SYNTAX.equals(syntaxOid)) {
        isBinary = true;
      } else {
        final Syntax syntax = getSyntax(syntaxOid);
        if (Syntax.containsBooleanExtension(syntax, "X-NOT-HUMAN-READABLE")) {
          isBinary = true;
        }
      }
      if (isBinary) {
        Collections.addAll(binaryAttrs, type.getNames());
      }
    }
    return binaryAttrs.toArray(new String[0]);
  }


  /**
   * Returns the DIT content rules.
   *
   * @return  DIT content rules
   */
  public Collection<DITContentRule> getDITContentRules()
  {
    return ditContentRules.getElements();
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
    final DITContentRule ditContentRule = ditContentRules.getElementByKey(name);
    if (ditContentRule == null) {
      return ditContentRules.getElementByName(name);
    }
    return ditContentRule;
  }


  /**
   * Sets the DIT content rules.
   *
   * @param  c  DIT content rules
   */
  public void setDITContentRules(final Collection<DITContentRule> c)
  {
    assertMutable();
    ditContentRules.setElements(c);
  }


  /**
   * Returns the DIT structure rules.
   *
   * @return  DIT structure rules
   */
  public Collection<DITStructureRule> getDITStructureRules()
  {
    return ditStructureRules.getElements();
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
    return ditStructureRules.getElementByKey(id);
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
    return ditStructureRules.getElementByName(name);
  }


  /**
   * Sets the DIT structure rules.
   *
   * @param  c  DIT structure rules
   */
  public void setDITStructureRules(final Collection<DITStructureRule> c)
  {
    assertMutable();
    ditStructureRules.setElements(c);
  }


  /**
   * Returns the syntaxes.
   *
   * @return  syntaxes
   */
  public Collection<Syntax> getSyntaxes()
  {
    return syntaxes.getElements();
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
    return syntaxes.getElementByKey(oid);
  }


  /**
   * Sets the syntaxes.
   *
   * @param  c  syntaxes
   */
  public void setSyntaxes(final Collection<Syntax> c)
  {
    assertMutable();
    syntaxes.setElements(c);
  }


  /**
   * Returns the matching rules.
   *
   * @return  matching rules
   */
  public Collection<MatchingRule> getMatchingRules()
  {
    return matchingRules.getElements();
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
    final MatchingRule matchingRule = matchingRules.getElementByKey(name);
    if (matchingRule == null) {
      return matchingRules.getElementByName(name);
    }
    return matchingRule;
  }


  /**
   * Sets the matching rules.
   *
   * @param  c  matching rules
   */
  public void setMatchingRules(final Collection<MatchingRule> c)
  {
    assertMutable();
    matchingRules.setElements(c);
  }


  /**
   * Returns the matching rule uses.
   *
   * @return  matching rule uses
   */
  public Collection<MatchingRuleUse> getMatchingRuleUses()
  {
    return matchingRuleUses.getElements();
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
    final MatchingRuleUse matchingRuleUse = matchingRuleUses.getElementByKey(name);
    if (matchingRuleUse == null) {
      return matchingRuleUses.getElementByName(name);
    }
    return matchingRuleUse;
  }


  /**
   * Sets the matching rule uses.
   *
   * @param  c  matching rule uses
   */
  public void setMatchingRuleUses(final Collection<MatchingRuleUse> c)
  {
    assertMutable();
    matchingRuleUses.setElements(c);
  }


  /**
   * Returns the name forms.
   *
   * @return  name forms
   */
  public Collection<NameForm> getNameForms()
  {
    return nameForms.getElements();
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
    final NameForm nameForm = nameForms.getElementByKey(name);
    if (nameForm == null) {
      return nameForms.getElementByName(name);
    }
    return nameForm;
  }


  /**
   * Sets the name forms.
   *
   * @param  c  name forms
   */
  public void setNameForms(final Collection<NameForm> c)
  {
    assertMutable();
    nameForms.setElements(c);
  }


  /**
   * Returns the object classes.
   *
   * @return  object classes
   */
  public Collection<ObjectClass> getObjectClasses()
  {
    return objectClasses.getElements();
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
    final ObjectClass objectClass = objectClasses.getElementByKey(name);
    if (objectClass == null) {
      return objectClasses.getElementByName(name);
    }
    return objectClass;
  }


  /**
   * Sets the object classes.
   *
   * @param  c  object classes
   */
  public void setObjectClasses(final Collection<ObjectClass> c)
  {
    assertMutable();
    objectClasses.setElements(c);
  }


  @Override
  public boolean equals(final Object o)
  {
    if (o == this) {
      return true;
    }
    if (o instanceof Schema) {
      final Schema v = (Schema) o;
      return LdapUtils.areEqual(attributeTypes, v.attributeTypes) &&
             LdapUtils.areEqual(ditContentRules, v.ditContentRules) &&
             LdapUtils.areEqual(ditStructureRules, v.ditStructureRules) &&
             LdapUtils.areEqual(syntaxes, v.syntaxes) &&
             LdapUtils.areEqual(matchingRules, v.matchingRules) &&
             LdapUtils.areEqual(matchingRuleUses, v.matchingRuleUses) &&
             LdapUtils.areEqual(nameForms, v.nameForms) &&
             LdapUtils.areEqual(objectClasses, v.objectClasses);
    }
    return false;
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
  public String toString()
  {
    return "[" +
      getClass().getName() + "@" + hashCode() + "::" +
      "attributeTypes=" + attributeTypes + ", " +
      "ditContentRules=" + ditContentRules + ", " +
      "ditStructureRules=" + ditStructureRules + ", " +
      "syntaxes=" + syntaxes + ", " +
      "matchingRules=" + matchingRules + ", " +
      "matchingRuleUses=" + matchingRuleUses + ", " +
      "nameForms=" + nameForms + ", " +
      "objectClasses=" + objectClasses + "]";
  }
}
