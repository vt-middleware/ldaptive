/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.schema;

import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.ldaptive.LdapUtils;

/**
 * Bean for an attribute syntax schema element.
 *
 * <pre>
   SyntaxDescription = LPAREN WSP
     numericoid                 ; object identifier
     [ SP "DESC" SP qdstring ]  ; description
     extensions WSP RPAREN      ; extensions
 * </pre>
 *
 * @author  Middleware Services
 * @version  $Revision: 2940 $ $Date: 2014-03-31 11:10:46 -0400 (Mon, 31 Mar 2014) $
 */
public class Syntax extends AbstractSchemaElement
{

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 1129;

  /** Pattern to match definitions. */
  private static final Pattern DEFINITION_PATTERN = Pattern.compile(
    WSP_REGEX + "\\(" +
      WSP_REGEX + "(" + NO_WSP_REGEX + ")" +
      WSP_REGEX + "(?:DESC '([^']+)')?" +
      WSP_REGEX + "(?:(X-[^ ]+.*))?" +
      WSP_REGEX + "\\)" + WSP_REGEX);

  /** OID. */
  private final String oid;


  /**
   * Creates a new attribute syntax.
   *
   * @param  s  oid
   */
  public Syntax(final String s)
  {
    oid = s;
  }


  /**
   * Creates a new attribute syntax.
   *
   * @param  oid  oid
   * @param  description  description
   * @param  extensions  extensions
   */
  // CheckStyle:HiddenField OFF
  public Syntax(
    final String oid,
    final String description,
    final Extensions extensions)
  {
    this(oid);
    setDescription(description);
    setExtensions(extensions);
  }
  // CheckStyle:HiddenField ON


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
   * Parses the supplied definition string and creates an initialized attribute
   * syntax.
   *
   * @param  definition  to parse
   *
   * @return  attribute syntax
   *
   * @throws  ParseException  if the supplied definition is invalid
   */
  public static Syntax parse(final String definition)
    throws ParseException
  {
    final Matcher m = DEFINITION_PATTERN.matcher(definition);
    if (!m.matches()) {
      throw new ParseException(
        "Invalid attribute syntax definition: " + definition,
        definition.length());
    }

    final Syntax asd = new Syntax(m.group(1).trim());

    // CheckStyle:MagicNumber OFF
    asd.setDescription(m.group(2) != null ? m.group(2).trim() : null);

    // parse extensions
    if (m.group(3) != null) {
      asd.setExtensions(Extensions.parse(m.group(3).trim()));
    }
    return asd;
    // CheckStyle:MagicNumber ON
  }


  /** {@inheritDoc} */
  @Override
  public String format()
  {
    final StringBuilder sb = new StringBuilder("( ");
    sb.append(oid).append(" ");
    if (getDescription() != null) {
      sb.append("DESC ");
      sb.append(SchemaUtils.formatDescriptors(getDescription()));
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
        getDescription(),
        getExtensions());
  }


  /** {@inheritDoc} */
  @Override
  public String toString()
  {
    return
      String.format(
        "[%s@%d::oid=%s, description=%s, extensions=%s]",
        getClass().getName(),
        hashCode(),
        oid,
        getDescription(),
        getExtensions());
  }
}
