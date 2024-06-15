/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.io;

import java.io.InputStream;
import java.util.Objects;
import org.ldaptive.LdapUtils;

/**
 * Creates an {@link InputStream} from a string that is prefixed with 'classpath:'. See {@link
 * Class#getResourceAsStream(String)}.
 *
 * @author  Middleware Services
 */
public class ClasspathResourceLoader implements ResourceLoader
{

  /** Prefix used to indicate a classpath resource. */
  private static final String PREFIX = "classpath:";


  @Override
  public boolean supports(final String path)
  {
    return path != null && path.startsWith(PREFIX);
  }


  @Override
  public InputStream load(final String path)
  {
    if (!supports(path)) {
      throw new IllegalArgumentException("Path '" + path + "' must start with " + PREFIX);
    }
    // load the resource using a class in the base package
    final InputStream is = LdapUtils.class.getResourceAsStream(path.substring(PREFIX.length()));
    Objects.requireNonNull(is, "Could not get stream from '" + path + "' classpath");
    return is;
  }
}
