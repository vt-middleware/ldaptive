/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.asn1;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Encodes constructed types to their DER format.
 *
 * @author  Middleware Services
 */
public class ConstructedDEREncoder extends AbstractDERType implements DEREncoder
{

  /** Encoders in this sequence. */
  private final DEREncoder[] derEncoders;


  /**
   * Creates a new sequence encoder.
   *
   * @param  tag  der tag associated with this type
   * @param  encoders  to encode in this sequence
   */
  public ConstructedDEREncoder(final DERTag tag, final DEREncoder... encoders)
  {
    super(tag);
    if (!tag.isConstructed()) {
      throw new IllegalArgumentException("DER tag must be constructed");
    }
    if (encoders == null || encoders.length == 0) {
      throw new IllegalArgumentException("Encoders cannot be null or empty");
    }
    derEncoders = encoders;
  }


  @Override
  public byte[] encode()
  {
    final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    try {
      try (bytes) {
        for (DEREncoder encoder : derEncoders) {
          bytes.write(encoder.encode());
        }
      }
    } catch (IOException e) {
      throw new IllegalStateException("Encode failed", e);
    }
    return encode(bytes.toByteArray());
  }
}
