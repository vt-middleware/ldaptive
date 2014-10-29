/*
  $Id: CustomObject.java 3012 2014-07-02 15:23:50Z dfisher $

  Copyright (C) 2003-2014 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 3012 $
  Updated: $Date: 2014-07-02 11:23:50 -0400 (Wed, 02 Jul 2014) $
*/
package org.ldaptive.beans.reflect;

import org.ldaptive.io.ValueTranscoder;

/**
 * Interface for testing bean annotations.
 *
 * @author  Middleware Services
 * @version  $Revision: 3012 $ $Date: 2014-07-02 11:23:50 -0400 (Wed, 02 Jul 2014) $
 */
public interface CustomObject
{


  /** Prepare this object for use; */
  void initialize();


  /** Transcoder that adds 'prefix-' to string values. */
  static class PrefixStringValueTranscoder implements ValueTranscoder<String>
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
