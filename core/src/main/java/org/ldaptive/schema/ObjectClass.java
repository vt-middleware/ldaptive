/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.schema;

import java.text.ParseException;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.ldaptive.LdapUtils;

/**
 * Bean for an object class schema element.
 *
 * <pre>
   ObjectClassDescription = LPAREN WSP
     numericoid                 ; object identifier
     [ SP "NAME" SP qdescrs ]   ; short names (descriptors)
     [ SP "DESC" SP qdstring ]  ; description
     [ SP "OBSOLETE" ]          ; not active
     [ SP "SUP" SP oids ]       ; superior object classes
     [ SP kind ]                ; kind of class
     [ SP "MUST" SP oids ]      ; attribute types
     [ SP "MAY" SP oids ]       ; attribute types
     extensions WSP RPAREN
 * </pre>
 *
 * @author  Middleware Services
 * @version  $Revision: 2940 $ $Date: 2014-03-31 11:10:46 -0400 (Mon, 31 Mar 2014) $
 */
public class ObjectClass extends AbstractNamedSchemaElement
{

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 1109;

  /** Pattern to match definitions. */
  private static final Pattern DEFINITION_PATTERN = Pattern.compile(
    WSP_REGEX + "\\(" +
      WSP_REGEX + "(" + NO_WSP_REGEX + ")" +
      WSP_REGEX + "(?:NAME (?:'([^']+)'|\\(([^\\)]+)\\)))?" +
      WSP_REGEX + "(?:DESC '([^']+)')?" +
      WSP_REGEX + "(OBSOLETE)?" +
      WSP_REGEX + "(?:SUP (?:(" + NO_WSP_REGEX + ")|\\(([^\\)]+)\\)))?" +
      WSP_REGEX + "(\\p{Alpha}+)?" +
      WSP_REGEX + "(?:MUST (?:(" + NO_WSP_REGEX + ")|\\(([^\\)]+)\\)))?" +
      WSP_REGEX + "(?:MAY (?:(" + NO_WSP_REGEX + ")|\\(([^\\)]+)\\)))?" +
      WSP_REGEX + "(?:(X-[^ ]+.*))?" +
      WSP_REGEX + "\\)" + WSP_REGEX);

  /** OID. */
  private final String oid;

  /** Superior classes. */
  private String[] superiorClasses;

  /** Object class type. */
  private ObjectClassType objectClassType;

  /** Required attributes. */
  private String[] requiredAttributes;

  /** Optional attributes. */
  private String[] optionalAttributes;


  /**
   * Creates a new object class.
   *
   * @param  s  oid
   */
  public ObjectClass(final String s)
  {
    oid = s;
  }


  /**
   * Creates a new object class.
   *
   * @param  oid  oid
   * @param  names  names
   * @param  description  description
   * @param  obsolete  obsolete
   * @param  superiorClasses  superior classes
   * @param  objectClassType  object class type
   * @param  requiredAttributes  required attributes
   * @param  optionalAttributes  optional attributes
   * @param  extensions  extensions
   */
  // CheckStyle:ParameterNumber|HiddenField OFF
  public ObjectClass(
    final String oid,
    final String[] names,
    final String description,
    final boolean obsolete,
    final String[] superiorClasses,
    final ObjectClassType objectClassType,
    final String[] requiredAttributes,
    final String[] optionalAttributes,
    final Extensions extensions)
  {
    this(oid);
    setNames(names);
    setDescription(description);
    setObsolete(obsolete);
    setSuperiorClasses(superiorClasses);
    setObjectClassType(objectClassType);
    setRequiredAttributes(requiredAttributes);
    setOptionalAttributes(optionalAttributes);
    setExtensions(extensions);
  }
  // CheckStyle:ParameterNumber|HiddenField ON


  /**
   * Returns the oid.
   *
   * @return  oid
   */
  public String getOID()
  {
    return oid;
  }


  /**
   * Returns the superior classes.
   *
   * @return  superior classes
   */
  public String[] getSuperiorClasses()
  {
    return superiorClasses;
  }


  /**
   * Sets the superior classes.
   *
   * @param  s  superior classes
   */
  public void setSuperiorClasses(final String[] s)
  {
    superiorClasses = s;
  }


  /**
   * Returns the object class type.
   *
   * @return  object class type
   */
  public ObjectClassType getObjectClassType()
  {
    return objectClassType;
  }


  /**
   * Sets the object class type.
   *
   * @param  type  object class type
   */
  public void setObjectClassType(final ObjectClassType type)
  {
    objectClassType = type;
  }


  /**
   * Returns the required attributes.
   *
   * @return  required attributes
   */
  public String[] getRequiredAttributes()
  {
    return requiredAttributes;
  }


  /**
   * Sets the required attributes.
   *
   * @param  s  required attributes
   */
  public void setRequiredAttributes(final String[] s)
  {
    requiredAttributes = s;
  }


  /**
   * Returns the optional attributes.
   *
   * @return  optional attributes
   */
  public String[] getOptionalAttributes()
  {
    return optionalAttributes;
  }


  /**
   * Sets the optional attributes.
   *
   * @param  s  optional attributes
   */
  public void setOptionalAttributes(final String[] s)
  {
    optionalAttributes = s;
  }


  /**
   * Parses the supplied definition string and creates an initialized object
   * class.
   *
   * @param  definition  to parse
   *
   * @return  object class
   *
   * @throws  ParseException  if the supplied definition is invalid
   */
  public static ObjectClass parse(final String definition)
    throws ParseException
  {
    final Matcher m = DEFINITION_PATTERN.matcher(definition);
    if (!m.matches()) {
      throw new ParseException(
        "Invalid object class definition: " + definition,
        definition.length());
    }

    final ObjectClass ocd = new ObjectClass(m.group(1).trim());

    // CheckStyle:MagicNumber OFF
    // parse names
    if (m.group(2) != null) {
      ocd.setNames(SchemaUtils.parseDescriptors(m.group(2).trim()));
    } else if (m.group(3) != null) {
      ocd.setNames(SchemaUtils.parseDescriptors(m.group(3).trim()));
    }

    ocd.setDescription(m.group(4) != null ? m.group(4).trim() : null);
    ocd.setObsolete(m.group(5) != null);

    // parse superior classes
    if (m.group(6) != null) {
      ocd.setSuperiorClasses(SchemaUtils.parseOIDs(m.group(6).trim()));
    } else if (m.group(7) != null) {
      ocd.setSuperiorClasses(SchemaUtils.parseOIDs(m.group(7).trim()));
    }

    if (m.group(8) != null) {
      ocd.setObjectClassType(ObjectClassType.valueOf(m.group(8).trim()));
    }

    // parse required attributes
    if (m.group(9) != null) {
      ocd.setRequiredAttributes(SchemaUtils.parseOIDs(m.group(9).trim()));
    } else if (m.group(10) != null) {
      ocd.setRequiredAttributes(SchemaUtils.parseOIDs(m.group(10).trim()));
    }

    // parse optional attributes
    if (m.group(11) != null) {
      ocd.setOptionalAttributes(SchemaUtils.parseOIDs(m.group(11).trim()));
    } else if (m.group(12) != null) {
      ocd.setOptionalAttributes(SchemaUtils.parseOIDs(m.group(12).trim()));
    }

    // parse extensions
    if (m.group(13) != null) {
      ocd.setExtensions(Extensions.parse(m.group(13).trim()));
    }
    return ocd;
    // CheckStyle:MagicNumber ON
  }


  /** {@inheritDoc} */
  @Override
  public String format()
  {
    final StringBuilder sb = new StringBuilder("( ");
    sb.append(oid).append(" ");
    if (getNames() != null && getNames().length > 0) {
      sb.append("NAME ");
      sb.append(SchemaUtils.formatDescriptors(getNames()));
    }
    if (getDescription() != null) {
      sb.append("DESC ");
      sb.append(SchemaUtils.formatDescriptors(getDescription()));
    }
    if (isObsolete()) {
      sb.append("OBSOLETE ");
    }
    if (superiorClasses != null && superiorClasses.length > 0) {
      sb.append("SUP ");
      sb.append(SchemaUtils.formatOids(superiorClasses));
    }
    if (objectClassType != null) {
      sb.append(objectClassType.name()).append(" ");
    }
    if (requiredAttributes != null && requiredAttributes.length > 0) {
      sb.append("MUST ");
      sb.append(SchemaUtils.formatOids(requiredAttributes));
    }
    if (optionalAttributes != null && optionalAttributes.length > 0) {
      sb.append("MAY ");
      sb.append(SchemaUtils.formatOids(optionalAttributes));
    }
    if (getExtensions() != null) {
      sb.append(getExtensions().format());
    }
    sb.append(")");
    return sb.toString();
  }


  /** {@inheritDoc} */
  @Override
  public int hashCode()
  {
    return
      LdapUtils.computeHashCode(
        HASH_CODE_SEED,
        oid,
        getNames(),
        getDescription(),
        isObsolete(),
        superiorClasses,
        objectClassType,
        requiredAttributes,
        optionalAttributes,
        getExtensions());
  }


  /** {@inheritDoc} */
  @Override
  public String toString()
  {
    return
      String.format(
        "[%s@%d::oid=%s, names=%s, description=%s, obsolete=%s, " +
        "superiorClasses=%s, objectClassType=%s, requiredAttributes=%s, " +
        "optionalAttributes=%s, extensions=%s]",
        getClass().getName(),
        hashCode(),
        oid,
        Arrays.toString(getNames()),
        getDescription(),
        isObsolete(),
        Arrays.toString(superiorClasses),
        objectClassType,
        Arrays.toString(requiredAttributes),
        Arrays.toString(optionalAttributes),
        getExtensions());
  }
}
