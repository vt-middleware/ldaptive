/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.transport;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.ldaptive.AddResponse;
import org.ldaptive.BindResponse;
import org.ldaptive.CompareResponse;
import org.ldaptive.DeleteResponse;
import org.ldaptive.ModifyDnResponse;
import org.ldaptive.ModifyResponse;
import org.ldaptive.Result;
import org.ldaptive.SearchResponse;
import org.ldaptive.asn1.ApplicationDERTag;
import org.ldaptive.asn1.ConstructedDEREncoder;
import org.ldaptive.asn1.ContextDERTag;
import org.ldaptive.asn1.DEREncoder;
import org.ldaptive.asn1.IntegerType;
import org.ldaptive.asn1.OctetStringType;
import org.ldaptive.asn1.UniversalDERTag;
import org.ldaptive.extended.ExtendedResponse;

/**
 * Simple implementation of response encoding, used for testing.
 *
 * @author  Middleware Services
 */
public final class ResponseEncoder
{

  /** Default constructor. */
  private ResponseEncoder() {}


  /**
   * Returns the response encoders for a result message.
   *
   * @param  result  to encode
   *
   * @return  encoded response
   */
  public static byte[] encode(final Result result)
  {
    DEREncoder[] responseEncoders = null;
    if (result instanceof AddResponse) {
      responseEncoders = getResponseEncoders(result, AddResponse.PROTOCOL_OP);
    } else if (result instanceof BindResponse) {
      responseEncoders = getBindResponseEncoder((BindResponse) result);
    } else if (result instanceof CompareResponse) {
      responseEncoders = getResponseEncoders(result, CompareResponse.PROTOCOL_OP);
    } else if (result instanceof DeleteResponse) {
      responseEncoders = getResponseEncoders(result, DeleteResponse.PROTOCOL_OP);
    } else if (result instanceof ExtendedResponse) {
      responseEncoders = getExtendedResponseEncoder((ExtendedResponse) result);
    } else if (result instanceof ModifyDnResponse) {
      responseEncoders = getResponseEncoders(result, ModifyDnResponse.PROTOCOL_OP);
    } else if (result instanceof ModifyResponse) {
      responseEncoders = getResponseEncoders(result, ModifyResponse.PROTOCOL_OP);
    } else if (result instanceof SearchResponse) {
      responseEncoders = getResponseEncoders(result, SearchResponse.PROTOCOL_OP);
    }
    final ConstructedDEREncoder se = new ConstructedDEREncoder(
      UniversalDERTag.SEQ, responseEncoders);
    return se.encode();
  }


  /**
   * Returns the response encoders for a bind response message.
   *
   * @param  result  to encode
   *
   * @return  bind response encoders
   */
  private static DEREncoder[] getBindResponseEncoder(final BindResponse result)
  {
    // CheckStyle:MagicNumber OFF
    final DEREncoder saslCredentials = result.getServerSaslCreds() != null ?
      new OctetStringType(new ContextDERTag(7, false), result.getServerSaslCreds()) : null;
    // CheckStyle:MagicNumber ON
    if (saslCredentials == null)  {
      return getResponseEncoders(result, BindResponse.PROTOCOL_OP);
    }
    return getResponseEncoders(result, BindResponse.PROTOCOL_OP, saslCredentials);
  }


  /**
   * Returns the response encoders for an extended response message.
   *
   * @param  result  to encode
   *
   * @return  extended response encoders
   */
  private static DEREncoder[] getExtendedResponseEncoder(final ExtendedResponse result)
  {
    // CheckStyle:MagicNumber OFF
    final DEREncoder nameEncoder = result.getResponseName() != null ?
      new OctetStringType(new ContextDERTag(10, false), result.getResponseName()) : null;
    final DEREncoder valueEncoder = result.getResponseValue() != null ?
      new OctetStringType(new ContextDERTag(11, false), result.getResponseValue()) : null;
    // CheckStyle:MagicNumber ON
    if (nameEncoder == null && valueEncoder == null)  {
      return getResponseEncoders(result, ExtendedResponse.PROTOCOL_OP);
    }
    if (nameEncoder != null && valueEncoder != null) {
      return getResponseEncoders(result, ExtendedResponse.PROTOCOL_OP, nameEncoder, valueEncoder);
    }
    return getResponseEncoders(result, ExtendedResponse.PROTOCOL_OP, nameEncoder != null ? nameEncoder : valueEncoder);
  }


  /**
   * Returns the default response encoders for response messages.
   *
   * @param  result  to create encoders for
   * @param  protocolOp  of the response to encode
   * @param  encoders  to append to the response
   *
   * @return  response encoders
   */
  private static DEREncoder[] getResponseEncoders(
    final Result result,
    final int protocolOp,
    final DEREncoder... encoders)
  {
    final List<DEREncoder> baseEncoders = new ArrayList<>();
    baseEncoders.add(new IntegerType(UniversalDERTag.ENUM, result.getResultCode().value()));
    baseEncoders.add(new OctetStringType(result.getMatchedDN() != null ? result.getMatchedDN() : ""));
    baseEncoders.add(new OctetStringType(result.getDiagnosticMessage() != null ? result.getDiagnosticMessage() : ""));
    final String[] referrals = result.getReferralURLs();
    if (referrals != null && referrals.length > 0) {
      baseEncoders.add(
        new ConstructedDEREncoder(
          UniversalDERTag.SEQ,
          Stream.of(referrals).map(OctetStringType::new).toArray(DEREncoder[]::new)));
    }
    // TODO add response control encoder
    Stream.of(encoders).forEach(baseEncoders::add);
    return new DEREncoder[] {
      new IntegerType(result.getMessageID()),
      new ConstructedDEREncoder(
        new ApplicationDERTag(protocolOp, true),
        baseEncoders.toArray(new DEREncoder[0])),
    };
  }
}
