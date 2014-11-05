/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.ssl;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import org.ldaptive.LdapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for all credential readers. It provides support for loading files
 * from resources on the classpath or a filepath. If a path is prefixed with the
 * string "classpath:" it is interpreted as a classpath specification. If a path
 * is prefixed with the string "file:" it is interpreted as a file path. Any
 * other input throws IllegalArgumentException.
 *
 * @param  <T>  Type of credential read by this instance.
 *
 * @author  Middleware Services
 */
public abstract class AbstractCredentialReader<T> implements CredentialReader<T>
{

  /** Logger for this class. */
  protected final Logger logger = LoggerFactory.getLogger(getClass());


  /** {@inheritDoc} */
  @Override
  public T read(final String path, final String... params)
    throws IOException, GeneralSecurityException
  {
    final InputStream is = LdapUtils.getResource(path);
    if (is != null) {
      logger.trace("Found resource at {}", path);
      try {
        final T credential = read(is, params);
        logger.debug(
          "Successfully loaded credential {} from path {}",
          credential,
          path);
        return credential;
      } finally {
        is.close();
      }
    } else {
      logger.debug("Failed to load {}", path);
      return null;
    }
  }


  /**
   * Gets a buffered input stream from the given input stream. If the given
   * instance is already buffered, it is simply returned.
   *
   * @param  is  input stream from which to create buffered instance.
   *
   * @return  buffered input stream. If the given instance is already buffered,
   * it is simply returned.
   */
  protected InputStream getBufferedInputStream(final InputStream is)
  {
    if (is.markSupported()) {
      return is;
    } else {
      return new BufferedInputStream(is);
    }
  }
}
