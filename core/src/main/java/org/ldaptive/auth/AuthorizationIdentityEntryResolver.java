/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.auth;

import org.ldaptive.LdapException;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchResponse;
import org.ldaptive.control.AuthorizationIdentityResponseControl;

/**
 * Reads the authorization identity response control, then performs an object level search on the result. Useful when
 * users authenticate with some mapped identifier, like DIGEST-MD5. This resolver must be used with an {@link
 * AuthenticationHandler} that is configured to send the {@link
 * org.ldaptive.control.AuthorizationIdentityRequestControl}.
 *
 * @author  Middleware Services
 */
public class AuthorizationIdentityEntryResolver extends AbstractSearchEntryResolver
{


  @Override
  protected SearchResponse performLdapSearch(
    final AuthenticationCriteria criteria,
    final AuthenticationHandlerResponse response)
    throws LdapException
  {
    final AuthorizationIdentityResponseControl ctrl = (AuthorizationIdentityResponseControl) response.getControl(
      AuthorizationIdentityResponseControl.OID);
    if (ctrl == null) {
      throw new IllegalStateException("Authorization Identity Response Control not found");
    }
    logger.debug("Found authorization identity response control {}", ctrl);

    final String authzId = ctrl.getAuthorizationId();
    final String dn = authzId.split(":", 2)[1].trim();
    return response.getConnection().operation(createSearchRequest(criteria, dn)).execute();
  }


  /**
   * Returns a search request for an object level search for the supplied DN.
   *
   * @param  ac  authentication criteria containing return attributes
   * @param  dn  from the who am i operation
   *
   * @return  search request
   */
  protected SearchRequest createSearchRequest(final AuthenticationCriteria ac, final String dn)
  {
    final SearchRequest request = SearchRequest.objectScopeSearchRequest(
      dn,
      ac.getAuthenticationRequest().getReturnAttributes());
    request.setDerefAliases(getDerefAliases());
    request.setBinaryAttributes(getBinaryAttributes());
    return request;
  }


  @Override
  public String toString()
  {
    return "[" + super.toString() + "]";
  }
}
