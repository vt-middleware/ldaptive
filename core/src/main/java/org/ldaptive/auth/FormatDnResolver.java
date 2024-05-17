/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.auth;

import java.util.Arrays;
import org.ldaptive.AbstractFreezable;
import org.ldaptive.LdapException;
import org.ldaptive.LdapUtils;
import org.ldaptive.dn.AttributeValueEscaper;
import org.ldaptive.dn.DefaultAttributeValueEscaper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Returns a DN by applying a formatter. See {@link java.util.Formatter}.
 *
 * @author  Middleware Services
 */
public class FormatDnResolver extends AbstractFreezable implements DnResolver
{

  /** log for this class. */
  protected final Logger logger = LoggerFactory.getLogger(getClass());

  /** attribute value escaper. */
  private final AttributeValueEscaper attributeValueEscaper = new DefaultAttributeValueEscaper();

  /** format of DN. */
  private String formatString;

  /** format arguments. */
  private Object[] formatArgs;

  /** whether to escape the user input. */
  private boolean escapeUser = true;


  /** Default constructor. */
  public FormatDnResolver() {}


  /**
   * Creates a new format DN resolver.
   *
   * @param  format  formatter string
   */
  public FormatDnResolver(final String format)
  {
    setFormat(format);
  }


  /**
   * Creates a new format DN resolver with the supplied format and arguments.
   *
   * @param  format  to set formatter string
   * @param  args  to set formatter arguments
   */
  public FormatDnResolver(final String format, final Object[] args)
  {
    setFormat(format);
    setFormatArgs(args);
  }


  /**
   * Returns the formatter string used to return the entry DN.
   *
   * @return  user field
   */
  public final String getFormat()
  {
    return formatString;
  }


  /**
   * Sets the formatter string used to return the entry DN.
   *
   * @param  format  formatter string
   */
  public final void setFormat(final String format)
  {
    assertMutable();
    formatString = format;
  }


  /**
   * Returns the format arguments.
   *
   * @return  format args
   */
  public final Object[] getFormatArgs()
  {
    return LdapUtils.copyArray(formatArgs);
  }


  /**
   * Sets the format arguments.
   *
   * @param  args  to set format arguments
   */
  public final void setFormatArgs(final Object[] args)
  {
    assertMutable();
    formatArgs = LdapUtils.copyArray(args);
  }


  /**
   * Returns whether the user input will be escaped using {@link #attributeValueEscaper}.
   *
   * @return  whether the user input will be escaped.
   */
  public final boolean getEscapeUser()
  {
    return escapeUser;
  }


  /**
   * Sets whether the user input will be escaped using {@link #attributeValueEscaper}.
   *
   * @param  b  whether the user input will be escaped.
   */
  public final void setEscapeUser(final boolean b)
  {
    assertMutable();
    escapeUser = b;
  }


  /**
   * Returns a DN for the supplied user by applying it to a format string.
   *
   * @param  user  to format dn for
   *
   * @return  user DN
   *
   * @throws  LdapException  never
   */
  @Override
  public String resolve(final User user)
    throws LdapException
  {
    if (formatString == null) {
      throw new IllegalStateException("Format string cannot be null");
    }
    String dn = null;
    if (user != null && user.getIdentifier() != null && !"".equals(user.getIdentifier())) {
      final String escapedUser = escapeUser ? attributeValueEscaper.escape(user.getIdentifier()) : user.getIdentifier();
      logger.debug("Formatting DN for {} with {}", escapedUser, formatString);
      if (formatArgs != null && formatArgs.length > 0) {
        final Object[] args = new Object[formatArgs.length + 1];
        args[0] = escapedUser;
        System.arraycopy(formatArgs, 0, args, 1, formatArgs.length);
        dn = String.format(formatString, args);
      } else {
        dn = String.format(formatString, escapedUser);
      }
    } else {
      logger.debug("User input was empty or null");
    }
    return dn;
  }


  @Override
  public String toString()
  {
    return "[" +
      getClass().getName() + "@" + hashCode() + "::" +
      "formatString=" + formatString + ", " +
      "formatArgs=" + Arrays.toString(formatArgs) + ", " +
      "escapeUser=" + escapeUser + "]";
  }
}
