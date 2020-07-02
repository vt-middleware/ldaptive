/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.io;

import java.io.IOException;
import java.io.InputStream;
import org.ldaptive.LdapUtils;

/**
 * Provides utility methods for resources.
 *
 * @author  Middleware Services
 */
public final class ResourceUtils
{

  /** Default resource loaders. */
  private static final ResourceLoader[] DEFAULT_RESOURCE_LOADERS = new ResourceLoader[] {
    new ClasspathResourceLoader(),
    new FileResourceLoader(),
  };

  /** Custom resource loaders. */
  private static  ResourceLoader[] customResourceLoaders;


  /** Default constructor. */
  private ResourceUtils() {}


  /**
   * Sets the custom resource loaders.
   *
   * @param  loaders  custom resource loaders
   */
  public static void setCustomResourceLoaders(final ResourceLoader... loaders)
  {
    customResourceLoaders = loaders;
  }


  /**
   * Returns whether the supplied path is supported by a {@link ResourceLoader}.
   *
   * @param  path  to inspect
   * @param  loaders  to invoke {@link ResourceLoader#supports(String)} on
   *
   * @return  whether the supplied string represents a resource
   */
  public static boolean isResource(final String path, final ResourceLoader... loaders)
  {
    for (ResourceLoader loader : loaders) {
      if (loader.supports(path)) {
        return true;
      }
    }
    return false;
  }


  /**
   * Invokes {@link #isResource(String, ResourceLoader...)} with {@link #DEFAULT_RESOURCE_LOADERS}.
   *
   * @param  path  to inspect
   *
   * @return  whether the supplied string represents a resource
   */
  public static boolean isResource(final String path)
  {
    if (customResourceLoaders != null && customResourceLoaders.length > 0) {
      return isResource(path, LdapUtils.concatArrays(DEFAULT_RESOURCE_LOADERS, customResourceLoaders));
    }
    return isResource(path, DEFAULT_RESOURCE_LOADERS);
  }


  /**
   * Attempts to find a {@link ResourceLoader} that supports the supplied path. If found, that resource loader is used
   * to load the input stream.
   *
   * @param  path  that designates a resource
   * @param  loaders  to invoke {@link ResourceLoader#load(String)} on
   *
   * @return  input  stream to read the resource
   *
   * @throws  IOException  if the resource cannot be read
   * @throws  IllegalArgumentException  if path is not supported
   */
  public static InputStream getResource(final String path, final ResourceLoader... loaders)
    throws IOException
  {
    for (ResourceLoader loader : loaders) {
      if (loader.supports(path)) {
        return loader.load(path);
      }
    }
    throw new IllegalArgumentException("Could not find a resource loader for '" + path + "'");
  }


  /**
   * Invokes {@link #getResource(String, ResourceLoader...)} with {@link #DEFAULT_RESOURCE_LOADERS}.
   *
   * @param  path  that designates a resource
   *
   * @return  input  stream to read the resource
   *
   * @throws  IOException  if the resource cannot be read
   * @throws  IllegalArgumentException  if path is not supported
   */
  public static InputStream getResource(final String path)
    throws IOException
  {
    if (customResourceLoaders != null && customResourceLoaders.length > 0) {
      return getResource(path, LdapUtils.concatArrays(DEFAULT_RESOURCE_LOADERS, customResourceLoaders));
    }
    return getResource(path, DEFAULT_RESOURCE_LOADERS);
  }


  /**
   * Reads the data from the supplied resource path using the supplied loaders. See {@link
   * #getResource(String, ResourceLoader...)} and {@link LdapUtils#readInputStream(InputStream)}.
   *
   * @param  path  that designates a resource
   * @param  loaders  to invoke {@link #getResource(String, ResourceLoader...)} with
   *
   * @return  bytes  read from the resource
   *
   * @throws  IOException  if the resource cannot be read
   */
  public static byte[] readResource(final String path, final ResourceLoader... loaders)
    throws IOException
  {
    return LdapUtils.readInputStream(getResource(path, loaders));
  }


  /**
   * Reads the data from the supplied resource path. See {@link #getResource(String)} and {@link
   * LdapUtils#readInputStream(InputStream)}.
   *
   * @param  path  that designates a resource
   *
   * @return  bytes  read from the resource
   *
   * @throws  IOException  if the resource cannot be read
   */
  public static byte[] readResource(final String path)
    throws IOException
  {
    return LdapUtils.readInputStream(getResource(path));
  }
}
