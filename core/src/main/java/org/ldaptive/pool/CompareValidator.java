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
package org.ldaptive.pool;

import org.ldaptive.CompareOperation;
import org.ldaptive.CompareRequest;
import org.ldaptive.Connection;
import org.ldaptive.LdapAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Validates a connection is healthy by performing a compare operation.
 *
 * @author  Middleware Services
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
 */
public class CompareValidator implements Validator<Connection>
{

  /** Logger for this class. */
  protected final Logger logger = LoggerFactory.getLogger(getClass());

  /** Compare request to perform validation with. */
  private CompareRequest compareRequest;


  /** Creates a new compare validator. */
  public CompareValidator()
  {
    compareRequest = new CompareRequest();
    compareRequest.setDn("");
    compareRequest.setAttribute(new LdapAttribute("objectClass", "top"));
  }


  /**
   * Creates a new compare validator.
   *
   * @param  cr  to use for compares
   */
  public CompareValidator(final CompareRequest cr)
  {
    compareRequest = cr;
  }


  /**
   * Returns the compare request.
   *
   * @return  compare request
   */
  public CompareRequest getCompareRequest()
  {
    return compareRequest;
  }


  /**
   * Sets the compare request.
   *
   * @param  cr  compare request
   */
  public void setCompareRequest(final CompareRequest cr)
  {
    compareRequest = cr;
  }


  /** {@inheritDoc} */
  @Override
  public boolean validate(final Connection c)
  {
    boolean success = false;
    if (c != null) {
      try {
        final CompareOperation compare = new CompareOperation(c);
        success = compare.execute(compareRequest).getResult();
      } catch (Exception e) {
        logger.debug(
          "validation failed for compare request {}",
          compareRequest,
          e);
      }
    }
    return success;
  }


  /** {@inheritDoc} */
  @Override
  public String toString()
  {
    return
      String.format(
        "[%s@%d::compareRequest=%s]",
        getClass().getName(),
        hashCode(),
        compareRequest);
  }
}
