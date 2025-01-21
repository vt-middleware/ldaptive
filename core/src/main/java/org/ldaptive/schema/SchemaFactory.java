/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.schema;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.LdapEntry;
import org.ldaptive.LdapException;
import org.ldaptive.ReturnAttributes;
import org.ldaptive.io.LdifReader;
import org.ldaptive.schema.transcode.AttributeTypeValueTranscoder;
import org.ldaptive.schema.transcode.DITContentRuleValueTranscoder;
import org.ldaptive.schema.transcode.DITStructureRuleValueTranscoder;
import org.ldaptive.schema.transcode.MatchingRuleUseValueTranscoder;
import org.ldaptive.schema.transcode.MatchingRuleValueTranscoder;
import org.ldaptive.schema.transcode.NameFormValueTranscoder;
import org.ldaptive.schema.transcode.ObjectClassValueTranscoder;
import org.ldaptive.schema.transcode.SyntaxValueTranscoder;

/**
 * Factory to create {@link Schema} objects from an LDAP entry.
 *
 * @author  Middleware Services
 */
public final class SchemaFactory
{

  /** Attribute on the root DSE indicating the location of the subschema entry. */
  private static final String SUBSCHEMA_SUBENTRY_ATTR_NAME = "subschemaSubentry";

  /** Attribute types attribute name on the subschema entry. */
  private static final String ATTRIBUTE_TYPES_ATTR_NAME = "attributeTypes";

  /** DIT content rules attribute name on the subschema entry. */
  private static final String DIT_CONTENT_RULES_ATTR_NAME = "dITContentRules";

  /** DIT structure rules attribute name on the subschema entry. */
  private static final String DIT_STRUCTURE_RULES_ATTR_NAME = "dITStructureRules";

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
   * Creates a new schema. The input stream should contain the LDIF for the subschema entry.
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
    final LdifReader reader = new LdifReader(new InputStreamReader(is, StandardCharsets.UTF_8));
    return createSchema(reader.read().getEntry());
  }


  /**
   * Creates a new schema. The subschema subentry is searched for on the root DSE, followed by searching for the
   * subschema entry itself.
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
    final LdapEntry rootDSE = SchemaUtils.getLdapEntry(
      factory,
      "",
      "(objectClass=*)",
      SUBSCHEMA_SUBENTRY_ATTR_NAME);
    final String entryDn = rootDSE.getAttribute(SUBSCHEMA_SUBENTRY_ATTR_NAME).getStringValue();
    return createSchema(
      SchemaUtils.getLdapEntry(factory, entryDn, "(objectClass=subSchema)", ReturnAttributes.ALL.value()));
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
  public static Schema createSchema(final ConnectionFactory factory, final String entryDn)
    throws LdapException
  {
    return createSchema(
      SchemaUtils.getLdapEntry(factory, entryDn, "(objectClass=subSchema)", ReturnAttributes.ALL.value()));
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
    schemaEntry.processAttribute(
      ATTRIBUTE_TYPES_ATTR_NAME,
      attr -> schema.setAttributeTypes(attr.getValues(new AttributeTypeValueTranscoder().decoder())));
    schemaEntry.processAttribute(
      DIT_CONTENT_RULES_ATTR_NAME,
      attr -> schema.setDitContentRules(attr.getValues(new DITContentRuleValueTranscoder().decoder())));
    schemaEntry.processAttribute(
      DIT_STRUCTURE_RULES_ATTR_NAME,
      attr -> schema.setDitStructureRules(attr.getValues(new DITStructureRuleValueTranscoder().decoder())));
    schemaEntry.processAttribute(
      LDAP_SYNTAXES_ATTR_NAME,
      attr -> schema.setSyntaxes(attr.getValues(new SyntaxValueTranscoder().decoder())));
    schemaEntry.processAttribute(
      MATCHING_RULES_ATTR_NAME,
      attr -> schema.setMatchingRules(attr.getValues(new MatchingRuleValueTranscoder().decoder())));
    schemaEntry.processAttribute(
      MATCHING_RULE_USE_ATTR_NAME,
      attr -> schema.setMatchingRuleUses(attr.getValues(new MatchingRuleUseValueTranscoder().decoder())));
    schemaEntry.processAttribute(
      NAME_FORMS_ATTR_NAME,
      attr -> schema.setNameForms(attr.getValues(new NameFormValueTranscoder().decoder())));
    schemaEntry.processAttribute(
      OBJECT_CLASS_ATTR_NAME,
      attr -> schema.setObjectClasses(attr.getValues(new ObjectClassValueTranscoder().decoder())));
    return schema;
  }
}
