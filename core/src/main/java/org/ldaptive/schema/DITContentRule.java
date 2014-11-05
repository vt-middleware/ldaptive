/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.schema;

import java.text.ParseException;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.ldaptive.LdapUtils;

/**
 * Bean for a DIT content rule schema element.
 *
 * <pre>
   DITContentRuleDescription = LPAREN WSP
     numericoid                 ; object identifier
     [ SP "NAME" SP qdescrs ]   ; short names (descriptors)
     [ SP "DESC" SP qdstring ]  ; description
     [ SP "OBSOLETE" ]          ; not active
     [ SP "AUX" SP oids ]       ; auxiliary object classes
     [ SP "MUST" SP oids ]      ; attribute types
     [ SP "MAY" SP oids ]       ; attribute types
     [ SP "NOT" SP oids ]       ; attribute types
     extensions WSP RPAREN      ; extensions
 * </pre>
 *
 * @author  Middleware Services
 * @version  $Revision: 2940 $ $Date: 2014-03-31 11:10:46 -0400 (Mon, 31 Mar 2014) $
 */
public class DITContentRule extends AbstractNamedSchemaElement
{

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 1151;

  /** Pattern to match definitions. */
  private static final Pattern DEFINITION_PATTERN = Pattern.compile(
    WSP_REGEX + "\\(" +
      WSP_REGEX + "(" + NO_WSP_REGEX + ")" +
      WSP_REGEX + "(?:NAME (?:'([^']+)'|\\(([^\\)]+)\\)))?" +
      WSP_REGEX + "(?:DESC '([^']+)')?" +
      WSP_REGEX + "(OBSOLETE)?" +
      WSP_REGEX + "(?:AUX (?:(" + NO_WSP_REGEX + ")|\\(([^\\)]+)\\)))?" +
      WSP_REGEX + "(?:MUST (?:(" + NO_WSP_REGEX + ")|\\(([^\\)]+)\\)))?" +
      WSP_REGEX + "(?:MAY (?:(" + NO_WSP_REGEX + ")|\\(([^\\)]+)\\)))?" +
      WSP_REGEX + "(?:NOT (?:(" + NO_WSP_REGEX + ")|\\(([^\\)]+)\\)))?" +
      WSP_REGEX + "(?:(X-[^ ]+.*))?" +
      WSP_REGEX + "\\)" + WSP_REGEX);

  /** OID. */
  private final String oid;

  /** Auxiliary classes. */
  private String[] auxiliaryClasses;

  /** Required attributes. */
  private String[] requiredAttributes;

  /** Optional attributes. */
  private String[] optionalAttributes;

  /** Restricted attributes. */
  private String[] restrictedAttributes;


  /**
   * Creates a new DIT content rule.
   *
   * @param  s  oid
   */
  public DITContentRule(final String s)
  {
    oid = s;
  }


  /**
   * Creates a new DIT content rule.
   *
   * @param  oid  oid
   * @param  names  names
   * @param  description  description
   * @param  obsolete  obsolete
   * @param  auxiliaryClasses  auxiliary classes
   * @param  requiredAttributes  required attributes
   * @param  optionalAttributes  optional attributes
   * @param  restrictedAttributes  restricted attributes
   * @param  extensions  extensions
   */
  // CheckStyle:ParameterNumber|HiddenField OFF
  public DITContentRule(
    final String oid,
    final String[] names,
    final String description,
    final boolean obsolete,
    final String[] auxiliaryClasses,
    final String[] requiredAttributes,
    final String[] optionalAttributes,
    final String[] restrictedAttributes,
    final Extensions extensions)
  {
    this(oid);
    setNames(names);
    setDescription(description);
    setObsolete(obsolete);
    setAuxiliaryClasses(auxiliaryClasses);
    setRequiredAttributes(requiredAttributes);
    setOptionalAttributes(optionalAttributes);
    setRestrictedAttributes(restrictedAttributes);
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
   * Returns the auxiliary classes.
   *
   * @return  auxiliary classes
   */
  public String[] getAuxiliaryClasses()
  {
    return auxiliaryClasses;
  }


  /**
   * Sets the auxiliary classes.
   *
   * @param  s  auxiliary classes
   */
  public void setAuxiliaryClasses(final String[] s)
  {
    auxiliaryClasses = s;
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
   * Returns the restricted attributes.
   *
   * @return  restricted attributes
   */
  public String[] getRestrictedAttributes()
  {
    return restrictedAttributes;
  }


  /**
   * Sets the restricted attributes.
   *
   * @param  s  restricted attributes
   */
  public void setRestrictedAttributes(final String[] s)
  {
    restrictedAttributes = s;
  }


  /**
   * Parses the supplied definition string and creates an initialized DIT
   * content rule.
   *
   * @param  definition  to parse
   *
   * @return  DIT content rule
   *
   * @throws  ParseException  if the supplied definition is invalid
   */
  public static DITContentRule parse(final String definition)
    throws ParseException
  {
    final Matcher m = DEFINITION_PATTERN.matcher(definition);
    if (!m.matches()) {
      throw new ParseException(
        "Invalid DIT content rule definition: " + definition,
        definition.length());
    }

    final DITContentRule dcrd = new DITContentRule(m.group(1).trim());

    // CheckStyle:MagicNumber OFF
    // parse names
    if (m.group(2) != null) {
      dcrd.setNames(SchemaUtils.parseDescriptors(m.group(2).trim()));
    } else if (m.group(3) != null) {
      dcrd.setNames(SchemaUtils.parseDescriptors(m.group(3).trim()));
    }

    dcrd.setDescription(m.group(4) != null ? m.group(4).trim() : null);
    dcrd.setObsolete(m.group(5) != null);

    // parse auxiliary classes
    if (m.group(6) != null) {
      dcrd.setAuxiliaryClasses(SchemaUtils.parseOIDs(m.group(6).trim()));
    } else if (m.group(7) != null) {
      dcrd.setAuxiliaryClasses(SchemaUtils.parseOIDs(m.group(7).trim()));
    }

    // parse required attributes
    if (m.group(9) != null) {
      dcrd.setRequiredAttributes(SchemaUtils.parseOIDs(m.group(9).trim()));
    } else if (m.group(10) != null) {
      dcrd.setRequiredAttributes(SchemaUtils.parseOIDs(m.group(10).trim()));
    }

    // parse optional attributes
    if (m.group(11) != null) {
      dcrd.setOptionalAttributes(SchemaUtils.parseOIDs(m.group(11).trim()));
    } else if (m.group(12) != null) {
      dcrd.setOptionalAttributes(SchemaUtils.parseOIDs(m.group(12).trim()));
    }

    // parse restricted attributes
    if (m.group(11) != null) {
      dcrd.setRestrictedAttributes(SchemaUtils.parseOIDs(m.group(11).trim()));
    } else if (m.group(12) != null) {
      dcrd.setRestrictedAttributes(SchemaUtils.parseOIDs(m.group(12).trim()));
    }

    // parse extensions
    if (m.group(13) != null) {
      dcrd.setExtensions(Extensions.parse(m.group(13).trim()));
    }
    return dcrd;
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
    if (auxiliaryClasses != null && auxiliaryClasses.length > 0) {
      sb.append("AUX ");
      sb.append(SchemaUtils.formatOids(auxiliaryClasses));
    }
    if (requiredAttributes != null && requiredAttributes.length > 0) {
      sb.append("MUST ");
      sb.append(SchemaUtils.formatOids(requiredAttributes));
    }
    if (optionalAttributes != null && optionalAttributes.length > 0) {
      sb.append("MAY ");
      sb.append(SchemaUtils.formatOids(optionalAttributes));
    }
    if (restrictedAttributes != null && restrictedAttributes.length > 0) {
      sb.append("NOT ");
      sb.append(SchemaUtils.formatOids(restrictedAttributes));
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
        auxiliaryClasses,
        requiredAttributes,
        optionalAttributes,
        restrictedAttributes,
        getExtensions());
  }


  /** {@inheritDoc} */
  @Override
  public String toString()
  {
    return
      String.format(
        "[%s@%d::oid=%s, names=%s, description=%s, obsolete=%s, " +
        "auxiliaryClasses=%s, requiredAttributes=%s, " +
        "optionalAttributes=%s, restrictedAttributes=%s, extensions=%s]",
        getClass().getName(),
        hashCode(),
        oid,
        Arrays.toString(getNames()),
        getDescription(),
        isObsolete(),
        Arrays.toString(auxiliaryClasses),
        Arrays.toString(requiredAttributes),
        Arrays.toString(optionalAttributes),
        Arrays.toString(restrictedAttributes),
        getExtensions());
  }
}
