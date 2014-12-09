/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.intermediate;

import org.ldaptive.LdapUtils;
import org.ldaptive.control.ResponseControl;

/**
 * Base class for ldap intermediate response messages.
 *
 * @author  Middleware Services
 */
public abstract class AbstractIntermediateResponse
  implements IntermediateResponse
{

  /** response oid. */
  private final String responseOid;

  /** response controls. */
  private final ResponseControl[] responseControls;

  /** message ID. */
  private final int messageId;


  /**
   * Creates a new abstract intermediate response.
   *
   * @param  oid  OID of this message
   * @param  c  response controls
   * @param  i  message id
   */
  public AbstractIntermediateResponse(
    final String oid,
    final ResponseControl[] c,
    final int i)
  {
    responseOid = oid;
    responseControls = c;
    messageId = i;
  }


  @Override
  public String getOID()
  {
    return responseOid;
  }


  @Override
  public ResponseControl[] getControls()
  {
    return responseControls;
  }


  @Override
  public ResponseControl getControl(final String oid)
  {
    if (getControls() != null) {
      for (ResponseControl c : getControls()) {
        if (c.getOID().equals(oid)) {
          return c;
        }
      }
    }
    return null;
  }


  @Override
  public int getMessageId()
  {
    return messageId;
  }


  @Override
  public boolean equals(final Object o)
  {
    return LdapUtils.areEqual(this, o);
  }


  /**
   * Returns the hash code for this object.
   *
   * @return  hash code
   */
  @Override
  public abstract int hashCode();
}
