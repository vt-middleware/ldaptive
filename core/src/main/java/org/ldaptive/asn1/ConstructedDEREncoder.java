/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.asn1;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Encodes constructed types to their DER format.
 *
 * @author  Middleware Services
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
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


  /** {@inheritDoc} */
  @Override
  public byte[] encode()
  {
    final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    try {
      try {
        for (DEREncoder encoder : derEncoders) {
          bytes.write(encoder.encode());
        }
      } finally {
        bytes.close();
      }
    } catch (IOException e) {
      throw new IllegalStateException("Encode failed", e);
    }
    return encode(bytes.toByteArray());
  }
}
