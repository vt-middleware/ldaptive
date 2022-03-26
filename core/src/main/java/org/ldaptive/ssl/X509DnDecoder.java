/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.ssl;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import org.ldaptive.asn1.DERBuffer;
import org.ldaptive.asn1.DERParser;
import org.ldaptive.asn1.DERPath;
import org.ldaptive.asn1.OidType;
import org.ldaptive.asn1.UniversalDERTag;
import org.ldaptive.dn.Dn;
import org.ldaptive.dn.NameValue;
import org.ldaptive.dn.RDn;

/**
 * Utility class for decoding the DER data in an X509 DN.
 *
 * @author  Middleware Services
 */
public class X509DnDecoder implements Function<DERBuffer, Dn>
{

  /** DER path for RDN parsing. */
  private static final DERPath RDN_PATH = new DERPath("/SEQ/SET");

  /** DER path for parsing attribute value assertion. */
  private static final DERPath ASSERTION_PATH = new DERPath("/SEQ");


  @Override
  public Dn apply(final DERBuffer encoded)
  {
    final List<RDn> rdns = new ArrayList<>();
    final DERParser parser = new DERParser();
    parser.registerHandler(
      RDN_PATH,
      (p, e) -> {
        rdns.add(new RDn(decode(e)));
        e.position(e.limit());
      });
    parser.parse(encoded);
    return new Dn(rdns);
  }


  /**
   * Converts bytes in the buffer to attribute value assertions by reading from the current position to the limit.
   *
   * @param  encoded  buffer containing DER-encoded data where the buffer is positioned at the tag of the oid and the
   *                  limit is set beyond the last byte of attribute value data.
   *
   * @return  decoded bytes as attribute value assertions
   */
  private static List<NameValue> decode(final DERBuffer encoded)
  {
    final List<NameValue> nameValues = new ArrayList<>();
    final DERParser parser = new DERParser();
    parser.registerHandler(
      ASSERTION_PATH,
      (p, e) -> {
        if (UniversalDERTag.OID.getTagNo() != p.readTag(e).getTagNo()) {
          throw new IllegalArgumentException("Expected OID tag");
        }

        final int seqLimit = e.limit();
        final int oidLength = p.readLength(e);
        e.limit(e.position() + oidLength);

        final String oid = OidType.decode(e);
        e.limit(seqLimit);

        p.readTag(e);
        p.readLength(e);
        nameValues.add(new NameValue(oid, e.getRemainingBytes()));
      });
    parser.parse(encoded);
    return nameValues;
  }
}
