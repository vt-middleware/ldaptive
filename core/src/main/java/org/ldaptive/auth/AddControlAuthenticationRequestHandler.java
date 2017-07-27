/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.auth;

import java.util.Arrays;
import org.ldaptive.LdapException;
import org.ldaptive.LdapUtils;
import org.ldaptive.control.RequestControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Authentication request handler that adds {@link RequestControl}s to the {@link AuthenticationRequest}.
 *
 * @author  Middleware Services
 */
public class AddControlAuthenticationRequestHandler implements AuthenticationRequestHandler
{

  /** Logger for this class. */
  protected final Logger logger = LoggerFactory.getLogger(getClass());

  /** Factory that produces request controls. */
  private final ControlFactory controlFactory;


  /**
   * Creates a new add control authentication request handler.
   *
   * @param  factory  to produce request controls
   */
  public AddControlAuthenticationRequestHandler(final ControlFactory factory)
  {
    controlFactory = factory;
  }


  @Override
  public void handle(final String dn, final AuthenticationRequest request)
    throws LdapException
  {
    final RequestControl[] ctls = controlFactory.getControls(dn, request.getUserEx());
    logger.trace("{} produced controls {}", controlFactory, Arrays.toString(ctls));
    if (ctls != null && ctls.length > 0) {
      if (request.getControls() != null && request.getControls().length > 0) {
        request.setControls(LdapUtils.concatArrays(request.getControls(), ctls));
      } else {
        request.setControls(ctls);
      }
    }
  }


  /** Factory that produces {@link RequestControl}s. */
  public interface ControlFactory
  {


    /**
     * Creates a new array of request controls. Implementations must treat the supplied parameters as unauthenticated
     * data. Authentication has not been performed when this factory is invoked.
     *
     * @param  dn  distinguished name of the unauthenticated user
     * @param  user  id of the unauthenticated user
     *
     * @return  request controls
     */
    RequestControl[] getControls(String dn, User user);
  }
}
