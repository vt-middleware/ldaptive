/*
  $Id$

  Copyright (C) 2003-2014 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision$
  Updated: $Date$
*/
package org.ldaptive.extended;

import java.util.Arrays;
import org.ldaptive.AbstractRequest;
import org.ldaptive.asn1.ConstructedDEREncoder;
import org.ldaptive.asn1.IntegerType;
import org.ldaptive.asn1.UniversalDERTag;

/**
 * Contains the data required to perform an ldap cancel operation. See RFC 3909.
 *
 * @author  Middleware Services
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
 */
public class CancelRequest extends AbstractRequest implements ExtendedRequest
{

  /** OID of this extended request. */
  public static final String OID = "1.3.6.1.1.8";

  /** message id to cancel. */
  private int messageId;


  /** Default constructor. */
  public CancelRequest() {}


  /**
   * Creates a new cancel request.
   *
   * @param  id  of the message to cancel
   */
  public CancelRequest(final int id)
  {
    setMessageId(id);
  }


  /**
   * Returns the message id to cancel.
   *
   * @return  message id
   */
  public int getMessageId()
  {
    return messageId;
  }


  /**
   * Sets the message id to cancel.
   *
   * @param  id  of the message to cancel
   */
  public void setMessageId(final int id)
  {
    messageId = id;
  }


  /** {@inheritDoc} */
  @Override
  public byte[] encode()
  {
    final ConstructedDEREncoder se = new ConstructedDEREncoder(
      UniversalDERTag.SEQ,
      new IntegerType(getMessageId()));
    return se.encode();
  }


  /** {@inheritDoc} */
  @Override
  public String getOID()
  {
    return OID;
  }


  /** {@inheritDoc} */
  @Override
  public String toString()
  {
    return
      String.format(
        "[%s@%d::messageId=%s, controls=%s]",
        getClass().getName(),
        hashCode(),
        messageId,
        Arrays.toString(getControls()));
  }
}
