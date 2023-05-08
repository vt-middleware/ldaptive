/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.schema;

import java.nio.CharBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.ldaptive.LdapUtils;

/**
 * Bean for a name form schema element.
 *
 * <pre>
   NameFormDescription = LPAREN WSP
     numericoid                 ; object identifier
     [ SP "NAME" SP qdescrs ]   ; short names (descriptors)
     [ SP "DESC" SP qdstring ]  ; description
     [ SP "OBSOLETE" ]          ; not active
     SP "OC" SP oid             ; structural object class
     SP "MUST" SP oids          ; attribute types
     [ SP "MAY" SP oids ]       ; attribute types
     extensions WSP RPAREN      ; extensions
 * </pre>
 *
 * @author  Middleware Services
 */
public class NameForm extends AbstractNamedSchemaElement
{

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 1163;

  /** OID. */
  private final String oid;

  /** Structural object class. */
  private String structuralClass;

  /** Required attributes. */
  private String[] requiredAttributes;

  /** Optional attributes. */
  private String[] optionalAttributes;


  /**
   * Creates a new name form.
   *
   * @param  s  oid
   */
  public NameForm(final String s)
  {
    oid = s;
  }


  /**
   * Creates a new name form.
   *
   * @param  oid  oid
   * @param  names  names
   * @param  description  description
   * @param  obsolete  obsolete
   * @param  structuralClass  structural object class
   * @param  requiredAttributes  required attributes
   * @param  optionalAttributes  optional attributes
   * @param  extensions  extensions
   */
  // CheckStyle:ParameterNumber|HiddenField OFF
  public NameForm(
    final String oid,
    final String[] names,
    final String description,
    final boolean obsolete,
    final String structuralClass,
    final String[] requiredAttributes,
    final String[] optionalAttributes,
    final Extensions extensions)
  {
    this(oid);
    setNames(names);
    setDescription(description);
    setObsolete(obsolete);
    setStructuralClass(structuralClass);
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
   * Returns the structural object class.
   *
   * @return  structural object class
   */
  public String getStructuralClass()
  {
    return structuralClass;
  }


  /**
   * Sets the structural object class.
   *
   * @param  s  structural object class
   */
  public void setStructuralClass(final String s)
  {
    structuralClass = s;
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
   * Parses the supplied definition string and creates an initialized name form.
   *
   * @param  definition  to parse
   *
   * @return  name form
   *
   * @throws  SchemaParseException  if the supplied definition is invalid
   */
  public static NameForm parse(final String definition)
    throws SchemaParseException
  {
    return SchemaParser.parse(NameForm.class, definition);
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
    if (structuralClass != null) {
      sb.append("OC ").append(structuralClass).append(" ");
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
    if (o instanceof NameForm) {
      final NameForm v = (NameForm) o;
      return LdapUtils.areEqual(oid, v.oid) &&
        LdapUtils.areEqual(getNames(), v.getNames()) &&
        LdapUtils.areEqual(getDescription(), v.getDescription()) &&
        LdapUtils.areEqual(isObsolete(), v.isObsolete()) &&
        LdapUtils.areEqual(structuralClass, v.structuralClass) &&
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
        structuralClass,
        requiredAttributes,
        optionalAttributes,
        getExtensions());
  }


  @Override
  public String toString()
  {
    return "[" +
      getClass().getName() + "@" + hashCode() + "::" +
      "oid=" + oid + ", " +
      "names=" + Arrays.toString(getNames()) + ", " +
      "description=" + getDescription() + ", " +
      "obsolete=" + isObsolete() + ", " +
      "structuralClass=" + structuralClass + ", " +
      "requiredAttributes=" + Arrays.toString(requiredAttributes) + ", " +
      "optionalAttributes=" + Arrays.toString(optionalAttributes) + ", " +
      "extensions=" + getExtensions() + "]";
  }


  /** Parses a name form definition using a char buffer. */
  public static class DefaultDefinitionFunction extends AbstractDefaultDefinitionFunction<NameForm>
  {


    @Override
    public NameForm parse(final String definition)
      throws SchemaParseException
    {
      final CharBuffer buffer = validate(definition);
      skipSpaces(buffer);
      final NameForm nf = new NameForm(readUntilSpace(buffer));
      final Extensions exts = new Extensions();
      while (buffer.hasRemaining()) {
        skipSpaces(buffer);
        final String token = readUntilSpace(buffer);
        skipSpaces(buffer);
        switch (token) {
        case "NAME":
          nf.setNames(readQDStrings(buffer));
          break;
        case "DESC":
          nf.setDescription(readQDString(buffer));
          break;
        case "OBSOLETE":
          nf.setObsolete(true);
          break;
        case "OC":
          nf.setStructuralClass(readOID(buffer));
          break;
        case "MUST":
          nf.setRequiredAttributes(readOIDs(buffer));
          break;
        case "MAY":
          nf.setOptionalAttributes(readOIDs(buffer));
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
        nf.setExtensions(exts);
      }
      return nf;
    }
  }


  /** Parses a name form definition using a regular expression. */
  public static class RegexDefinitionFunction extends AbstractRegexDefinitionFunction<NameForm>
  {

    /** Pattern to match definitions. */
    private static final Pattern DEFINITION_PATTERN = Pattern.compile(
      WSP_REGEX + "\\(" +
        WSP_REGEX + "(" + NO_WSP_REGEX + ")" +
        WSP_REGEX + "(?:NAME" + ONE_WSP_REGEX + "(?:'([^']+)'|\\(([^\\)]+)\\)))?" +
        WSP_REGEX + "(?:DESC" + ONE_WSP_REGEX + "'([^']*)')?" +
        WSP_REGEX + "(OBSOLETE)?" +
        WSP_REGEX + "(?:OC" + ONE_WSP_REGEX + "(" + NO_WSP_REGEX + "))?" +
        WSP_REGEX + "(?:MUST" + ONE_WSP_REGEX + "(?:(" + NO_WSP_REGEX + ")|\\(([^\\)]+)\\)))?" +
        WSP_REGEX + "(?:MAY" + ONE_WSP_REGEX + "(?:(" + NO_WSP_REGEX + ")|\\(([^\\)]+)\\)))?" +
        WSP_REGEX + "(?:(X-[^ ]+.*))?" +
        WSP_REGEX + "\\)" + WSP_REGEX);


    @Override
    public NameForm parse(final String definition)
      throws SchemaParseException
    {
      final Matcher m = DEFINITION_PATTERN.matcher(definition);
      if (!m.matches()) {
        throw new SchemaParseException("Invalid name form definition: " + definition);
      }

      final NameForm nfd = new NameForm(m.group(1).trim());

      // CheckStyle:MagicNumber OFF
      // parse names
      if (m.group(2) != null) {
        nfd.setNames(SchemaUtils.parseDescriptors(m.group(2).trim()));
      } else if (m.group(3) != null) {
        nfd.setNames(SchemaUtils.parseDescriptors(m.group(3).trim()));
      }

      nfd.setDescription(m.group(4) != null ? m.group(4).trim() : null);
      nfd.setObsolete(m.group(5) != null);
      nfd.setStructuralClass(m.group(6) != null ? m.group(6).trim() : null);

      // parse required attributes
      if (m.group(7) != null) {
        nfd.setRequiredAttributes(SchemaUtils.parseOIDs(m.group(7).trim()));
      } else if (m.group(8) != null) {
        nfd.setRequiredAttributes(SchemaUtils.parseOIDs(m.group(8).trim()));
      }

      // parse optional attributes
      if (m.group(9) != null) {
        nfd.setOptionalAttributes(SchemaUtils.parseOIDs(m.group(9).trim()));
      } else if (m.group(10) != null) {
        nfd.setOptionalAttributes(SchemaUtils.parseOIDs(m.group(10).trim()));
      }

      // parse extensions
      if (m.group(11) != null) {
        nfd.setExtensions(parseExtensions(m.group(11).trim()));
      }
      return nfd;
      // CheckStyle:MagicNumber ON
    }
  }
}
