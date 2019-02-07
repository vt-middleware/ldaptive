/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.extended;

import org.ldaptive.asn1.AbstractParseHandler;
import org.ldaptive.asn1.DERBuffer;
import org.ldaptive.asn1.DERParser;
import org.ldaptive.asn1.DERPath;
import org.ldaptive.asn1.DefaultDERBuffer;
import org.ldaptive.asn1.OctetStringType;

/**
 * Utility class for parsing the responseValue from a password generation.
 *
 * @author  Middleware Services
 */
public final class PasswordModifyResponseParser
{


  /**
   * Default constructor.
   */
  private PasswordModifyResponseParser() {}


  /**
   * Parse the supplied extended operation response.
   *
   * @param  response  from a password modify extended operation
   *
   * @return  generated password
   */
  public static String parse(final ExtendedResponse response)
  {
    final StringBuilder sb = new StringBuilder();
    final DERParser p = new DERParser();
    p.registerHandler(GenPasswdHandler.PATH, new GenPasswdHandler(sb));
    p.parse(new DefaultDERBuffer(response.getResponseValue()));
    return sb.toString();
  }


  /** Parse handler implementation for the genPasswd. */
  private static class GenPasswdHandler extends AbstractParseHandler<StringBuilder>
  {

    /** DER path to generated password. */
    public static final DERPath PATH = new DERPath("/SEQ/CTX(0)");


    /**
     * Creates a new gen passwd handler.
     *
     * @param  sb  to append to
     */
    GenPasswdHandler(final StringBuilder sb)
    {
      super(sb);
    }


    @Override
    public void handle(final DERParser parser, final DERBuffer encoded)
    {
      getObject().append(OctetStringType.decode(encoded));
    }
  }
}
