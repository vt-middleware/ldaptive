/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.schema;

import java.nio.CharBuffer;
import java.util.Arrays;
import java.util.List;
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
 */
public class ObjectClass extends AbstractNamedSchemaElement
{

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 1109;

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
   * Parses the supplied definition string and creates an initialized object class.
   *
   * @param  definition  to parse
   *
   * @return  object class
   *
   * @throws  SchemaParseException  if the supplied definition is invalid
   */
  public static ObjectClass parse(final String definition)
    throws SchemaParseException
  {
    return SchemaParser.parse(ObjectClass.class, definition);
  }


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


  @Override
  public boolean equals(final Object o)
  {
    if (o == this) {
      return true;
    }
    if (o instanceof ObjectClass) {
      final ObjectClass v = (ObjectClass) o;
      return LdapUtils.areEqual(oid, v.oid) &&
             LdapUtils.areEqual(getNames(), v.getNames()) &&
             LdapUtils.areEqual(getDescription(), v.getDescription()) &&
             LdapUtils.areEqual(isObsolete(), v.isObsolete()) &&
             LdapUtils.areEqual(superiorClasses, v.superiorClasses) &&
             LdapUtils.areEqual(objectClassType, v.objectClassType) &&
             LdapUtils.areEqual(requiredAttributes, v.requiredAttributes) &&
             LdapUtils.areEqual(optionalAttributes, v.optionalAttributes) &&
             LdapUtils.areEqual(getExtensions(), v.getExtensions());
    }
    return false;
  }


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


  @Override
  public String toString()
  {
    return new StringBuilder("[").append(
      getClass().getName()).append("@").append(hashCode()).append("::")
      .append("oid=").append(oid).append(", ")
      .append("names=").append(Arrays.toString(getNames())).append(", ")
      .append("description=").append(getDescription()).append(", ")
      .append("obsolete=").append(isObsolete()).append(", ")
      .append("superiorClasses=").append(Arrays.toString(superiorClasses)).append(", ")
      .append("objectClassType=").append(objectClassType).append(", ")
      .append("requiredAttributes=").append(Arrays.toString(requiredAttributes)).append(", ")
      .append("optionalAttributes=").append(Arrays.toString(optionalAttributes)).append(", ")
      .append("extensions=").append(getExtensions()).append("]").toString();
  }


  /** Parses an object class definition using a char buffer. */
  public static class DefaultDefinitionFunction extends AbstractDefaultDefinitionFunction<ObjectClass>
  {


    @Override
    public ObjectClass parse(final String definition)
      throws SchemaParseException
    {
      final CharBuffer buffer = validate(definition);
      skipSpaces(buffer);
      final ObjectClass oc = new ObjectClass(readUntilSpace(buffer));
      final Extensions exts = new Extensions();
      while (buffer.hasRemaining()) {
        skipSpaces(buffer);
        final String token = readUntilSpace(buffer);
        skipSpaces(buffer);
        switch (token) {
        case "NAME":
          oc.setNames(readQDStrings(buffer));
          break;
        case "DESC":
          oc.setDescription(readQDString(buffer));
          break;
        case "OBSOLETE":
          oc.setObsolete(true);
          break;
        case "SUP":
          oc.setSuperiorClasses(readOIDs(buffer));
          break;
        case "ABSTRACT":
          oc.setObjectClassType(ObjectClassType.ABSTRACT);
          break;
        case "STRUCTURAL":
          oc.setObjectClassType(ObjectClassType.STRUCTURAL);
          break;
        case "AUXILIARY":
          oc.setObjectClassType(ObjectClassType.AUXILIARY);
          break;
        case "MUST":
          oc.setRequiredAttributes(readOIDs(buffer));
          break;
        case "MAY":
          oc.setOptionalAttributes(readOIDs(buffer));
          break;
        case "":
          break;
        default:
          if (!token.startsWith("X-")) {
            throw new SchemaParseException(
              "Definition '" + definition + "' contains invalid extension '" + token + "'");
          }
          skipSpaces(buffer);
          exts.addExtension(token, List.of(readQDStrings(buffer)));
          break;
        }
      }
      if (!exts.isEmpty()) {
        oc.setExtensions(exts);
      }
      return oc;
    }
  }


  /** Parses an object class definition using a regular expression. */
  public static class RegexDefinitionFunction extends AbstractRegexDefinitionFunction<ObjectClass>
  {

    /** Pattern to match definitions. */
    private static final Pattern DEFINITION_PATTERN = Pattern.compile(
      WSP_REGEX + "\\(" +
        WSP_REGEX + "(" + NO_WSP_REGEX + ")" +
        WSP_REGEX + "(?:NAME" + ONE_WSP_REGEX + "(?:'([^']+)'|\\(([^\\)]+)\\)))?" +
        WSP_REGEX + "(?:DESC" + ONE_WSP_REGEX + "'([^']*)')?" +
        WSP_REGEX + "(OBSOLETE)?" +
        WSP_REGEX + "(?:SUP" + ONE_WSP_REGEX + "(?:(" + NO_WSP_REGEX + ")|\\(([^\\)]+)\\)))?" +
        WSP_REGEX + "(\\p{Alpha}+)?" +
        WSP_REGEX + "(?:MUST" + ONE_WSP_REGEX + "(?:(" + NO_WSP_REGEX + ")|\\(([^\\)]+)\\)))?" +
        WSP_REGEX + "(?:MAY" + ONE_WSP_REGEX + "(?:(" + NO_WSP_REGEX + ")|\\(([^\\)]+)\\)))?" +
        WSP_REGEX + "(?:(X-[^ ]+.*))?" +
        WSP_REGEX + "\\)" + WSP_REGEX);


    @Override
    public ObjectClass parse(final String definition)
      throws SchemaParseException
    {
      final Matcher m = DEFINITION_PATTERN.matcher(definition);
      if (!m.matches()) {
        throw new SchemaParseException("Invalid object class definition: " + definition);
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
        ocd.setExtensions(parseExtensions(m.group(13).trim()));
      }
      return ocd;
      // CheckStyle:MagicNumber ON
    }
  }
}
