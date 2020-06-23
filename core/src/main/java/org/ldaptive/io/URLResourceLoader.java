/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.io;

import java.io.InputStream;
import java.net.URL;

/**
 * Creates an {@link InputStream} from a string that is a {@link URL}.
 *
 * @author  Middleware Services
 */
public class URLResourceLoader implements ResourceLoader
{


  @Override
  public boolean supports(final String path)
  {
    try {
      new URL(path);
      return true;
    } catch (Exception e) {
      return false;
    }
  }


  @Override
  public InputStream load(final String path)
  {
    try {
      return new URL(path).openStream();
    } catch (Exception e) {
      throw new IllegalArgumentException(e);
    }
  }
}
