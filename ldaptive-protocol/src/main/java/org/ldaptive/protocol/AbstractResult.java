/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.protocol;

import java.util.ArrayList;
import java.util.List;
import org.ldaptive.LdapUtils;
import org.ldaptive.ResultCode;
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
 * </pre>
 *
 * @author  Middleware Services
 */
public abstract class AbstractResult extends AbstractMessage implements Result
{

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
    return referralURLs.stream().toArray(String[]::new);
  }


  /**
   * Adds referral URLs to the result.
   *
   * @param  url  to add
   */
  public void addReferralURLs(final String... url)
  {
    for (String s : url) {
      referralURLs.add(s);
    }
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
      .append("diagnosticMessage=").append(diagnosticMessage).append(", ")
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
    ResultCodeHandler(final AbstractResult response)
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
    MatchedDNHandler(final AbstractResult response)
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
    DiagnosticMessageHandler(final AbstractResult response)
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
    ReferralHandler(final AbstractResult response)
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


    AbstractBuilder(final T t)
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
