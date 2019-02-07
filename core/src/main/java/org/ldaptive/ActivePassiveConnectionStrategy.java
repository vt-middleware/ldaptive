/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Connection strategy that attempts hosts ordered exactly the way they are configured. This means that the first host
 * will always be attempted first, followed by each host in the list.
 *
 * @author  Middleware Services
 */
public class ActivePassiveConnectionStrategy extends AbstractConnectionStrategy
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
      ldapURLs = Stream.of(urls.split(" ")).map(LdapURL::new).collect(Collectors.collectingAndThen(
        Collectors.toList(),
        Collections::unmodifiableList));
    } else {
      ldapURLs = Collections.singletonList(new LdapURL(urls));
    }
  }


  @Override
  public List<LdapURL> apply()
  {
    if (!isInitialized()) {
      throw new IllegalStateException("Strategy is not initialized");
    }
    return ldapURLs;
  }
}
