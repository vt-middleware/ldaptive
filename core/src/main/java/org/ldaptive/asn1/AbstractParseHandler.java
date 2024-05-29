/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.asn1;

import org.ldaptive.Message;

/**
 * Parse handler for managing and initializing an object.
 *
 * @param  <T>  type of object initialized by this handler
 *
 * @author  Middleware Services
 */
public abstract class AbstractParseHandler<T extends Message, B extends Message.Builder<T>> implements ParseHandler
{

  /** Builder that will produce a message object of type T. */
  private final B builder;


  /**
   * Creates a new abstract parse handler.
   *
   * @param  builder  Produces the message.
   */
  public AbstractParseHandler(final B builder)
  {
    this.builder = builder;
  }


  /**
   * @return  Message builder that is building the message from DER-encoded data.
   */
  public B getBuilder()
  {
    return builder;
  }


  /**
   * @return  Built message object.
   */
  public T getMessage()
  {
    return builder.build();
  }
}
