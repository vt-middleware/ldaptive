/*
  $Id: DefaultDnValueMutator.java 3013 2014-07-02 15:26:52Z dfisher $

  Copyright (C) 2003-2014 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 3013 $
  Updated: $Date: 2014-07-02 11:26:52 -0400 (Wed, 02 Jul 2014) $
*/
package org.ldaptive.beans.reflect;

import java.util.Arrays;
import java.util.Collection;
import org.ldaptive.beans.AttributeValueMutator;
import org.ldaptive.beans.DnValueMutator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Uses a {@link AttributeValueMutator} to mutate the configured DN of an
 * object.
 *
 * @author  Middleware Services
 * @version  $Revision: 3013 $ $Date: 2014-07-02 11:26:52 -0400 (Wed, 02 Jul 2014) $
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


  /** {@inheritDoc} */
  @Override
  public String getValue(final Object object)
  {
    final Collection<String> c = dnMutator.getStringValues(object);
    if (c != null && !c.isEmpty()) {
      return c.iterator().next();
    }
    return null;
  }


  /** {@inheritDoc} */
  @Override
  public void setValue(final Object object, final String value)
  {
    dnMutator.setStringValues(object, Arrays.asList(value));
  }


  /** {@inheritDoc} */
  @Override
  public String toString()
  {
    return
      String.format(
        "[%s@%d::dnMutator=%s]",
        getClass().getName(),
        hashCode(),
        dnMutator);
  }
}
