/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.transport;

import java.util.Optional;
import org.ldaptive.Request;
import org.ldaptive.SearchRequest;
import org.ldaptive.UnbindRequest;
import org.ldaptive.asn1.DERBuffer;
import org.ldaptive.asn1.DERParser;

/**
 * Parses a buffer looking for an LDAP request message.
 *
 * @author  Middleware Services
 */
public class RequestParser
{

  /** Parser for decoding LDAP messages. */
  private final DERParser parser = new DERParser();

  /** Message produced from parsing a DER buffer. */
  private Request message;


  /**
   * Creates a new request parser.
   */
  public RequestParser()
  {
    parser.registerHandler("/SEQ/APP(0)", (p, e) -> {
      e.clear();
      message = null;
    });
    parser.registerHandler("/SEQ/APP(2)", (p, e) -> {
      e.clear();
      message = new UnbindRequest();
    });
    parser.registerHandler("/SEQ/APP(3)", (p, e) -> {
      e.clear();
      // note that no decoding is occurring here
      message = new SearchRequest();
    });
    parser.registerHandler("/SEQ/APP(6)", (p, e) -> {
      e.clear();
      message = null;
    });
    parser.registerHandler("/SEQ/APP(8)", (p, e) -> {
      e.clear();
      message = null;
    });
    parser.registerHandler("/SEQ/APP(10)", (p, e) -> {
      e.clear();
      message = null;
    });
    parser.registerHandler("/SEQ/APP(12)", (p, e) -> {
      e.clear();
      message = null;
    });
    parser.registerHandler("/SEQ/APP(14)", (p, e) -> {
      e.clear();
      message = null;
    });
    parser.registerHandler("/SEQ/APP(16)", (p, e) -> {
      e.clear();
      message = null;
    });
    parser.registerHandler("/SEQ/APP(23)", (p, e) -> {
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
