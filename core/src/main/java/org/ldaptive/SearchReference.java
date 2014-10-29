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
package org.ldaptive;

import java.util.Arrays;
import java.util.Collection;
import org.ldaptive.control.ResponseControl;

/**
 * Simple bean representing a search reference.
 *
 * @author  Middleware Services
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
 */
public class SearchReference implements ResponseMessage
{

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 349;

  /** referral urls. */
  private final String[] referralUrls;

  /** response controls. */
  private final ResponseControl[] responseControls;

  /** message ID. */
  private final int messageId;


  /**
   * Creates a search reference.
   *
   * @param  id  message id
   * @param  c  response controls
   * @param  url  referral urls
   */
  public SearchReference(
    final int id,
    final ResponseControl[] c,
    final String... url)
  {
    messageId = id;
    responseControls = c;
    referralUrls = url;
  }


  /**
   * Creates a search reference.
   *
   * @param  id  message id
   * @param  c  response controls
   * @param  urls  referral urls
   */
  public SearchReference(
    final int id,
    final ResponseControl[] c,
    final Collection<String> urls)
  {
    messageId = id;
    responseControls = c;
    referralUrls = urls.toArray(new String[urls.size()]);
  }


  /**
   * Returns the referral urls for this search reference.
   *
   * @return  referral urls
   */
  public String[] getReferralUrls()
  {
    return referralUrls;
  }


  /** {@inheritDoc} */
  @Override
  public ResponseControl[] getControls()
  {
    return responseControls;
  }


  /** {@inheritDoc} */
  @Override
  public ResponseControl getControl(final String oid)
  {
    if (getControls() != null) {
      for (ResponseControl c : getControls()) {
        if (c.getOID().equals(oid)) {
          return c;
        }
      }
    }
    return null;
  }


  /** {@inheritDoc} */
  @Override
  public int getMessageId()
  {
    return messageId;
  }


  /** {@inheritDoc} */
  @Override
  public int hashCode()
  {
    return
      LdapUtils.computeHashCode(
        HASH_CODE_SEED,
        referralUrls,
        responseControls,
        messageId);
  }


  /** {@inheritDoc} */
  @Override
  public String toString()
  {
    return
      String.format(
        "[%s@%d::referralUrls=%s, responseControls=%s, messageId=%s]",
        getClass().getName(),
        hashCode(),
        Arrays.toString(referralUrls),
        Arrays.toString(responseControls),
        messageId);
  }
}
