/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.io;

import java.io.IOException;
import java.io.InputStream;

/**
 * Creates an {@link InputStream} from a string URI.
 *
 * @author  Middleware Services
 */
public interface ResourceLoader
{


  /**
   * Returns whether the supplied path can be loaded by this resource loader.
   *
   * @param  path  to check
   *
   * @return  whether the supplied path can be loaded by this resource loader
   */
  boolean supports(String path);


  /**
   * Reads an input stream from a path.
   *
   * @param  path  from which to read resource.
   *
   * @return  input stream.
   *
   * @throws  IOException  On IO errors.
   */
  InputStream load(String path)
    throws IOException;
}
