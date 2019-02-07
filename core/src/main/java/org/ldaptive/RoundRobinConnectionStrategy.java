/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Connection strategy that moves the first URL in it's list to the end of that list.
 *
 * @author  Middleware Services
 */
public class RoundRobinConnectionStrategy extends AbstractConnectionStrategy
{

  /** LDAP URLs. */
  private List<LdapURL> ldapURLs;


  @Override
  public boolean isInitialized()
  {
    return ldapURLs != null;
  }


  @Override
  public void initialize(final String urls)
  {
    if (urls.contains(" ")) {
      ldapURLs = Stream.of(urls.split(" ")).map(LdapURL::new).collect(Collectors.toList());
    } else {
      ldapURLs = Collections.singletonList(new LdapURL(urls));
    }
  }


  @Override
  public synchronized List<LdapURL> apply()
  {
    if (!isInitialized()) {
      throw new IllegalStateException("Strategy is not initialized");
    }
    if (ldapURLs.size() == 1) {
      return ldapURLs;
    }
    final List<LdapURL> l = List.copyOf(ldapURLs);
    ldapURLs.add(ldapURLs.remove(0));
    return l;
  }
}
