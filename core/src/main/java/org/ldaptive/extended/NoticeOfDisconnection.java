/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.extended;

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
public final class NoticeOfDisconnection extends UnsolicitedNotification
{

  /** OID of this response. */
  public static final String OID = "1.3.6.1.4.1.1466.20036";

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 10289;


  /**
   * Default constructor.
   */
  public NoticeOfDisconnection()
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


  /**
   * Creates a builder for this class.
   *
   * @return  new builder
   */
  public static Builder builder()
  {
    return new Builder();
  }


  // CheckStyle:OFF
  public static final class Builder extends UnsolicitedNotification.Builder
  {


    private Builder()
    {
      super(new NoticeOfDisconnection());
    }


    @Override
    protected Builder self()
    {
      return this;
    }
  }
  // CheckStyle:ON
}
