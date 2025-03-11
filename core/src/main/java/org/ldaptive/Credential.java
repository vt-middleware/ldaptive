/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

/**
 * Provides convenience methods for converting the various types of passwords into a byte array.
 *
 * @author  Middleware Services
 */
public final class Credential
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
    LdapUtils.assertNotNullArg(password, "Password cannot be null");
    bytes = LdapUtils.utf8Encode(password, false);
  }


  /**
   * Creates a new credential.
   *
   * @param  password  converted from UTF-8 to a byte array
   */
  public Credential(final char[] password)
  {
    LdapUtils.assertNotNullArg(password, "Password cannot be null");
    bytes = LdapUtils.utf8Encode(password, false);
  }


  /**
   * Creates a new credential.
   *
   * @param  password  to store
   */
  public Credential(final byte[] password)
  {
    LdapUtils.assertNotNullArg(password, "Password cannot be null");
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
   * Returns whether the underlying byte array is null.
   *
   * @return  whether the underlying byte array is null
   */
  public boolean isNull()
  {
    return bytes == null;
  }


  /**
   * Returns whether the underlying byte array is empty.
   *
   * @return  whether the underlying byte array is empty
   */
  public boolean isEmpty()
  {
    return bytes.length == 0;
  }


  @Override
  public String toString()
  {
    return "[" + getClass().getName() + "@" + hashCode() + "::" + "bytes=" + LdapUtils.utf8Encode(bytes) + "]";
  }


  /**
   * Returns a credential initialized with the supplied credential.
   *
   * @param  credential  to read properties from
   *
   * @return  credential
   */
  public static Credential copy(final Credential credential)
  {
    return new Credential(LdapUtils.copyArray(credential.bytes));
  }
}
