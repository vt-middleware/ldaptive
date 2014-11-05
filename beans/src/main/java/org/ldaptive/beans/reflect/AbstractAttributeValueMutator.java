/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.beans.reflect;

import org.ldaptive.SortBehavior;
import org.ldaptive.beans.AttributeValueMutator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base implementation of a {@link AttributeValueMutator}. Uses a {@link
 * ReflectionTranscoder} for mutating values.
 *
 * @author  Middleware Services
 */
public abstract class AbstractAttributeValueMutator
  implements AttributeValueMutator
{

  /** Logger for this class. */
  protected final Logger logger = LoggerFactory.getLogger(getClass());

  /** Name of the attribute. */
  private final String attributeName;

  /** Whether this attribute is binary. */
  private final boolean attributeBinary;

  /** Sort behavior of this attribute. */
  private final SortBehavior attributeSortBehavior;

  /** Transcoder for modifying this attribute. */
  private final ReflectionTranscoder valueTranscoder;


  /**
   * Creates a new abstract attribute value mutator.
   *
   * @param  name  of the attribute
   * @param  binary  whether this attribute is binary
   * @param  sortBehavior  how to sort this attribute
   * @param  transcoder  for mutating the attribute
   */
  public AbstractAttributeValueMutator(
    final String name,
    final boolean binary,
    final SortBehavior sortBehavior,
    final ReflectionTranscoder transcoder)
  {
    attributeName = name;
    attributeBinary = binary;
    attributeSortBehavior = sortBehavior;
    valueTranscoder = transcoder;
  }


  /** {@inheritDoc} */
  @Override
  public String getName()
  {
    return attributeName;
  }


  /** {@inheritDoc} */
  @Override
  public boolean isBinary()
  {
    return attributeBinary;
  }


  /** {@inheritDoc} */
  @Override
  public SortBehavior getSortBehavior()
  {
    return attributeSortBehavior;
  }


  /**
   * Returns the reflection transcoder.
   *
   * @return  reflection transcoder
   */
  protected ReflectionTranscoder getReflectionTranscoder()
  {
    return valueTranscoder;
  }
}
