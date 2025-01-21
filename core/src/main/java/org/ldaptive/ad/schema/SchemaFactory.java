/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.ad.schema;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.ldaptive.LdapException;
import org.ldaptive.ReturnAttributes;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchResponse;
import org.ldaptive.control.util.PagedResultsClient;
import org.ldaptive.io.LdifReader;
import org.ldaptive.schema.AttributeType;
import org.ldaptive.schema.ObjectClass;
import org.ldaptive.schema.ObjectClassType;
import org.ldaptive.schema.Schema;
import org.ldaptive.schema.SchemaUtils;

/**
 * Factory to create {@link Schema} from an active directory schema search result. Active Directory does not adhere to
 * RFC 4512 to represent its schema. Each schema element is represented with a separate LDAP entry. The factory parses
 * and sets the object classes and attribute types for the schema. The other properties on the schema object are not
 * available.
 *
 * @author  Middleware Services
 */
public final class SchemaFactory
{

  /** Attribute on the root DSE indicating the location of the schema entries. */
  private static final String SCHEMA_NAMING_CONTEXT_ATTR_NAME = "schemaNamingContext";

  /** Filter for schema entries. */
  private static final String SCHEMA_FILTER = "(|(objectClass=attributeSchema)(objectClass=classSchema))";

  /** Object class attribute name on the entry. */
  private static final String OBJECT_CLASS_ATTR_NAME = "objectClass";

  /** Object class category attribute name on the entry. */
  private static final String OBJECT_CLASS_CATEGORY_ATTR_NAME = "objectClassCategory";

  /** Attribute schema attribute name on the entry. */
  private static final String ATTRIBUTE_SCHEMA_ATTR_NAME = "attributeSchema";

  /** Class schema attribute name on the entry. */
  private static final String CLASS_SCHEMA_ATTR_NAME = "classSchema";

  /** Attribute ID attribute name on the entry. */
  private static final String ATTRIBUTE_ID_ATTR_NAME = "attributeID";

  /** LDAP display name attribute name on the entry. */
  private static final String LDAP_DISPLAY_NAME_ATTR_NAME = "lDAPDisplayName";

  /** Admin display name attribute name on the entry. */
  private static final String ADMIN_DISPLAY_NAME_ATTR_NAME = "adminDisplayName";

  /** Name attribute name on the entry. */
  private static final String NAME_ATTR_NAME = "name";

  /** Admin description attribute name on the entry. */
  private static final String ADMIN_DESCRIPTION_ATTR_NAME = "adminDescription";

  /** Attribute syntax attribute name on the entry. */
  private static final String ATTRIBUTE_SYNTAX_ATTR_NAME = "attributeSyntax";

  /** Is single valued attribute name on the entry. */
  private static final String IS_SINGLE_VALUED_ATTR_NAME = "isSingleValued";

  /** System only attribute name on the entry. */
  private static final String SYSTEM_ONLY_ATTR_NAME = "systemOnly";

  /** Governs ID attribute name on the entry. */
  private static final String GOVERNS_ID_ATTR_NAME = "governsID";

  /** Sub class of attribute name on the entry. */
  private static final String SUB_CLASS_OF_ATTR_NAME = "subClassOf";

  /** Must contain attribute name on the entry. */
  private static final String MUST_CONTAIN_ATTR_NAME = "mustContain";

  /** System must contain attribute name on the entry. */
  private static final String SYSTEM_MUST_CONTAIN_ATTR_NAME = "systemMustContain";

  /** May contain attribute name on the entry. */
  private static final String MAY_CONTAIN_ATTR_NAME = "mayContain";

  /** System may contain attribute name on the entry. */
  private static final String SYSTEM_MAY_CONTAIN_ATTR_NAME = "systemMayContain";


  /** Default constructor. */
  private SchemaFactory() {}


  /**
   * Creates a new schema. The input stream should contain the LDIF for the schema search results.
   *
   * @param  is  containing the schema ldif
   *
   * @return  schema
   *
   * @throws  IOException  if an error occurs reading the input stream
   */
  public static Schema createSchema(final InputStream is)
    throws IOException
  {
    final LdifReader reader = new LdifReader(new InputStreamReader(is, StandardCharsets.UTF_8));
    return createSchema(reader.read());
  }


  /**
   * Creates a new schema. The schema naming context is searched for on the root DSE, followed by searching for all
   * entries under the schema naming context.
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
      SCHEMA_NAMING_CONTEXT_ATTR_NAME);
    final String entryDn = rootDSE.getAttribute(SCHEMA_NAMING_CONTEXT_ATTR_NAME).getStringValue();
    return createSchema(factory, entryDn, SCHEMA_FILTER, ReturnAttributes.ALL.value());
  }


  /**
   * Creates a new schema. The entryDn is searched to obtain the schema.
   *
   * @param  factory  to obtain an LDAP connection from
   * @param  entryDn  the schema entries
   *
   * @return  schema
   *
   * @throws  LdapException  if the search fails
   */
  public static Schema createSchema(final ConnectionFactory factory, final String entryDn)
    throws LdapException
  {
    return createSchema(factory, entryDn, SCHEMA_FILTER, ReturnAttributes.ALL.value());
  }


  /**
   * Creates a new schema. The schema result should contain entries with the 'attributeSchema' and 'classSchema'
   * objectClasses.
   *
   * @param  schemaResult  containing the schema entries
   *
   * @return  schema
   */
  public static Schema createSchema(final SearchResponse schemaResult)
  {
    final Set<AttributeType> attributeTypes = new HashSet<>();
    final Set<ObjectClass> objectClasses = new HashSet<>();
    for (LdapEntry entry : schemaResult.getEntries()) {
      entry.processAttribute(
        OBJECT_CLASS_ATTR_NAME,
        attr -> {
          if (attr.getStringValues().contains(ATTRIBUTE_SCHEMA_ATTR_NAME)) {
            attributeTypes.add(createAttributeType(entry));
          }
          if (attr.getStringValues().contains(CLASS_SCHEMA_ATTR_NAME)) {
            objectClasses.add(createObjectClass(entry));
          }
        });
    }

    final Schema schema = new Schema();
    schema.setAttributeTypes(attributeTypes);
    schema.setObjectClasses(objectClasses);
    return schema;
  }


  /**
   * Searches for the supplied dn and returns its ldap entry. This methods uses the paged results search control as
   * schema entries typically number beyond the server search size limit. An entry handler is used in order to free
   * memory as soon an entry is processed.
   *
   * @param  factory  to obtain an LDAP connection from
   * @param  dn  to search for
   * @param  filter  to search with
   * @param  retAttrs  attributes to return
   *
   * @return  new schema
   *
   * @throws  LdapException  if the search fails
   */
  private static Schema createSchema(
    final ConnectionFactory factory,
    final String dn,
    final String filter,
    final String... retAttrs)
    throws LdapException
  {
    final Set<AttributeType> attributeTypes = new HashSet<>();
    final Set<ObjectClass> objectClasses = new HashSet<>();
    final PagedResultsClient client = new PagedResultsClient(factory, 100);
    client.setEntryHandlers(entry -> {
      final LdapAttribute attr = entry.getAttribute(OBJECT_CLASS_ATTR_NAME);
      if (attr != null) {
        if (attr.getStringValues().contains(ATTRIBUTE_SCHEMA_ATTR_NAME)) {
          attributeTypes.add(createAttributeType(entry));
        }
        if (attr.getStringValues().contains(CLASS_SCHEMA_ATTR_NAME)) {
          objectClasses.add(createObjectClass(entry));
        }
      }
      return null;
    });
    client.executeToCompletion(SearchRequest.builder().dn(dn).filter(filter).returnAttributes(retAttrs).build());

    final Schema schema = new Schema();
    schema.setAttributeTypes(attributeTypes);
    schema.setObjectClasses(objectClasses);
    return schema;
  }


  /**
   * Creates an attribute type from the supplied ldap entry. The entry must contain an objectClass of 'attributeSchema'.
   * This method only populates the OID, names, description, syntax, and single valued properties of the attribute type.
   *
   * @param  entry  containing an attribute schema
   *
   * @return  attribute type
   */
  private static AttributeType createAttributeType(final LdapEntry entry)
  {
    final LdapAttribute la = entry.getAttribute(OBJECT_CLASS_ATTR_NAME);
    if (la == null || !la.getStringValues().contains(ATTRIBUTE_SCHEMA_ATTR_NAME)) {
      throw new IllegalArgumentException("Entry is not an attribute schema");
    }
    return
      new AttributeType(
        entry.getAttributeStringValue(ATTRIBUTE_ID_ATTR_NAME),
        getFirstAttributeValues(
          entry, LDAP_DISPLAY_NAME_ATTR_NAME, ADMIN_DISPLAY_NAME_ATTR_NAME, NAME_ATTR_NAME),
        entry.getAttributeStringValue(ADMIN_DESCRIPTION_ATTR_NAME),
        false,
        null,
        null,
        null,
        null,
        entry.getAttributeStringValue(ATTRIBUTE_SYNTAX_ATTR_NAME),
        Boolean.parseBoolean(entry.getAttributeStringValue(IS_SINGLE_VALUED_ATTR_NAME)),
        false,
        Boolean.parseBoolean(entry.getAttributeStringValue(SYSTEM_ONLY_ATTR_NAME)),
        null,
        null);
  }


  /**
   * Creates an object class from the supplied ldap entry. The entry must contain an objectClass of 'classSchema'. This
   * method only populates the OID, names, description, superior classes, object class type, required attributes, and
   * optional attributes of the object class.
   *
   * @param  entry  containing a class schema
   *
   * @return  object class
   */
  private static ObjectClass createObjectClass(final LdapEntry entry)
  {
    final LdapAttribute la = entry.getAttribute(OBJECT_CLASS_ATTR_NAME);
    if (la == null || !la.getStringValues().contains(CLASS_SCHEMA_ATTR_NAME)) {
      throw new IllegalArgumentException("Entry is not an object class");
    }

    ObjectClassType ocType = null;
    final String ocCategory = entry.getAttributeStringValue(OBJECT_CLASS_CATEGORY_ATTR_NAME);
    if (ocCategory != null) {
      for (ObjectClassType type : ObjectClassType.values()) {
        if (type.ordinal() == Integer.parseInt(ocCategory)) {
          ocType = type;
          break;
        }
      }
    }
    return
      new ObjectClass(
        entry.getAttributeStringValue(GOVERNS_ID_ATTR_NAME),
        getFirstAttributeValues(
          entry, LDAP_DISPLAY_NAME_ATTR_NAME, ADMIN_DISPLAY_NAME_ATTR_NAME, NAME_ATTR_NAME),
        entry.getAttributeStringValue(ADMIN_DESCRIPTION_ATTR_NAME),
        false,
        getAllAttributeValues(entry, SUB_CLASS_OF_ATTR_NAME),
        ocType,
        getAllAttributeValues(entry, MUST_CONTAIN_ATTR_NAME, SYSTEM_MUST_CONTAIN_ATTR_NAME),
        getAllAttributeValues(entry, MAY_CONTAIN_ATTR_NAME, SYSTEM_MAY_CONTAIN_ATTR_NAME),
        null);
  }


  /**
   * Returns the values for the first attribute name found in the supplied entry.
   *
   * @param  entry  containing the attributes
   * @param  names  to search for in the entry
   *
   * @return  attribute values or null if no attributes are found
   */
  private static String[] getFirstAttributeValues(final LdapEntry entry, final String... names)
  {
    for (String name : names) {
      final LdapAttribute la = entry.getAttribute(name);
      if (la != null) {
        return la.getStringValues().toArray(new String[0]);
      }
    }
    return null;
  }


  /**
   * Returns the values for all attributes with the supplied names found in the supplied entry.
   *
   * @param  entry  containing the attributes
   * @param  names  to search for in the entry
   *
   * @return  attribute values or null if no attributes are found
   */
  private static String[] getAllAttributeValues(final LdapEntry entry, final String... names)
  {
    final List<String> allValues = new ArrayList<>();
    for (String name : names) {
      final LdapAttribute la = entry.getAttribute(name);
      if (la != null) {
        allValues.addAll(la.getStringValues());
      }
    }
    return allValues.isEmpty() ? null : allValues.toArray(new String[0]);
  }
}
