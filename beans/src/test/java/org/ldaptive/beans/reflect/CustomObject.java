/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.beans.reflect;

import org.ldaptive.transcode.ValueTranscoder;

/**
 * Interface for testing bean annotations.
 *
 * @author  Middleware Services
 */
public interface CustomObject
{


  /** Prepare this object for use. */
  void initialize();


  /** Transcoder that adds 'prefix-' to string values. */
  class PrefixStringValueTranscoder implements ValueTranscoder<String>
  {

    /** Default constructor. */
    public PrefixStringValueTranscoder() {}


    /**
     * Constructor with a non string param.
     *
     * @param  i  ignored argument
     */
    public PrefixStringValueTranscoder(final int i) {}


    @Override
    public String decodeStringValue(final String value)
    {
      return value.replaceFirst("prefix-", "");
    }

    @Override
    public String decodeBinaryValue(final byte[] value)
    {
      throw new UnsupportedOperationException();
    }

    @Override
    public String encodeStringValue(final String value)
    {
      return String.format("prefix-%s", value);
    }

    @Override
    public byte[] encodeBinaryValue(final String value)
    {
      throw new UnsupportedOperationException();
    }

    @Override
    public Class<String> getType()
    {
      return String.class;
    }
  }
}
