/*
  $Id: SchemaFactory.java 2940 2014-03-31 15:10:46Z dfisher $

  Copyright (C) 2003-2014 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 2940 $
  Updated: $Date: 2014-03-31 11:10:46 -0400 (Mon, 31 Mar 2014) $
*/
package org.ldaptive.schema;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.ldaptive.LdapException;
import org.ldaptive.ReturnAttributes;
import org.ldaptive.SearchExecutor;
import org.ldaptive.SearchResult;
import org.ldaptive.SearchScope;
import org.ldaptive.io.LdifReader;
import org.ldaptive.schema.io.AttributeTypeValueTranscoder;
import org.ldaptive.schema.io.DITContentRuleValueTranscoder;
import org.ldaptive.schema.io.DITStructureRuleValueTranscoder;
import org.ldaptive.schema.io.MatchingRuleUseValueTranscoder;
import org.ldaptive.schema.io.MatchingRuleValueTranscoder;
import org.ldaptive.schema.io.NameFormValueTranscoder;
import org.ldaptive.schema.io.ObjectClassValueTranscoder;
import org.ldaptive.schema.io.SyntaxValueTranscoder;

/**
 * Factory to create {@link Schema} objects from an LDAP entry.
 *
 * @author  Middleware Services
 * @version  $Revision: 2940 $ $Date: 2014-03-31 11:10:46 -0400 (Mon, 31 Mar 2014) $
 */
public final class SchemaFactory
{

  /**
   * Attribute on the root DSE indicating the location of the subschema entry.
   */
  private static final String SUBSCHEMA_SUBENTRY_ATTR_NAME =
    "subschemaSubentry";

  /** Attribute types attribute name on the subschema entry. */
  private static final String ATTRIBUTE_TYPES_ATTR_NAME = "attributeTypes";

  /** DIT content rules attribute name on the subschema entry. */
  private static final String DIT_CONTENT_RULES_ATTR_NAME = "dITContentRules";

  /** DIT structure rules attribute name on the subschema entry. */
  private static final String DIT_STRUCTURE_RULES_ATTR_NAME =
    "dITStructureRules";

  /** LDAP syntaxes attribute name on the subschema entry. */
  private static final String LDAP_SYNTAXES_ATTR_NAME = "ldapSyntaxes";

  /** Matching rules attribute name on the subschema entry. */
  private static final String MATCHING_RULES_ATTR_NAME = "matchingRules";

  /** Matching rule use attribute name on the subschema entry. */
  private static final String MATCHING_RULE_USE_ATTR_NAME = "matchingRuleUse";

  /** Name forms attribute name on the subschema entry. */
  private static final String NAME_FORMS_ATTR_NAME = "nameForms";

  /** Object classes attribute name on the subschema entry. */
  private static final String OBJECT_CLASS_ATTR_NAME = "objectClasses";


  /** Default constructor. */
  private SchemaFactory() {}


  /**
   * Creates a new schema. The input stream should contain the LDIF for the
   * subschema entry.
   *
   * @param  is  containing the schema ldif
   *
   * @return  schema created from the ldif
   *
   * @throws  IOException  if an error occurs reading the input stream
   */
  public static Schema createSchema(final InputStream is)
    throws IOException
  {
    final LdifReader reader = new LdifReader(new InputStreamReader(is));
    return createSchema(reader.read().getEntry());
  }


  /**
   * Creates a new schema. The subschema subentry is searched for on the root
   * DSE, followed by searching for the subschema entry itself.
   *
   * @param  factory  to obtain an LDAP connection from
   *
   * @return  schema created from the connection factory
   *
   * @throws  LdapException  if the search fails
   */
  public static Schema createSchema(final ConnectionFactory factory)
    throws LdapException
  {
    final LdapEntry rootDSE = getLdapEntry(
      factory,
      "",
      "(objectClass=*)",
      new String[] {SUBSCHEMA_SUBENTRY_ATTR_NAME});
    final String entryDn = rootDSE.getAttribute(
      SUBSCHEMA_SUBENTRY_ATTR_NAME).getStringValue();
    return
      createSchema(
        getLdapEntry(
          factory,
          entryDn,
          "(objectClass=subSchema)",
          ReturnAttributes.ALL.value()));
  }


  /**
   * Creates a new schema. The entryDn is searched to obtain the schema.
   *
   * @param  factory  to obtain an LDAP connection from
   * @param  entryDn  the subschema entry
   *
   * @return  schema created from the connection factory
   *
   * @throws  LdapException  if the search fails
   */
  public static Schema createSchema(
    final ConnectionFactory factory,
    final String entryDn)
    throws LdapException
  {
    return
      createSchema(
        getLdapEntry(
          factory,
          entryDn,
          "(objectClass=subSchema)",
          ReturnAttributes.ALL.value()));
  }


  /**
   * Creates a new schema. The schema entry is parsed to obtain the schema.
   *
   * @param  schemaEntry  containing the schema
   *
   * @return  schema created from the entry
   */
  public static Schema createSchema(final LdapEntry schemaEntry)
  {
    if (schemaEntry == null) {
      throw new IllegalArgumentException("Schema entry cannot be null");
    }

    final Schema schema = new Schema();

    final LdapAttribute atAttr = schemaEntry.getAttribute(
      ATTRIBUTE_TYPES_ATTR_NAME);
    if (atAttr != null) {
      schema.setAttributeTypes(
        atAttr.getValues(new AttributeTypeValueTranscoder()));
    }

    final LdapAttribute dcrAttr = schemaEntry.getAttribute(
      DIT_CONTENT_RULES_ATTR_NAME);
    if (dcrAttr != null) {
      schema.setDitContentRules(
        dcrAttr.getValues(new DITContentRuleValueTranscoder()));
    }

    final LdapAttribute dsrAttr = schemaEntry.getAttribute(
      DIT_STRUCTURE_RULES_ATTR_NAME);
    if (dsrAttr != null) {
      schema.setDitStructureRules(
        dsrAttr.getValues(new DITStructureRuleValueTranscoder()));
    }

    final LdapAttribute sAttr = schemaEntry.getAttribute(
      LDAP_SYNTAXES_ATTR_NAME);
    if (sAttr != null) {
      schema.setSyntaxes(sAttr.getValues(new SyntaxValueTranscoder()));
    }

    final LdapAttribute mrAttr = schemaEntry.getAttribute(
      MATCHING_RULES_ATTR_NAME);
    if (mrAttr != null) {
      schema.setMatchingRules(
        mrAttr.getValues(new MatchingRuleValueTranscoder()));
    }

    final LdapAttribute mruAttr = schemaEntry.getAttribute(
      MATCHING_RULE_USE_ATTR_NAME);
    if (mruAttr != null) {
      schema.setMatchingRuleUses(
        mruAttr.getValues(new MatchingRuleUseValueTranscoder()));
    }

    final LdapAttribute nfAttr = schemaEntry.getAttribute(NAME_FORMS_ATTR_NAME);
    if (nfAttr != null) {
      schema.setNameForms(nfAttr.getValues(new NameFormValueTranscoder()));
    }

    final LdapAttribute ocAttr = schemaEntry.getAttribute(
      OBJECT_CLASS_ATTR_NAME);
    if (ocAttr != null) {
      schema.setObjectClasses(
        ocAttr.getValues(new ObjectClassValueTranscoder()));
    }

    return schema;
  }


  /**
   * Searches for the supplied dn and returns its ldap entry.
   *
   * @param  factory  to obtain an LDAP connection from
   * @param  dn  to search for
   * @param  filter  search filter
   * @param  retAttrs  attributes to return
   *
   * @return  ldap entry
   *
   * @throws  LdapException  if the search fails
   */
  protected static LdapEntry getLdapEntry(
    final ConnectionFactory factory,
    final String dn,
    final String filter,
    final String[] retAttrs)
    throws LdapException
  {
    final SearchExecutor executor = new SearchExecutor();
    executor.setBaseDn(dn);
    executor.setSearchScope(SearchScope.OBJECT);
    executor.setReturnAttributes(retAttrs);

    final SearchResult result = executor.search(factory, filter).getResult();
    return result.getEntry();
  }
}
