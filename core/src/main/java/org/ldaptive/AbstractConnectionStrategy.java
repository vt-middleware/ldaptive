/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for connection strategy implementations.
 *
 * @author  Middleware Services
 */
public abstract class AbstractConnectionStrategy implements ConnectionStrategy
{

  /** Logger for this class. */
  protected final Logger logger = LoggerFactory.getLogger(getClass());

  /** URL metadata. */
  private final Map<LdapURL, Map<String, Object>> metadata = new ConcurrentHashMap<>();


  @Override
  public Map<LdapURL, Map<String, Object>> getMetadata()
  {
    return metadata;
  }
}
