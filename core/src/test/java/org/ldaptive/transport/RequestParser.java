/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.transport;

import java.util.Optional;
import org.ldaptive.Request;
import org.ldaptive.SearchRequest;
import org.ldaptive.UnbindRequest;
import org.ldaptive.asn1.DERBuffer;
import org.ldaptive.asn1.DERParser;
import org.ldaptive.asn1.DERPath;

/**
 * Parses a buffer looking for an LDAP request message.
 *
 * @author  Middleware Services
 */
public class RequestParser
{

  /** Bind request DER path. */
  private static final DERPath BIND_PATH = new DERPath("/SEQ/APP(0)");

  /** Unbind request DER path. */
  private static final DERPath UNBIND_PATH = new DERPath("/SEQ/APP(2)");

  /** Search request DER path. */
  private static final DERPath SEARCH_PATH = new DERPath("/SEQ/APP(3)");

  /** Modify request DER path. */
  private static final DERPath MODIFY_PATH = new DERPath("/SEQ/APP(6)");

  /** Add request DER path. */
  private static final DERPath ADD_PATH = new DERPath("/SEQ/APP(8)");

  /** Delete request DER path. */
  private static final DERPath DELETE_PATH = new DERPath("/SEQ/APP(10)");

  /** Modify DN request DER path. */
  private static final DERPath MODIFY_DN_PATH = new DERPath("/SEQ/APP(12)");

  /** Compare request DER path. */
  private static final DERPath COMPARE_PATH = new DERPath("/SEQ/APP(14)");

  /** Abandon request DER path. */
  private static final DERPath ABANDON_PATH = new DERPath("/SEQ/APP(16)");

  /** Extended request DER path. */
  private static final DERPath EXTENDED_PATH = new DERPath("/SEQ/APP(23)");

  /** Parser for decoding LDAP messages. */
  private final DERParser parser = new DERParser();

  /** Message produced from parsing a DER buffer. */
  private Request message;


  /**
   * Creates a new request parser.
   */
  public RequestParser()
  {
    parser.registerHandler(BIND_PATH, (p, e) -> {
      e.clear();
      message = null;
    });
    parser.registerHandler(UNBIND_PATH, (p, e) -> {
      e.clear();
      message = new UnbindRequest();
    });
    parser.registerHandler(SEARCH_PATH, (p, e) -> {
      e.clear();
      // note that no decoding is occurring here
      message = new SearchRequest();
    });
    parser.registerHandler(MODIFY_PATH, (p, e) -> {
      e.clear();
      message = null;
    });
    parser.registerHandler(ADD_PATH, (p, e) -> {
      e.clear();
      message = null;
    });
    parser.registerHandler(DELETE_PATH, (p, e) -> {
      e.clear();
      message = null;
    });
    parser.registerHandler(MODIFY_DN_PATH, (p, e) -> {
      e.clear();
      message = null;
    });
    parser.registerHandler(COMPARE_PATH, (p, e) -> {
      e.clear();
      message = null;
    });
    parser.registerHandler(ABANDON_PATH, (p, e) -> {
      e.clear();
      message = null;
    });
    parser.registerHandler(EXTENDED_PATH, (p, e) -> {
      e.clear();
      message = null;
    });
  }


  /**
   * Examines the supplied buffer and parses an LDAP request message if one is found.
   *
   * @param  buffer  to parse
   *
   * @return  optional LDAP message
   */
  public Optional<Request> parse(final DERBuffer buffer)
  {
    parser.parse(buffer);
    return Optional.ofNullable(message);
  }
}
