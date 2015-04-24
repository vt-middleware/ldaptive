/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.schema;

import java.text.ParseException;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.ldaptive.LdapUtils;

/**
 * Bean for an matching rule use schema element.
 *
 * <pre>
   MatchingRuleUseDescription = LPAREN WSP
     numericoid                 ; object identifier
     [ SP "NAME" SP qdescrs ]   ; short names (descriptors)
     [ SP "DESC" SP qdstring ]  ; description
     [ SP "OBSOLETE" ]          ; not active
     SP "APPLIES" SP oids       ; attribute types
     extensions WSP RPAREN      ; extensions
 * </pre>
 *
 * @author  Middleware Services
 */
public class MatchingRuleUse extends AbstractNamedSchemaElement
{

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 1123;

  /** Pattern to match definitions. */
  private static final Pattern DEFINITION_PATTERN = Pattern.compile(
    WSP_REGEX + "\\(" +
      WSP_REGEX + "(" + NO_WSP_REGEX + ")" +
      WSP_REGEX + "(?:NAME (?:'([^']+)'|\\(([^\\)]+)\\)))?" +
      WSP_REGEX + "(?:DESC '([^']*)')?" +
      WSP_REGEX + "(OBSOLETE)?" +
      WSP_REGEX + "(?:APPLIES (?:(" + NO_WSP_REGEX + ")|\\(([^\\)]+)\\)))?" +
      WSP_REGEX + "(?:(X-[^ ]+.*))?" +
      WSP_REGEX + "\\)" + WSP_REGEX);

  /** OID. */
  private final String oid;

  /** Superior classes. */
  private String[] appliesAttributeTypes;


  /**
   * Creates a new matching rule use.
   *
   * @param  s  oid
   */
  public MatchingRuleUse(final String s)
  {
    oid = s;
  }


  /**
   * Creates a new matching rule use.
   *
   * @param  oid  oid
   * @param  names  names
   * @param  description  description
   * @param  obsolete  obsolete
   * @param  appliesAttributeTypes  applies attribute types
   * @param  extensions  extensions
   */
  // CheckStyle:ParameterNumber|HiddenField OFF
  public MatchingRuleUse(
    final String oid,
    final String[] names,
    final String description,
    final boolean obsolete,
    final String[] appliesAttributeTypes,
    final Extensions extensions)
  {
    this(oid);
    setNames(names);
    setDescription(description);
    setObsolete(obsolete);
    setAppliesAttributeTypes(appliesAttributeTypes);
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
   * Returns the applies attribute types.
   *
   * @return  attribute types
   */
  public String[] getAppliesAttributeTypes()
  {
    return appliesAttributeTypes;
  }


  /**
   * Sets the applies attribute types.
   *
   * @param  s  attribute types
   */
  public void setAppliesAttributeTypes(final String[] s)
  {
    appliesAttributeTypes = s;
  }


  /**
   * Parses the supplied definition string and creates an initialized matching rule use.
   *
   * @param  definition  to parse
   *
   * @return  matching rule use
   *
   * @throws  ParseException  if the supplied definition is invalid
   */
  public static MatchingRuleUse parse(final String definition)
    throws ParseException
  {
    final Matcher m = DEFINITION_PATTERN.matcher(definition);
    if (!m.matches()) {
      throw new ParseException("Invalid matching rule use definition: " + definition, definition.length());
    }

    final MatchingRuleUse mrud = new MatchingRuleUse(m.group(1).trim());

    // CheckStyle:MagicNumber OFF
    // parse names
    if (m.group(2) != null) {
      mrud.setNames(SchemaUtils.parseDescriptors(m.group(2).trim()));
    } else if (m.group(3) != null) {
      mrud.setNames(SchemaUtils.parseDescriptors(m.group(3).trim()));
    }

    mrud.setDescription(m.group(4) != null ? m.group(4).trim() : null);
    mrud.setObsolete(m.group(5) != null);

    // parse applies attribute types
    if (m.group(6) != null) {
      mrud.setAppliesAttributeTypes(SchemaUtils.parseOIDs(m.group(6).trim()));
    } else if (m.group(7) != null) {
      mrud.setAppliesAttributeTypes(SchemaUtils.parseOIDs(m.group(7).trim()));
    }

    // parse extensions
    if (m.group(8) != null) {
      mrud.setExtensions(Extensions.parse(m.group(8).trim()));
    }
    return mrud;
    // CheckStyle:MagicNumber ON
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
    if (appliesAttributeTypes != null && appliesAttributeTypes.length > 0) {
      sb.append("APPLIES ");
      sb.append(SchemaUtils.formatOids(appliesAttributeTypes));
    }
    if (getExtensions() != null) {
      sb.append(getExtensions().format());
    }
    sb.append(")");
    return sb.toString();
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
        appliesAttributeTypes,
        getExtensions());
  }


  @Override
  public String toString()
  {
    return
      String.format(
        "[%s@%d::oid=%s, names=%s, description=%s, obsolete=%s, " +
        "appliesAttributeTypes=%s, extensions=%s]",
        getClass().getName(),
        hashCode(),
        oid,
        Arrays.toString(getNames()),
        getDescription(),
        isObsolete(),
        Arrays.toString(appliesAttributeTypes),
        getExtensions());
  }
}
