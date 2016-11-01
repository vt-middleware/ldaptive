/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.asn1;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.ldaptive.LdapUtils;
import org.ldaptive.io.ValueTranscoder;

/**
 * A set of attribute value assertions.
 *
 * @author  Middleware Services
 */
public class RDN implements DEREncoder
{

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 619;

  /** Attribute value assertions in this RDN. */
  private final AttributeValueAssertion[] attributeValueAssertions;


  /**
   * Creates a new RDN.
   *
   * @param  assertions  in this RDN
   */
  public RDN(final Collection<AttributeValueAssertion> assertions)
  {
    attributeValueAssertions = assertions.toArray(new AttributeValueAssertion[assertions.size()]);
  }


  /**
   * Creates a new RDN.
   *
   * @param  assertions  in this RDN
   */
  public RDN(final AttributeValueAssertion... assertions)
  {
    attributeValueAssertions = assertions;
  }


  /**
   * Returns the attribute value assertions in this RDN.
   *
   * @return  attribute value assertions
   */
  public AttributeValueAssertion[] getAttributeValueAssertions()
  {
    return attributeValueAssertions;
  }


  /**
   * Returns the attribute values for the supplied oid.
   *
   * @param  oid  to match
   *
   * @return  attribute values
   */
  public AttributeValueAssertion.Value[] getAttributeValues(final String oid)
  {
    final List<AttributeValueAssertion.Value> values = new ArrayList<>();
    for (AttributeValueAssertion type : attributeValueAssertions) {
      if (type.getOid().equals(oid)) {
        values.add(type.getValue());
      }
    }
    return values.toArray(new AttributeValueAssertion.Value[values.size()]);
  }


  /**
   * Returns a single attribute value for the supplied oid. See {@link #getAttributeValues(String)}.
   *
   * @param  oid  to match
   *
   * @return  attribute value
   */
  public AttributeValueAssertion.Value getAttributeValue(final String oid)
  {
    final AttributeValueAssertion.Value[] values = getAttributeValues(oid);
    if (values == null || values.length == 0) {
      return null;
    }
    return values[0];
  }


  /**
   * Returns the attribute values decoded by the supplied transcoder.
   *
   * @param  <T>  type of value
   * @param  oid  to match
   * @param  transcoder  to decode the binary value
   *
   * @return  decoded attribute values
   */
  @SuppressWarnings("unchecked")
  public <T> T[] getAttributeValues(final String oid, final ValueTranscoder<T> transcoder)
  {
    final List<T> values = new ArrayList<>();
    for (AttributeValueAssertion type : attributeValueAssertions) {
      if (type.getOid().equals(oid)) {
        values.add(transcoder.decodeBinaryValue(type.getValue().getBytes()));
      }
    }
    return (T[]) values.toArray();
  }


  /**
   * Returns a single attribute value for the supplied oid. See {@link #getAttributeValues(String, ValueTranscoder)}.
   *
   * @param  <T>  type of value
   * @param  oid  to match
   * @param  transcoder  to decode the binary value
   *
   * @return  decoded attribute value
   */
  public <T> T getAttributeValue(final String oid, final ValueTranscoder<T> transcoder)
  {
    final T[] values = getAttributeValues(oid, transcoder);
    if (values == null || values.length == 0) {
      return null;
    }
    return values[0];
  }


  @Override
  public byte[] encode()
  {
    final List<DEREncoder> typeEncoders = new ArrayList<>();
    for (final AttributeValueAssertion types : attributeValueAssertions) {
      typeEncoders.add(new DEREncoder() {
          @Override
          public byte[] encode()
          {
            return types.encode();
          }
        });
    }

    final ConstructedDEREncoder se = new ConstructedDEREncoder(
      UniversalDERTag.SET,
      typeEncoders.toArray(new DEREncoder[typeEncoders.size()]));
    return se.encode();
  }


  /**
   * Converts bytes in the buffer to RDNs by reading from the current position to the limit.
   *
   * @param  encoded  buffer containing DER-encoded data where the buffer is positioned at the tag of the RDN and the
   *                  limit is set beyond the last byte of the RDN.
   *
   * @return  decoded bytes as RDNs
   */
  public static RDN[] decode(final ByteBuffer encoded)
  {
    final List<RDN> rdns = new ArrayList<>();
    final DERParser parser = new DERParser();
    parser.registerHandler(
      "/SEQ/SET",
      new ParseHandler() {
        @Override
        public void handle(final DERParser parser, final ByteBuffer encoded)
        {
          rdns.add(new RDN(AttributeValueAssertion.decode(encoded.slice())));
          encoded.position(encoded.limit());
        }
      });
    parser.parse(encoded);
    return rdns.toArray(new RDN[rdns.size()]);
  }


  @Override
  public boolean equals(final Object o)
  {
    if (o == this) {
      return true;
    }
    if (o instanceof RDN) {
      final RDN v = (RDN) o;
      return LdapUtils.areEqual(attributeValueAssertions, v.attributeValueAssertions);
    }
    return false;
  }


  @Override
  public int hashCode()
  {
    return LdapUtils.computeHashCode(HASH_CODE_SEED, (Object) attributeValueAssertions);
  }


  @Override
  public String toString()
  {
    return
      String.format(
        "[%s@%d::attributeValueAssertions=%s]",
        getClass().getName(),
        hashCode(),
        Arrays.toString(attributeValueAssertions));
  }
}
