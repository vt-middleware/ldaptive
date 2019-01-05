/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.protocol;

import org.ldaptive.LdapUtils;
import org.ldaptive.asn1.DERBuffer;

/**
 * LDAP notice of disconnection defined as:
 *
 * <pre>
   ExtendedResponse ::= [APPLICATION 24] SEQUENCE {
     COMPONENTS OF LDAPResult,
     responseName     [10] LDAPOID OPTIONAL,
     responseValue    [11] OCTET STRING OPTIONAL }
 * </pre>
 *
 * where the result code indicates the reason for disconnection and the response value is absent.
 *
 * @author  Middleware Services
 */
public class NoticeOfDisconnection extends UnsolicitedNotification
{

  /** OID of this response. */
  public static final String OID = "1.3.6.1.4.1.1466.20036";

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 10289;


  /**
   * Default constructor.
   */
  private NoticeOfDisconnection()
  {
    setResponseName(OID);
  }


  /**
   * Creates a new notice of disconnection.
   *
   * @param  buffer  to decode
   */
  public NoticeOfDisconnection(final DERBuffer buffer)
  {
    super(buffer);
  }


  @Override
  public boolean equals(final Object o)
  {
    if (o == this) {
      return true;
    }
    return o instanceof NoticeOfDisconnection && super.equals(o);
  }


  @Override
  public int hashCode()
  {
    return
      LdapUtils.computeHashCode(
        HASH_CODE_SEED,
        getMessageID(),
        getControls(),
        getResultCode(),
        getMatchedDN(),
        getDiagnosticMessage(),
        getReferralURLs(),
        getResponseName(),
        getResponseValue());
  }


  // CheckStyle:OFF
  protected static class Builder extends AbstractResult.AbstractBuilder<Builder, NoticeOfDisconnection>
  {


    public Builder()
    {
      super(new NoticeOfDisconnection());
    }


    @Override
    protected Builder self()
    {
      return this;
    }


    public Builder responseName(final String name)
    {
      object.setResponseName(name);
      return this;
    }


    public Builder responseValue(final byte[] value)
    {
      object.setResponseValue(value);
      return this;
    }
  }
  // CheckStyle:ON
}
