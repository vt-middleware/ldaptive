/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.control;

import java.util.ArrayList;
import java.util.List;
import org.ldaptive.LdapUtils;
import org.ldaptive.asn1.AbstractParseHandler;
import org.ldaptive.asn1.ConstructedDEREncoder;
import org.ldaptive.asn1.DERBuffer;
import org.ldaptive.asn1.DEREncoder;
import org.ldaptive.asn1.DERParser;
import org.ldaptive.asn1.DERPath;
import org.ldaptive.asn1.OctetStringType;
import org.ldaptive.asn1.UniversalDERTag;

/**
 * Request/response control for session tracking. See https://tools.ietf.org/html/draft-wahl-ldap-session-03.
 * Control is defined as:
 *
 * <pre>
   LDAPString ::= OCTET STRING -- UTF-8 encoded
   LDAPOID ::= OCTET STRING -- Constrained to numericoid

   SessionIdentifierControlValue ::= SEQUENCE {
   sessionSourceIp                 LDAPString,
   sessionSourceName               LDAPString,
   formatOID                       LDAPOID,
   sessionTrackingIdentifier       LDAPString
   }
 * </pre>
 *
 * Note that criticality must be either false or absent.
 *
 * @author  Middleware Services
 */
public class SessionTrackingControl extends AbstractControl implements RequestControl, ResponseControl
{

  /** OID of this control. */
  public static final String OID = "1.3.6.1.4.1.21008.108.63.1";

  /** OID for the Acct-Session-Id RADIUS attribute format. */
  public static final String RADIUS_ACCT_OID = "1.3.6.1.4.1.21008.108.63.1.1";

  /** OID for the Acct-Multi-Session-Id RADIUS attribute format. */
  public static final String RADIUS_ACCT_MULTI_OID = "1.3.6.1.4.1.21008.108.63.1.2";

  /** OID for the SASL authorization identity string format. */
  public static final String USERNAME_ACCT_OID = "1.3.6.1.4.1.21008.108.63.1.3";

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 7027;

  /** Session source ip. */
  private String sessionSourceIp;

  /** Session source name. */
  private String sessionSourceName;

  /** Format OID. */
  private String formatOID;

  /** Session tracking identifier. */
  private String sessionTrackingIdentifier;


  /** Default constructor. */
  public SessionTrackingControl()
  {
    super(OID);
  }


  /**
   * Creates a new session tracking control.
   *
   * @param  critical  whether this control is critical
   */
  public SessionTrackingControl(final boolean critical)
  {
    super(OID, critical);
  }


  /**
   * Creates a new session tracking control.
   *
   * @param  sourceIP  session source ip
   * @param  sourceName  session source name
   * @param  oid  format OID
   * @param  trackingIdentifier  session tracking identifier
   */
  public SessionTrackingControl(
    final String sourceIP,
    final String sourceName,
    final String oid,
    final String trackingIdentifier)
  {
    this(sourceIP, sourceName, oid, trackingIdentifier, false);
  }


  /**
   * Creates a new session tracking control.
   *
   * @param  sourceIP  session source ip
   * @param  sourceName  session source name
   * @param  oid  format OID
   * @param  trackingIdentifier  session tracking identifier
   * @param  critical  whether this control is critical
   */
  public SessionTrackingControl(
    final String sourceIP,
    final String sourceName,
    final String oid,
    final String trackingIdentifier,
    final boolean critical)
  {
    super(OID, critical);
    setSessionSourceIp(sourceIP);
    setSessionSourceName(sourceName);
    setFormatOID(oid);
    setSessionTrackingIdentifier(trackingIdentifier);
  }


  @Override
  public boolean hasValue()
  {
    return true;
  }


  /**
   * Returns the session source ip.
   *
   * @return  session source ip
   */
  public String getSessionSourceIp()
  {
    return sessionSourceIp;
  }


  /**
   * Sets the session source ip.
   *
   * @param  s  session source ip
   */
  public void setSessionSourceIp(final String s)
  {
    sessionSourceIp = s;
  }


  /**
   * Returns the session source name.
   *
   * @return  session source name
   */
  public String getSessionSourceName()
  {
    return sessionSourceName;
  }


  /**
   * Sets the session source name.
   *
   * @param  s  session source name
   */
  public void setSessionSourceName(final String s)
  {
    sessionSourceName = s;
  }


  /**
   * Returns the format OID.
   *
   * @return  format OID
   */
  public String getFormatOID()
  {
    return formatOID;
  }


  /**
   * Sets the format OID.
   *
   * @param  s  format OID
   */
  public void setFormatOID(final String s)
  {
    formatOID = s;
  }


  /**
   * Returns the session tracking identifier.
   *
   * @return  session tracking identifier
   */
  public String getSessionTrackingIdentifier()
  {
    return sessionTrackingIdentifier;
  }


  /**
   * Sets the session tracking identifier.
   *
   * @param  s  session tracking identifier
   */
  public void setSessionTrackingIdentifier(final String s)
  {
    sessionTrackingIdentifier = s;
  }


  @Override
  public boolean equals(final Object o)
  {
    if (o == this) {
      return true;
    }
    if (o instanceof SessionTrackingControl && super.equals(o)) {
      final SessionTrackingControl v = (SessionTrackingControl) o;
      return LdapUtils.areEqual(sessionSourceIp, v.sessionSourceIp) &&
        LdapUtils.areEqual(sessionSourceName, v.sessionSourceName) &&
        LdapUtils.areEqual(formatOID, v.formatOID) &&
        LdapUtils.areEqual(sessionTrackingIdentifier, v.sessionTrackingIdentifier);
    }
    return false;
  }


  @Override
  public int hashCode()
  {
    return
      LdapUtils.computeHashCode(
        HASH_CODE_SEED,
        getOID(),
        getCriticality(),
        sessionSourceIp,
        sessionSourceName,
        formatOID,
        sessionTrackingIdentifier);
  }


  @Override
  public String toString()
  {
    return new StringBuilder("[").append(
      getClass().getName()).append("@").append(hashCode()).append("::")
      .append("criticality=").append(getCriticality()).append(", ")
      .append("sessionSourceIp=").append(sessionSourceIp).append(", ")
      .append("sessionSourceName=").append(sessionSourceName).append(", ")
      .append("formatOID=").append(formatOID).append(", ")
      .append("sessionTrackingIdentifier=").append(sessionTrackingIdentifier).append("]").toString();
  }


  @Override
  public byte[] encode()
  {
    final List<DEREncoder> l = new ArrayList<>();
    l.add(new OctetStringType(getSessionSourceIp()));
    l.add(new OctetStringType(getSessionSourceName()));
    l.add(new OctetStringType(getFormatOID()));
    l.add(new OctetStringType(getSessionTrackingIdentifier()));

    final ConstructedDEREncoder se = new ConstructedDEREncoder(UniversalDERTag.SEQ, l.toArray(new DEREncoder[0]));
    return se.encode();
  }


  @Override
  public void decode(final DERBuffer encoded)
  {
    final DERParser parser = new DERParser();
    parser.registerHandler(SourceIpHandler.PATH, new SourceIpHandler(this));
    parser.registerHandler(SourceNameHandler.PATH, new SourceNameHandler(this));
    parser.registerHandler(FormatOIDHandler.PATH, new FormatOIDHandler(this));
    parser.registerHandler(TrackingIdentifierHandler.PATH, new TrackingIdentifierHandler(this));
    parser.parse(encoded);
  }


  /** Parse handler implementation for the source ip. */
  private static class SourceIpHandler extends AbstractParseHandler<SessionTrackingControl>
  {

    /** DER path to source ip value. */
    public static final DERPath PATH = new DERPath("/SEQ/OCTSTR[0]");


    /**
     * Creates a new source ip handler.
     *
     * @param  control  to configure
     */
    SourceIpHandler(final SessionTrackingControl control)
    {
      super(control);
    }


    @Override
    public void handle(final DERParser parser, final DERBuffer encoded)
    {
      getObject().setSessionSourceIp(OctetStringType.decode(encoded));
    }
  }


  /** Parse handler implementation for the source name. */
  private static class SourceNameHandler extends AbstractParseHandler<SessionTrackingControl>
  {

    /** DER path to source name value. */
    public static final DERPath PATH = new DERPath("/SEQ/OCTSTR[1]");


    /**
     * Creates a new source name handler.
     *
     * @param  control  to configure
     */
    SourceNameHandler(final SessionTrackingControl control)
    {
      super(control);
    }


    @Override
    public void handle(final DERParser parser, final DERBuffer encoded)
    {
      getObject().setSessionSourceName(OctetStringType.decode(encoded));
    }
  }


  /** Parse handler implementation for the format oid. */
  private static class FormatOIDHandler extends AbstractParseHandler<SessionTrackingControl>
  {

    /** DER path to format oid value. */
    public static final DERPath PATH = new DERPath("/SEQ/OCTSTR[2]");


    /**
     * Creates a new format oid handler.
     *
     * @param  control  to configure
     */
    FormatOIDHandler(final SessionTrackingControl control)
    {
      super(control);
    }


    @Override
    public void handle(final DERParser parser, final DERBuffer encoded)
    {
      getObject().setFormatOID(OctetStringType.decode(encoded));
    }
  }


  /** Parse handler implementation for the tracking identifier. */
  private static class TrackingIdentifierHandler extends AbstractParseHandler<SessionTrackingControl>
  {

    /** DER path to source name value. */
    public static final DERPath PATH = new DERPath("/SEQ/OCTSTR[3]");


    /**
     * Creates a new tracking identifier handler.
     *
     * @param  control  to configure
     */
    TrackingIdentifierHandler(final SessionTrackingControl control)
    {
      super(control);
    }


    @Override
    public void handle(final DERParser parser, final DERBuffer encoded)
    {
      getObject().setSessionTrackingIdentifier(OctetStringType.decode(encoded));
    }
  }
}
