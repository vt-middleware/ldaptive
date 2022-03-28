/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

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
    if (password == null) {
      throw new NullPointerException("Password cannot be null");
    }
    bytes = LdapUtils.utf8Encode(password, false);
  }


  /**
   * Creates a new credential.
   *
   * @param  password  converted from UTF-8 to a byte array
   */
  public Credential(final char[] password)
  {
    if (password == null) {
      throw new NullPointerException("Password cannot be null");
    }
    bytes = LdapUtils.utf8Encode(new String(password), false);
  }


  /**
   * Creates a new credential.
   *
   * @param  password  to store
   */
  public Credential(final byte[] password)
  {
    if (password == null) {
      throw new NullPointerException("Password cannot be null");
    }
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
    return LdapUtils.utf8Encode(bytes, false);
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
      .append("bytes=").append(LdapUtils.utf8Encode(bytes)).append("]").toString();
  }
}
