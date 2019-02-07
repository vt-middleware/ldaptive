/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.transcode;

import java.util.function.Function;

/**
 * Interface for decoding and encoding custom types for ldap attribute values.
 *
 * @param  <T>  type of value
 *
 * @author  Middleware Services
 */
public interface ValueTranscoder<T>
{


  /**
   * Decodes the supplied ldap attribute value into a custom type.
   *
   * @param  value  to decode
   *
   * @return  decoded value
   */
  T decodeStringValue(String value);


  /**
   * Decodes the supplied ldap attribute value into a custom type.
   *
   * @param  value  to decode
   *
   * @return  decoded value
   */
  T decodeBinaryValue(byte[] value);


  /**
   * Encodes the supplied value into an ldap attribute value.
   *
   * @param  value  to encode
   *
   * @return  encoded value
   */
  String encodeStringValue(T value);


  /**
   * Encodes the supplied value into an ldap attribute value.
   *
   * @param  value  to encode
   *
   * @return  encoded value
   */
  byte[] encodeBinaryValue(T value);


  /**
   * Returns the type produced by this value transcoder.
   *
   * @return  type produced by this value transcoder
   */
  Class<T> getType();


  /**
   * Functional implementation.
   *
   * @return  decoder function
   */
  default Function<byte[], T> decoder()
  {
    return this::decodeBinaryValue;
  }


  /**
   * Functional implementation.
   *
   * @return  encoder function
   */
  default Function<T, byte[]> encoder()
  {
    return this::encodeBinaryValue;
  }
}
