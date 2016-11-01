/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.asn1;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import org.ldaptive.LdapUtils;

/**
 * An OID representing the type of attribute and its value.
 *
 * @author  Middleware Services
 */
public class AttributeValueAssertion extends AbstractDERType implements DEREncoder
{

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 613;

  /** OID of the attribute. */
  private final String attributeOid;

  /** Tag and value of the attribute. */
  private final Value attributeValue;


  /**
   * Creates a new attribute value assertion.
   *
   * @param  oid  describing the attribute value
   * @param  value  of the attribute
   */
  public AttributeValueAssertion(final String oid, final Value value)
  {
    super(value.getTag());
    attributeOid = oid;
    attributeValue = value;
  }


  /**
   * Returns the OID.
   *
   * @return  oid
   */
  public String getOid()
  {
    return attributeOid;
  }


  /**
   * Returns the tag and value of the attribute.
   *
   * @return  attribute value
   */
  public Value getValue()
  {
    return attributeValue;
  }


  @Override
  public byte[] encode()
  {
    final ConstructedDEREncoder se = new ConstructedDEREncoder(
      UniversalDERTag.SEQ,
      new DEREncoder() {
        @Override
        public byte[] encode()
        {
          final OidType type = new OidType(attributeOid);
          return type.encode();
        }
      },
      new DEREncoder() {
        @Override
        public byte[] encode()
        {
          return AttributeValueAssertion.this.encode(attributeValue.getBytes());
        }
      });

    return se.encode();
  }


  /**
   * Converts bytes in the buffer to attribute value assertions by reading from the current position to the limit.
   *
   * @param  encoded  buffer containing DER-encoded data where the buffer is positioned at the tag of the oid and the
   *                  limit is set beyond the last byte of attribute value data.
   *
   * @return  decoded bytes as attribute value assertions
   */
  public static AttributeValueAssertion[] decode(final ByteBuffer encoded)
  {
    final List<AttributeValueAssertion> assertions = new ArrayList<>();
    final DERParser parser = new DERParser();
    parser.registerHandler(
      "/SEQ",
      new ParseHandler() {
        @Override
        public void handle(final DERParser parser, final ByteBuffer encoded)
        {
          if (UniversalDERTag.OID.getTagNo() != parser.readTag(encoded).getTagNo()) {
            throw new IllegalArgumentException("Expected OID tag");
          }

          final int seqLimit = encoded.limit();
          final int oidLength = parser.readLength(encoded);
          encoded.limit(encoded.position() + oidLength);

          final String oid = OidType.decode(encoded);
          encoded.limit(seqLimit);

          final DERTag tag = parser.readTag(encoded);
          parser.readLength(encoded);
          assertions.add(new AttributeValueAssertion(oid, new Value(tag, readBuffer(encoded))));
        }
      });
    parser.parse(encoded);
    return assertions.toArray(new AttributeValueAssertion[assertions.size()]);
  }


  @Override
  public boolean equals(final Object o)
  {
    if (o == this) {
      return true;
    }
    if (o instanceof AttributeValueAssertion) {
      final AttributeValueAssertion v = (AttributeValueAssertion) o;
      return LdapUtils.areEqual(attributeOid, v.attributeOid) &&
             LdapUtils.areEqual(attributeValue, v.attributeValue);
    }
    return false;
  }


  @Override
  public int hashCode()
  {
    return LdapUtils.computeHashCode(HASH_CODE_SEED, attributeOid, attributeValue);
  }


  @Override
  public String toString()
  {
    return
      String.format(
        "[%s@%d::attributeOid=%s, attributeValue=%s]",
        getClass().getName(),
        hashCode(),
        attributeOid,
        attributeValue);
  }


  /** Class to represent the tag and value. */
  public static class Value
  {

    /** hash code seed. */
    private static final int HASH_CODE_SEED = 617;

    /** attribute value tag. */
    private final DERTag attributeValueTag;

    /** attribute value bytes. */
    private final byte[] attributeValueBytes;


    /**
     * Creates a new value.
     *
     * @param  tag  type of the attribute value
     * @param  bytes  of the attribute
     */
    public Value(final DERTag tag, final byte[] bytes)
    {
      attributeValueTag = tag;
      attributeValueBytes = bytes;
    }


    /**
     * Returns the attribute value tag.
     *
     * @return  attribute value tag
     */
    public DERTag getTag()
    {
      return attributeValueTag;
    }


    /**
     * Returns the attribute value bytes.
     *
     * @return  attribute value bytes
     */
    public byte[] getBytes()
    {
      return attributeValueBytes;
    }


    @Override
    public boolean equals(final Object o)
    {
      if (o == this) {
        return true;
      }
      if (o instanceof Value) {
        final Value v = (Value) o;
        return LdapUtils.areEqual(attributeValueTag, v.attributeValueTag) &&
               LdapUtils.areEqual(attributeValueBytes, v.attributeValueBytes);
      }
      return false;
    }


    @Override
    public int hashCode()
    {
      return LdapUtils.computeHashCode(HASH_CODE_SEED, attributeValueTag, attributeValueBytes);
    }


    @Override
    public String toString()
    {
      return
        String.format(
          "[%s@%d::attributeValueTag=%s, attributeValueBytes=%s]",
          getClass().getName(),
          hashCode(),
          attributeValueTag,
          LdapUtils.utf8Encode(attributeValueBytes));
    }
  }
}
