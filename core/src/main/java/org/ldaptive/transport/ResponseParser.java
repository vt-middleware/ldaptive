/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.transport;

import java.util.Optional;
import org.ldaptive.AddResponse;
import org.ldaptive.BindResponse;
import org.ldaptive.CompareResponse;
import org.ldaptive.DeleteResponse;
import org.ldaptive.LdapEntry;
import org.ldaptive.Message;
import org.ldaptive.ModifyDnResponse;
import org.ldaptive.ModifyResponse;
import org.ldaptive.SearchResponse;
import org.ldaptive.SearchResultReference;
import org.ldaptive.asn1.DERBuffer;
import org.ldaptive.asn1.DERParser;
import org.ldaptive.asn1.DERPath;
import org.ldaptive.extended.ExtendedResponse;
import org.ldaptive.extended.IntermediateResponse;
import org.ldaptive.extended.NoticeOfDisconnection;
import org.ldaptive.extended.SyncInfoMessage;

/**
 * Parses a buffer looking for an LDAP response message.
 *
 * @author  Middleware Services
 */
public class ResponseParser
{

  /** Bind response DER path. */
  private static final DERPath BIND_PATH = new DERPath("/SEQ/APP(1)");

  /** Search entry DER path. */
  private static final DERPath ENTRY_PATH = new DERPath("/SEQ/APP(4)");

  /** Search response DER path. */
  private static final DERPath SEARCH_PATH = new DERPath("/SEQ/APP(5)");

  /** Modify response DER path. */
  private static final DERPath MODIFY_PATH = new DERPath("/SEQ/APP(7)");

  /** Add response DER path. */
  private static final DERPath ADD_PATH = new DERPath("/SEQ/APP(9)");

  /** Delete response DER path. */
  private static final DERPath DELETE_PATH = new DERPath("/SEQ/APP(11)");

  /** Modify DN response DER path. */
  private static final DERPath MODIFY_DN_PATH = new DERPath("/SEQ/APP(13)");

  /** Compare response DER path. */
  private static final DERPath COMPARE_PATH = new DERPath("/SEQ/APP(15)");

  /** Search reference result DER path. */
  private static final DERPath SEARCH_REFERENCE_PATH = new DERPath("/SEQ/APP(19)");

  /** Extended response DER path. */
  private static final DERPath EXTENDED_PATH = new DERPath("/SEQ/APP(24)");

  /** Intermediate response DER path. */
  private static final DERPath INTERMEDIATE_PATH = new DERPath("/SEQ/APP(25)");

  /** Parser for decoding LDAP messages. */
  private final DERParser parser = new DERParser();

  /** Message produced from parsing a DER buffer. */
  private Message message;


  /**
   * Creates a new response parser.
   */
  public ResponseParser()
  {
    parser.registerHandler(BIND_PATH, (p, e) -> {
      e.clear();
      message = new BindResponse(e);
    });
    parser.registerHandler(ENTRY_PATH, (p, e) -> {
      e.clear();
      message = new LdapEntry(e);
    });
    parser.registerHandler(SEARCH_PATH, (p, e) -> {
      e.clear();
      message = new SearchResponse(e);
    });
    parser.registerHandler(MODIFY_PATH, (p, e) -> {
      e.clear();
      message = new ModifyResponse(e);
    });
    parser.registerHandler(ADD_PATH, (p, e) -> {
      e.clear();
      message = new AddResponse(e);
    });
    parser.registerHandler(DELETE_PATH, (p, e) -> {
      e.clear();
      message = new DeleteResponse(e);
    });
    parser.registerHandler(MODIFY_DN_PATH, (p, e) -> {
      e.clear();
      message = new ModifyDnResponse(e);
    });
    parser.registerHandler(COMPARE_PATH, (p, e) -> {
      e.clear();
      message = new CompareResponse(e);
    });
    parser.registerHandler(SEARCH_REFERENCE_PATH, (p, e) -> {
      e.clear();
      message = new SearchResultReference(e);
    });
    parser.registerHandler(EXTENDED_PATH, (p, e) -> {
      e.clear();
      final ExtendedResponse extRes = new ExtendedResponse(e);
      if (NoticeOfDisconnection.OID.equals(extRes.getResponseName())) {
        e.clear();
        message = new NoticeOfDisconnection(e);
      } else {
        message = extRes;
      }
    });
    parser.registerHandler(INTERMEDIATE_PATH, (p, e) -> {
      e.clear();
      final IntermediateResponse intRes = new IntermediateResponse(e);
      if (SyncInfoMessage.OID.equals(intRes.getResponseName())) {
        e.clear();
        message = new SyncInfoMessage(e);
      } else {
        message = intRes;
      }
    });
  }


  /**
   * Examines the supplied buffer and parses an LDAP response message if one is found.
   *
   * @param  buffer  to parse
   *
   * @return  optional LDAP message
   */
  public Optional<Message> parse(final DERBuffer buffer)
  {
    parser.parse(buffer);
    return Optional.ofNullable(message);
  }
}
