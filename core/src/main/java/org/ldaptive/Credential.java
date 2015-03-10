/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.nio.charset.Charset;

/**
 * Provides convenience methods for converting the various types of passwords into a byte array.
 *
 * @author  Middleware Services
 */
public class Credential
{

  /** UTF-8 character set. */
  private static final Charset UTF8_CHARSET = Charset.forName("UTF-8");

  /** Credential stored as a byte array. */
  private final byte[] bytes;


  /**
   * Creates a new credential.
   *
   * @param  password  converted from UTF-8 to a byte array
   */
  public Credential(final String password)
  {
    bytes = password.getBytes(UTF8_CHARSET);
  }


  /**
   * Creates a new credential.
   *
   * @param  password  converted from UTF-8 to a byte array
   */
  public Credential(final char[] password)
  {
    bytes = new String(password).getBytes(UTF8_CHARSET);
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
    return new String(bytes, UTF8_CHARSET);
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
    return String.format("[%s@%d::bytes=%s]", getClass().getName(), hashCode(), new String(bytes, UTF8_CHARSET));
  }
}
