/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.protocol;

import java.nio.ByteBuffer;
import java.util.Optional;
import org.ldaptive.asn1.DERBuffer;
import org.ldaptive.asn1.DERParser;

/**
 * Parses a buffer looking for an LDAP response message.
 *
 * @author  Middleware Services
 */
public class ResponseParser
{

  /** Parser for decoding LDAP messages. */
  private final DERParser parser = new DERParser();

  /** Message produced from parsing a DER buffer. */
  private Message message;


  /**
   * Creates a new response parser.
   */
  public ResponseParser()
  {
    parser.registerHandler("/SEQ/APP(1)", (p, e) -> {
      e.clear();
      message = new BindResponse(e);
    });
    parser.registerHandler("/SEQ/APP(4)", (p, e) -> {
      e.clear();
      message = new SearchResultEntry(e);
    });
    parser.registerHandler("/SEQ/APP(5)", (p, e) -> {
      e.clear();
      message = new SearchResultDone(e);
    });
    parser.registerHandler("/SEQ/APP(7)", (p, e) -> {
      e.clear();
      message = new ModifyResponse(e);
    });
    parser.registerHandler("/SEQ/APP(9)", (p, e) -> {
      e.clear();
      message = new AddResponse(e);
    });
    parser.registerHandler("/SEQ/APP(11)", (p, e) -> {
      e.clear();
      message = new DeleteResponse(e);
    });
    parser.registerHandler("/SEQ/APP(13)", (p, e) -> {
      e.clear();
      message = new ModifyDnResponse(e);
    });
    parser.registerHandler("/SEQ/APP(15)", (p, e) -> {
      e.clear();
      message = new CompareResponse(e);
    });
    parser.registerHandler("/SEQ/APP(19)", (p, e) -> {
      e.clear();
      message = new SearchResultReference(e);
    });
    parser.registerHandler("/SEQ/APP(24)", (p, e) -> {
      e.clear();
      final ExtendedResponse extRes = new ExtendedResponse(e);
      if (NoticeOfDisconnection.OID.equals(extRes.getResponseName())) {
        e.clear();
        message = new NoticeOfDisconnection(e);
      } else if (UnsolicitedNotification.OID.equals(extRes.getResponseName())) {
        e.clear();
        message = new UnsolicitedNotification(e);
      } else {
        message = extRes;
      }
    });
    parser.registerHandler("/SEQ/APP(25)", (p, e) -> {
      e.clear();
      message = new IntermediateResponse(e);
    });
  }


  /**
   * Examines the supplied buffer and parses an LDAP response message if one is found.
   *
   * @param  buffer  to parse
   *
   * @return  optional LDAP message
   */
  public Optional<Message> parse(final ByteBuffer buffer)
  {
    parser.parse(buffer);
    return Optional.ofNullable(message);
  }
}
