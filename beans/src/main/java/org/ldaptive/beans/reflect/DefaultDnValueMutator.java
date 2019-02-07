/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.beans.reflect;

import java.util.Collection;
import java.util.Collections;
import org.ldaptive.beans.AttributeValueMutator;
import org.ldaptive.beans.DnValueMutator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Uses a {@link AttributeValueMutator} to mutate the configured DN of an object.
 *
 * @author  Middleware Services
 */
public class DefaultDnValueMutator implements DnValueMutator
{

  /** Logger for this class. */
  protected final Logger logger = LoggerFactory.getLogger(getClass());

  /** Mutator for the DN. */
  private final AttributeValueMutator dnMutator;


  /**
   * Creates a new default dn value mutator.
   *
   * @param  mutator  for the DN
   */
  public DefaultDnValueMutator(final AttributeValueMutator mutator)
  {
    dnMutator = mutator;
  }


  @Override
  public String getValue(final Object object)
  {
    final Collection<String> c = dnMutator.getStringValues(object);
    if (c != null && !c.isEmpty()) {
      return c.iterator().next();
    }
    return null;
  }


  @Override
  public void setValue(final Object object, final String value)
  {
    dnMutator.setStringValues(object, Collections.singletonList(value));
  }


  @Override
  public String toString()
  {
    return new StringBuilder("[").append(
      getClass().getName()).append("@").append(hashCode()).append("::")
      .append("dnMutator=").append(dnMutator).append("]").toString();
  }
}
