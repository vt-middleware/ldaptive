/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Connection strategy that randomizes the list of configured URLs. A random URL ordering will be created for each
 * connection attempt.
 *
 * @author  Middleware Services
 */
public class RandomConnectionStrategy extends AbstractConnectionStrategy
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
  public synchronized List<LdapURL> apply()
  {
    if (!isInitialized()) {
      throw new IllegalStateException("Strategy is not initialized");
    }
    return ldapURLs.stream().collect(Collectors.collectingAndThen(
      Collectors.toList(),
      l -> {
        Collections.shuffle(l);
        return Collections.unmodifiableList(l);
      }));
  }
}
