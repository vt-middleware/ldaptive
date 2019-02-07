/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.ad.schema;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashSet;
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

/**
 * Factory to create {@link Schema} from an active directory schema search result. Active Directory does not adhere to
 * RFC 4512 to represent it's schema. Each schema element is represented with a separate LDAP entry. The factory parses
 * and sets the object classes and attribute types for the schema. The other properties on the schema object are not
 * available.
 *
 * @author  Middleware Services
 */
public final class SchemaFactory
{


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
    final LdifReader reader = new LdifReader(new InputStreamReader(is));
    return createSchema(reader.read());
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
    return createSchema(getSearchResult(factory, entryDn, "(objectClass=*)", ReturnAttributes.ALL.value()));
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
      final LdapAttribute la = entry.getAttribute("objectClass");
      if (la != null && la.getStringValues().contains("attributeSchema")) {
        attributeTypes.add(createAttributeType(entry));
      }
      if (la != null && la.getStringValues().contains("classSchema")) {
        objectClasses.add(createObjectClass(entry));
      }
    }

    final Schema schema = new Schema();
    schema.setAttributeTypes(attributeTypes);
    schema.setObjectClasses(objectClasses);
    return schema;
  }


  /**
   * Searches for the supplied dn and returns its ldap entry. This methods uses the paged results search control as
   * schema entries typically number beyond the server search size limit.
   *
   * @param  factory  to obtain an LDAP connection from
   * @param  dn  to search for
   * @param  filter  to search with
   * @param  retAttrs  attributes to return
   *
   * @return  ldap entry
   *
   * @throws  LdapException  if the search fails
   */
  protected static SearchResponse getSearchResult(
    final ConnectionFactory factory,
    final String dn,
    final String filter,
    final String[] retAttrs)
    throws LdapException
  {
    final PagedResultsClient client = new PagedResultsClient(factory, 100);
    return client.executeToCompletion(
      SearchRequest.builder()
        .dn(dn).filter(filter).attributes(retAttrs).build());
  }


  /**
   * Creates an attribute type from the supplied ldap entry. The entry must contain an objectClass of 'attributeSchema'.
   * This method only populates the OID, names, description, syntax, and single valued properties of the attribute type.
   *
   * @param  entry  containing an attribute schema
   *
   * @return  attribute type
   */
  protected static AttributeType createAttributeType(final LdapEntry entry)
  {
    final LdapAttribute la = entry.getAttribute("objectClass");
    if (la == null || !la.getStringValues().contains("attributeSchema")) {
      throw new IllegalArgumentException("Entry is not an attribute schema");
    }
    return
      new AttributeType(
        getAttributeValue(entry, "attributeID"),
        getAttributeValues(entry, "lDAPDisplayName", "adminDisplayName", "name"),
        getAttributeValue(entry, "adminDescription"),
        false,
        null,
        null,
        null,
        null,
        getAttributeValue(entry, "attributeSyntax"),
        Boolean.valueOf(getAttributeValue(entry, "isSingleValued")),
        false,
        false,
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
  protected static ObjectClass createObjectClass(final LdapEntry entry)
  {
    final LdapAttribute la = entry.getAttribute("objectClass");
    if (la == null || !la.getStringValues().contains("classSchema")) {
      throw new IllegalArgumentException("Entry is not an object class");
    }

    ObjectClassType ocType = null;
    final String ocCategory = getAttributeValue(entry, "objectClassCategory");
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
        getAttributeValue(entry, "governsID"),
        getAttributeValues(entry, "lDAPDisplayName", "adminDisplayName", "name"),
        getAttributeValue(entry, "adminDescription"),
        false,
        getAttributeValues(entry, "possSuperiors", "systemPossSuperiors"),
        ocType,
        getAttributeValues(entry, "mustContain", "systemMustContain"),
        getAttributeValues(entry, "mayContain", "systemMayContain"),
        null);
  }


  /**
   * Returns a single value for the first attribute name found in the supplied entry.
   *
   * @param  entry  containing the attributes
   * @param  names  to search for in the entry
   *
   * @return  single attribute value
   */
  private static String getAttributeValue(final LdapEntry entry, final String... names)
  {
    String value = null;
    for (String name : names) {
      final LdapAttribute la = entry.getAttribute(name);
      if (la != null) {
        value = la.getStringValue();
        break;
      }
    }
    return value;
  }


  /**
   * Returns the values for the first attribute name found in the supplied entry.
   *
   * @param  entry  containing the attributes
   * @param  names  to search for in the entry
   *
   * @return  attribute values
   */
  private static String[] getAttributeValues(final LdapEntry entry, final String... names)
  {
    Collection<String> values = null;
    for (String name : names) {
      final LdapAttribute la = entry.getAttribute(name);
      if (la != null) {
        values = la.getStringValues();
        break;
      }
    }
    return values != null ? values.toArray(new String[0]) : null;
  }
}
