/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.auth;

import org.ldaptive.LdapException;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchResponse;
import org.ldaptive.extended.ExtendedResponse;
import org.ldaptive.extended.WhoAmIRequest;
import org.ldaptive.extended.WhoAmIResponseParser;

/**
 * Executes the whoami extended operation on the authenticated connection, then performs an object level search
 * on the result. Useful when users authenticate with some mapped identifier, like DIGEST-MD5.
 *
 * @author  Middleware Services
 */
public class WhoAmIEntryResolver extends AbstractSearchEntryResolver
{


  @Override
  protected SearchResponse performLdapSearch(
    final AuthenticationCriteria criteria,
    final AuthenticationHandlerResponse response)
    throws LdapException
  {
    final ExtendedResponse whoamiRes = response.getConnection().operation(new WhoAmIRequest()).execute();
    logger.debug("whoami operation returned {}", whoamiRes);

    if (!whoamiRes.isSuccess()) {
      throw new LdapException("Unsuccessful WhoAmI operation: " + whoamiRes);
    }
    final String authzId = WhoAmIResponseParser.parse(whoamiRes);
    if (authzId == null || !authzId.contains(":")) {
      throw new IllegalStateException("WhoAmI operation returned illegal authorization ID: '" + authzId + "'");
    }

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
}
