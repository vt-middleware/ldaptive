/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.util.List;
import lombok.experimental.SuperBuilder;
import org.ldaptive.control.ResponseControl;

/**
 * LDAP add response defined as:
 *
 * <pre>
   AddResponse ::= [APPLICATION 9] LDAPResult
 * </pre>
 *
 * @author  Middleware Services
 */
@SuperBuilder
public final class AddResponse extends AbstractResult
{

  /** BER protocol number. */
  public static final int PROTOCOL_OP = 9;

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 10211;


  /**
   * Creates a new instance with the given required parameters.
   *
   * @param  messageID  LDAP protocol message ID.
   * @param  controls  Response controls.
   * @param  resultCode  LDAP protocol result code.
   * @param  matchedDN  DN matched by operation.
   * @param  diagnosticMessage  Informative message returned by server.
   * @param  referralUrls  Zero or more referral URLs.
   */
  public AddResponse(
          final int messageID,
          final List<ResponseControl> controls,
          final ResultCode resultCode,
          final String matchedDN,
          final String diagnosticMessage,
          final List<String> referralUrls)
  {
    super(messageID, controls, resultCode, matchedDN, diagnosticMessage, referralUrls);
  }


  @Override
  public boolean equals(final Object o)
  {
    if (o == this) {
      return true;
    }
    return o instanceof AddResponse && super.equals(o);
  }


  @Override
  protected int getHashCodeSeed()
  {
    return HASH_CODE_SEED;
  }


  /**
   * Add response builder.
   */
  public static abstract class AddResponseBuilder extends AbstractResult.AbstractBuilder<AddResponse> {}
}
