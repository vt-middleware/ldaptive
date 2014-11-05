/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.schema.io;

import java.text.ParseException;
import org.ldaptive.schema.Syntax;

/**
 * Decodes and encodes an attribute syntax for use in an ldap attribute value.
 *
 * @author  Middleware Services
 * @version  $Revision: 2940 $ $Date: 2014-03-31 11:10:46 -0400 (Mon, 31 Mar 2014) $
 */
public class SyntaxValueTranscoder
  extends AbstractSchemaElementValueTranscoder<Syntax>
{


  /** {@inheritDoc} */
  @Override
  public Syntax decodeStringValue(final String value)
  {
    try {
      return Syntax.parse(value);
    } catch (ParseException e) {
      throw new IllegalArgumentException(
        "Could not transcode attribute syntax",
        e);
    }
  }


  /** {@inheritDoc} */
  @Override
  public Class<Syntax> getType()
  {
    return Syntax.class;
  }
}
