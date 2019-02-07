/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.nio.charset.StandardCharsets;

/**
 * Provides convenience methods for converting the various types of passwords into a byte array.
 *
 * @author  Middleware Services
 */
public class Credential
{

  /** Credential stored as a byte array. */
  private final byte[] bytes;


  /**
   * Creates a new credential.
   *
   * @param  password  converted from UTF-8 to a byte array
   */
  public Credential(final String password)
  {
    bytes = password.getBytes(StandardCharsets.UTF_8);
  }


  /**
   * Creates a new credential.
   *
   * @param  password  converted from UTF-8 to a byte array
   */
  public Credential(final char[] password)
  {
    bytes = new String(password).getBytes(StandardCharsets.UTF_8);
  }


  /**
   * Creates a new credential.
   *
   * @param  password  to store
   */
  public Credential(final byte[] password)
  {
    bytes = password;
  }


  /**
   * Returns this credential as a byte array.
   *
   * @return  credential bytes
   */
  public byte[] getBytes()
  {
    return bytes;
  }


  /**
   * Returns this credential as a string.
   *
   * @return  credential string
   */
  public String getString()
  {
    return new String(bytes, StandardCharsets.UTF_8);
  }


  /**
   * Returns this credential as a character array.
   *
   * @return  credential characters
   */
  public char[] getChars()
  {
    return getString().toCharArray();
  }


  @Override
  public String toString()
  {
    return new StringBuilder("[").append(
      getClass().getName()).append("@").append(hashCode()).append("::")
      .append("bytes=").append(new String(bytes, StandardCharsets.UTF_8)).append("]").toString();
  }
}
