/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.util.ArrayList;
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

  /** Whether to encode control characters. */
  protected static final boolean ENCODE_CNTRL_CHARS = Boolean.valueOf(
    System.getProperty("org.ldaptive.response.ENCODE_CNTRL_CHARS", "false"));

  /** Result code. */
  private ResultCode resultCode;

  /** Matched DN. */
  private String matchedDN;

  /** Diagnostic message. */
  private String diagnosticMessage;

  /** Referral URLS. */
  private List<String> referralURLs = new ArrayList<>();


  public ResultCode getResultCode()
  {
    return resultCode;
  }


  public void setResultCode(final ResultCode code)
  {
    resultCode = code;
  }


  public String getMatchedDN()
  {
    return matchedDN;
  }


  public void setMatchedDN(final String dn)
  {
    matchedDN = dn;
  }


  public String getDiagnosticMessage()
  {
    return diagnosticMessage;
  }


  public void setDiagnosticMessage(final String message)
  {
    diagnosticMessage = message;
  }


  public String[] getReferralURLs()
  {
    return referralURLs.toArray(new String[0]);
  }


  /**
   * Adds referral URLs to the result.
   *
   * @param  urls  to add
   */
  public void addReferralURLs(final String... urls)
  {
    for (String s : urls) {
      referralURLs.add(s);
    }
  }


  /**
   * Copies the property values from the supplied result to this result.
   *
   * @param  <T>  type of result
   * @param  result  to copy from
   */
  protected <T extends Result> void copyValues(final T result)
  {
    super.copyValues(result);
    setResultCode(result.getResultCode());
    setMatchedDN(result.getMatchedDN());
    setDiagnosticMessage(result.getDiagnosticMessage());
    addReferralURLs(result.getReferralURLs());
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
    return new StringBuilder(super.toString()).append(", ")
      .append("resultCode=").append(resultCode).append(", ")
      .append("matchedDN=").append(matchedDN).append(", ")
      .append("diagnosticMessage=").append(
        diagnosticMessage != null && !"".equals(diagnosticMessage) ?
          ENCODE_CNTRL_CHARS ? LdapUtils.percentEncodeControlChars(diagnosticMessage) : diagnosticMessage :
        diagnosticMessage).append(", ")
      .append("referralURLs=").append(referralURLs).toString();
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
  }
  // CheckStyle:ON
}
