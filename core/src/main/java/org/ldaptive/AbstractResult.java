/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.ldaptive.asn1.AbstractParseHandler;
import org.ldaptive.asn1.DERBuffer;
import org.ldaptive.asn1.DERParser;
import org.ldaptive.asn1.IntegerType;
import org.ldaptive.asn1.OctetStringType;

/**
 * LDAP result message defined as:
 *
 * <pre>
   LDAPResult ::= SEQUENCE {
     resultCode         ENUMERATED {
       ...  },
     matchedDN          LDAPDN,
     diagnosticMessage  LDAPString,
     referral           [3] Referral OPTIONAL }

   Referral ::= SEQUENCE SIZE (1..MAX) OF uri URI

     URI ::= LDAPString     -- limited to characters permitted in
                            -- URIs
 * </pre>
 *
 * @author  Middleware Services
 */
public abstract class AbstractResult extends AbstractMessage implements Result
{

  /** Referral URLS. */
  private final List<String> referralURLs = new ArrayList<>();

  /** Result code. */
  private ResultCode resultCode;

  /** Matched DN. */
  private String matchedDN;

  /** Diagnostic message. */
  private String diagnosticMessage;


  /**
   * Returns the result code.
   *
   * @return  result code
   */
  @Override
  public final ResultCode getResultCode()
  {
    return resultCode;
  }


  /**
   * Sets the result code.
   *
   * @param  code  result code
   */
  protected final void setResultCode(final ResultCode code)
  {
    resultCode = code;
  }


  /**
   * Returns the matched DN.
   *
   * @return  matched DN
   */
  @Override
  public final String getMatchedDN()
  {
    return matchedDN;
  }


  /**
   * Sets the matched DN.
   *
   * @param  dn  matched DN
   */
  protected final void setMatchedDN(final String dn)
  {
    matchedDN = dn;
  }


  /**
   * Returns the diagnostic message.
   *
   * @return  diagnostic message
   */
  @Override
  public final String getDiagnosticMessage()
  {
    return diagnosticMessage;
  }


  /**
   * Sets the diagnostic message.
   *
   * @param  message  diagnostic message
   */
  protected final void setDiagnosticMessage(final String message)
  {
    diagnosticMessage = message;
  }


  /**
   * Returns the referral URLs.
   *
   * @return  referral URLs
   */
  @Override
  public final String[] getReferralURLs()
  {
    return referralURLs != null ? referralURLs.toArray(new String[0]) : null;
  }


  /**
   * Adds referral URLs to the result.
   *
   * @param  urls  to add
   */
  protected final void addReferralURLs(final String... urls)
  {
    Collections.addAll(referralURLs, urls);
  }


  /**
   * Copies the property values from the supplied result to this result.
   *
   * @param  <T>  type of result
   * @param  result  to copy from
   */
  protected final <T extends Result> void copyValues(final T result)
  {
    super.copyValues(result);
    setResultCode(result.getResultCode());
    setMatchedDN(result.getMatchedDN());
    setDiagnosticMessage(result.getDiagnosticMessage());
    addReferralURLs(result.getReferralURLs());
  }


  /**
   * Returns whether the base properties of this result are equal. Those include message ID, controls, result code,
   * matched DN, diagnostic message and referral URLs.
   *
   * @param  result  to compare
   *
   * @return  whether result properties are equal
   */
  public final boolean equalsResult(final Result result)
  {
    if (result == this) {
      return true;
    }
    if (super.equalsMessage(result)) {
      return LdapUtils.areEqual(getResultCode(), result.getResultCode()) &&
        LdapUtils.areEqual(getMatchedDN(), result.getMatchedDN()) &&
        LdapUtils.areEqual(getDiagnosticMessage(), result.getDiagnosticMessage()) &&
        LdapUtils.areEqual(getReferralURLs(), result.getReferralURLs());
    }
    return false;
  }


  // CheckStyle:EqualsHashCode OFF
  @Override
  public boolean equals(final Object o)
  {
    if (o == this) {
      return true;
    }
    if (o instanceof AbstractResult && super.equals(o)) {
      final AbstractResult v = (AbstractResult) o;
      return LdapUtils.areEqual(getResultCode(), v.getResultCode()) &&
        LdapUtils.areEqual(getMatchedDN(), v.getMatchedDN()) &&
        LdapUtils.areEqual(getDiagnosticMessage(), v.getDiagnosticMessage()) &&
        LdapUtils.areEqual(getReferralURLs(), v.getReferralURLs());
    }
    return false;
  }
  // CheckStyle:EqualsHashCode ON


  @Override
  public String toString()
  {
    return super.toString() + ", " +
      "resultCode=" + resultCode + ", " +
      "matchedDN=" + matchedDN + ", " +
      "diagnosticMessage=" + getEncodedDiagnosticMessage() + ", " +
      "referralURLs=" + referralURLs;
  }


  /** Parse handler implementation for the LDAP result code. */
  protected static class ResultCodeHandler extends AbstractParseHandler<AbstractResult>
  {


    /**
     * Creates a new LDAP result code handler.
     *
     * @param  response  to configure
     */
    public ResultCodeHandler(final AbstractResult response)
    {
      super(response);
    }


    @Override
    public void handle(final DERParser parser, final DERBuffer encoded)
    {
      getObject().setResultCode(ResultCode.valueOf(IntegerType.decodeUnsignedPrimitive(encoded)));
    }
  }


  /** Parse handler implementation for the LDAP matched DN. */
  protected static class MatchedDNHandler extends AbstractParseHandler<AbstractResult>
  {


    /**
     * Creates a new LDAP matched DN handler.
     *
     * @param  response  to configure
     */
    public MatchedDNHandler(final AbstractResult response)
    {
      super(response);
    }


    @Override
    public void handle(final DERParser parser, final DERBuffer encoded)
    {
      getObject().setMatchedDN(OctetStringType.decode(encoded));
    }
  }


  /** Parse handler implementation for the LDAP diagnostic message. */
  protected static class DiagnosticMessageHandler extends AbstractParseHandler<AbstractResult>
  {


    /**
     * Creates a new LDAP diagnostic message handler.
     *
     * @param  response  to configure
     */
    public DiagnosticMessageHandler(final AbstractResult response)
    {
      super(response);
    }


    @Override
    public void handle(final DERParser parser, final DERBuffer encoded)
    {
      getObject().setDiagnosticMessage(OctetStringType.decode(encoded));
    }
  }


  /** Parse handler implementation for the LDAP referral. */
  protected static class ReferralHandler extends AbstractParseHandler<AbstractResult>
  {


    /**
     * Creates a new LDAP referral handler.
     *
     * @param  response  to configure
     */
    public ReferralHandler(final AbstractResult response)
    {
      super(response);
    }


    @Override
    public void handle(final DERParser parser, final DERBuffer encoded)
    {
      getObject().addReferralURLs(OctetStringType.decode(encoded));
    }
  }


  // CheckStyle:OFF
  protected abstract static class AbstractBuilder<B, T extends AbstractResult>
    extends AbstractMessage.AbstractBuilder<B, T>
  {


    protected AbstractBuilder(final T t)
    {
      super(t);
    }


    public B resultCode(final ResultCode code)
    {
      object.setResultCode(code);
      return self();
    }


    public B matchedDN(final String dn)
    {
      object.setMatchedDN(dn);
      return self();
    }


    public B diagnosticMessage(final String message)
    {
      object.setDiagnosticMessage(message);
      return self();
    }


    public B referralURLs(final String... url)
    {
      object.addReferralURLs(url);
      return self();
    }


    public B copy(final Result r)
    {
      object.copyValues(r);
      return self();
    }
  }
  // CheckStyle:ON
}
