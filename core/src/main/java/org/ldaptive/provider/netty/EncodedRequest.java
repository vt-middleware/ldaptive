/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.provider.netty;

import org.ldaptive.LdapUtils;
import org.ldaptive.Request;

/**
 * Wrapper object that stores an encoded request with it's message ID.
 *
 * @author  Middleware Services
 */
public class EncodedRequest
{

  /** Protocol message ID. */
  private final int messageID;

  /** Encoded request. */
  private final byte[] encoded;


  /**
   * Creates a new encoded request.
   *
   * @param  id  message ID
   * @param  request  to encode
   */
  public EncodedRequest(final int id, final Request request)
  {
    messageID = id;
    encoded = request.encode(messageID);
  }


  /**
   * Returns the message ID.
   *
   * @return  message ID
   */
  public int getMessageID()
  {
    return messageID;
  }


  /**
   * Returns the encoded request.
   *
   * @return  encoded request.
   */
  public byte[] getEncoded()
  {
    return encoded;
  }


  @Override
  public String toString()
  {
    return new StringBuilder(
      getClass().getName()).append("@").append(hashCode()).append("::")
      .append("messageID=").append(messageID).append(", ")
      .append("encoded=").append(LdapUtils.hexEncode(encoded)).toString();
  }
}
