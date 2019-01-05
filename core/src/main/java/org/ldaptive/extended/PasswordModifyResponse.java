/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.extended;

import org.ldaptive.Credential;
import org.ldaptive.asn1.AbstractParseHandler;
import org.ldaptive.asn1.DERBuffer;
import org.ldaptive.asn1.DERParser;
import org.ldaptive.asn1.DERPath;
import org.ldaptive.asn1.OctetStringType;

/**
 * Contains the response from an ldap password modify operation. See RFC 3062. Response is defined as:
 *
 * <pre>
   PasswdModifyResponseValue ::= SEQUENCE {
     genPasswd       [0]     OCTET STRING OPTIONAL }
 * </pre>
 *
 * @author  Middleware Services
 */
public class PasswordModifyResponse extends AbstractExtendedResponse<Credential>
{


  @Override
  public String getOID()
  {
    // RFC defines the response name as absent
    return null;
  }


  @Override
  public void decode(final DERBuffer encoded)
  {
    final DERParser parser = new DERParser();
    parser.registerHandler(GenPasswdHandler.PATH, new GenPasswdHandler(this));
    parser.parse(encoded);
  }


  @Override
  public String toString()
  {
    return String.format("[%s@%d]", getClass().getName(), hashCode());
  }


  /** Parse handler implementation for the genPasswd. */
  private static class GenPasswdHandler extends AbstractParseHandler<PasswordModifyResponse>
  {

    /** DER path to generated password. */
    public static final DERPath PATH = new DERPath("/SEQ/CTX(0)");


    /**
     * Creates a new gen passwd handler.
     *
     * @param  response  to configure
     */
    GenPasswdHandler(final PasswordModifyResponse response)
    {
      super(response);
    }


    @Override
    public void handle(final DERParser parser, final DERBuffer encoded)
    {
      getObject().setValue(new Credential(OctetStringType.decode(encoded)));
    }
  }
}
