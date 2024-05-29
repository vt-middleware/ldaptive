/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.util.ArrayList;
import java.util.List;
import lombok.experimental.SuperBuilder;
import org.ldaptive.asn1.AbstractParseHandler;
import org.ldaptive.asn1.DERBuffer;
import org.ldaptive.asn1.DERParser;
import org.ldaptive.asn1.DERPath;
import org.ldaptive.asn1.IntegerType;
import org.ldaptive.asn1.OctetStringType;
import org.ldaptive.asn1.ParseHandler;
import org.ldaptive.control.ResponseControl;

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
@SuperBuilder
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


  public AbstractResult(
          final int messageID,
          final List<ResponseControl> controls,
          final ResultCode resultCode,
          final String matchedDN,
          final String diagnosticMessage,
          final List<String> referralUrls) {
    super(messageID, controls);
    this.resultCode = resultCode;
    this.matchedDN = matchedDN;
    this.diagnosticMessage = diagnosticMessage;
    this.referralURLs.addAll(referralUrls);
  }


  /**
   * Returns the result code.
   *
   * @return  result code
   */
  public final ResultCode getResultCode()
  {
    return resultCode;
  }


  /**
   * Returns the matched DN.
   *
   * @return  matched DN
   */
  public final String getMatchedDN()
  {
    return matchedDN;
  }


  /**
   * Returns the diagnostic message.
   *
   * @return  diagnostic message
   */
  public final String getDiagnosticMessage()
  {
    return diagnosticMessage;
  }


  /**
   * Returns the referral URLs.
   *
   * @return  referral URLs
   */
  public final String[] getReferralURLs()
  {
    return referralURLs.toArray(String[]::new);
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


  @Override
  public int hashCode()
  {
    return
      LdapUtils.computeHashCode(
        getHashCodeSeed(),
        getMessageID(),
        getControls(),
        getResultCode(),
        getMatchedDN(),
        getDiagnosticMessage(),
        getReferralURLs());
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


  /** @return Unique per-class seed used in {@link #hashCode()} implementation. */
  protected abstract int getHashCodeSeed();


  /**
   * Base class for all result builders.
   *
   * @param <T> Type of result produced by this builder.
   */
  protected static abstract class AbstractBuilder<T extends Result> implements Result.Builder<T>
  {
    /** DER path to result code. */
    private static final DERPath RESULT_CODE_PATH = new DERPath("/SEQ/APP(9)/ENUM[0]");

    /** DER path to matched DN. */
    private static final DERPath MATCHED_DN_PATH = new DERPath("/SEQ/APP(9)/OCTSTR[1]");

    /** DER path to diagnostic message. */
    private static final DERPath DIAGNOSTIC_MESSAGE_PATH = new DERPath("/SEQ/APP(9)/OCTSTR[2]");

    /** DER path to referral. */
    private static final DERPath REFERRAL_PATH = new DERPath("/SEQ/APP(9)/CTX(3)/OCTSTR[0]");


    /**
     * Builds a new result from a buffer containing a DER encoding of the result.
     *
     * @param  buffer  to decode
     *
     * @return  New result object.
     */
    public T build(final DERBuffer buffer) {
      final DERParser parser = new DERParser();
      parser.registerHandler(MessageIDHandler.PATH, new MessageIDHandler<>(this));
      parser.registerHandler(RESULT_CODE_PATH, new ResultCodeHandler<>(this));
      parser.registerHandler(MATCHED_DN_PATH, new MatchedDNHandler<>(this));
      parser.registerHandler(DIAGNOSTIC_MESSAGE_PATH, new DiagnosticMessageHandler<>(this));
      parser.registerHandler(REFERRAL_PATH, new ReferralHandler<>(this));
      parser.registerHandler(ControlsHandler.PATH, new ControlsHandler<>(this));
      registerHandlers(parser);
      parser.parse(buffer);
      return build();
    }


    /**
     * @return  New result object.
     */
    public abstract T build();


    /**
     * Template method that may be overridden to register additional parse handlers with this builder.
     *
     * @param parser DER parser
     */
    public void registerHandlers(final DERParser parser) {}
  }


  /** Parse handler implementation for the LDAP result code. */
  protected static class ResultCodeHandler<T extends Result, B extends Result.Builder<T>>
          extends AbstractParseHandler<T, B>
  {
    /**
     * Creates a new LDAP result code handler.
     *
     * @param  builder  Result builder.
     */
    public ResultCodeHandler(final B builder)
    {
      super(builder);
    }


    @Override
    public void handle(final DERParser parser, final DERBuffer encoded)
    {
      getBuilder().resultCode(ResultCode.valueOf(IntegerType.decodeUnsignedPrimitive(encoded)));
    }
  }


  /** Parse handler implementation for the LDAP matched DN. */
  protected static class MatchedDNHandler<T extends Result, B extends Result.Builder<T>>
          extends AbstractParseHandler<T, B>
  {
    /**
     * Creates a new LDAP matched DN handler.
     *
     * @param  builder  Result builder
     */
    public MatchedDNHandler(final B builder)
    {
      super(builder);
    }


    @Override
    public void handle(final DERParser parser, final DERBuffer encoded)
    {
      getBuilder().matchedDN(OctetStringType.decode(encoded));
    }
  }


  /** Parse handler implementation for the LDAP diagnostic message. */
  protected static class DiagnosticMessageHandler<T extends Result, B extends Result.Builder<T>>
          extends AbstractParseHandler<T, B>
  {
    /**
     * Creates a new LDAP diagnostic message handler.
     *
     * @param  builder  Response builder.
     */
    public DiagnosticMessageHandler(final B builder)
    {
      super(builder);
    }


    @Override
    public void handle(final DERParser parser, final DERBuffer encoded)
    {
      getBuilder().diagnosticMessage(OctetStringType.decode(encoded));
    }
  }


  /** Parse handler implementation for the LDAP referral. */
  protected static class ReferralHandler<T extends Result, B extends Result.Builder<T>>
          extends AbstractParseHandler<T, B>
  {
    /**
     * Creates a new LDAP referral handler.
     *
     * @param  builder  Response builder
     */
    public ReferralHandler(final B builder)
    {
      super(builder);
    }


    @Override
    public void handle(final DERParser parser, final DERBuffer encoded)
    {
      getBuilder().referralURLs(OctetStringType.decode(encoded));
    }
  }
}
