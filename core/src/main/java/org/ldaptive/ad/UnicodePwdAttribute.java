/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.ad;

import java.util.Collection;
import org.ldaptive.LdapAttribute;
import org.ldaptive.ad.transcode.UnicodePwdValueTranscoder;

/**
 * Helper class for the active directory unicodePwd attribute. Configures a binary attribute of that name and allows
 * setting of the attribute value using a string. See {@link UnicodePwdValueTranscoder}.
 *
 * @author  Middleware Services
 */
public class UnicodePwdAttribute extends LdapAttribute
{

  /** name of this attribute. */
  private static final String ATTR_NAME = "unicodePwd";

  /** transcoder used when adding string values. */
  private static final UnicodePwdValueTranscoder TRANSCODER = new UnicodePwdValueTranscoder();


  /** Default constructor. */
  public UnicodePwdAttribute()
  {
    setName(ATTR_NAME);
    setBinary(true);
  }


  /**
   * Creates a new unicode pwd attribute.
   *
   * @param  values  of this attribute
   */
  public UnicodePwdAttribute(final String... values)
  {
    this();
    addStringValues(values);
  }


  @Override
  public Collection<String> getStringValues()
  {
    return getValues(TRANSCODER.decoder());
  }


  @Override
  public void addStringValues(final String... value)
  {
    addValues(TRANSCODER.encoder(), value);
  }
}
