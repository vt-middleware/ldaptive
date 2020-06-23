/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Creates an {@link InputStream} from a string that is prefixed with 'file:'. See {@link
 * FileInputStream#FileInputStream(File)}.
 *
 * @author  Middleware Services
 */
public class FileResourceLoader implements ResourceLoader
{

  /** Prefix used to indicate a file resource. */
  private static final String PREFIX = "file:";


  @Override
  public boolean supports(final String path)
  {
    return path != null && path.startsWith(PREFIX);
  }


  @Override
  public InputStream load(final String path)
    throws IOException
  {
    if (!supports(path)) {
      throw new IllegalArgumentException("Path '" + path + "' must start with " + PREFIX);
    }
    return new FileInputStream(new File(path.substring(PREFIX.length())));
  }
}
