/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.pool;

import org.ldaptive.CompareRequest;
import org.ldaptive.CompareResponse;
import org.ldaptive.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Validates a connection is healthy by performing a compare operation.
 *
 * @author  Middleware Services
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
    compareRequest = new CompareRequest("", "objectClass", "top");
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


  @Override
  public boolean validate(final Connection c)
  {
    if (c != null) {
      try {
        final CompareResponse response = c.operation(compareRequest).execute();
        return response.isTrue();
      } catch (Exception e) {
        logger.debug("validation failed for compare request {}", compareRequest, e);
      }
    }
    return false;
  }


  @Override
  public String toString()
  {
    return new StringBuilder("[").append(
      getClass().getName()).append("@").append(hashCode()).append("::")
      .append("compareRequest=").append(compareRequest).append("]").toString();
  }
}
