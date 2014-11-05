/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.schema;

/**
 * Provides utility methods for this package.
 *
 * @author  Middleware Services
 */
public final class SchemaUtils
{


  /** Default constructor. */
  private SchemaUtils() {}


  /**
   * Parses the supplied descriptors string and returns it's contents as a
   * string array. If the string contains a single quote it is assumed to be a
   * multivalue descriptor of the form "'value1' 'value2' 'value3'". Otherwise
   * it is treated as a single value descriptor.
   *
   * @param  descrs  string to parse
   *
   * @return  array of descriptors
   */
  public static String[] parseDescriptors(final String descrs)
  {
    if (descrs.contains("'")) {
      final String[] quotedDescr = descrs.split(" ");
      final String[] s = new String[quotedDescr.length];
      for (int i = 0; i < s.length; i++) {
        s[i] = quotedDescr[i].substring(1, quotedDescr[i].length() - 1).trim();
      }
      return s;
    } else {
      return new String[] {descrs};
    }
  }


  /**
   * Parses the supplied OID string and returns it's contents as a string array.
   * If the string contains a dollar sign it is assumed to be a multivalue OID
   * of the form "value1 $ value2 $ value3". Otherwise it is treated as a single
   * value OID.
   *
   * @param  oids  string to parse
   *
   * @return  array of oids
   */
  public static String[] parseOIDs(final String oids)
  {
    if (oids.contains("$")) {
      final String[] s = oids.split("\\$");
      for (int i = 0; i < s.length; i++) {
        s[i] = s[i].trim();
      }
      return s;
    } else {
      return new String[] {oids};
    }
  }


  /**
   * Parses the supplied number string and returns it's contents as a string
   * array.
   *
   * @param  numbers  string to parse
   *
   * @return  array of numbers
   */
  public static int[] parseNumbers(final String numbers)
  {
    final String[] s = numbers.split(" ");
    final int[] i = new int[s.length];
    for (int j = 0; j < i.length; j++) {
      i[j] = Integer.parseInt(s[j].trim());
    }
    return i;
  }


  /**
   * Returns a formatted string to describe the supplied descriptors.
   *
   * @param  descrs  to format
   *
   * @return  formatted string
   */
  public static String formatDescriptors(final String... descrs)
  {
    final StringBuilder sb = new StringBuilder();
    if (descrs.length == 1) {
      sb.append("'").append(descrs[0].replace("'", "\\27")).append("' ");
    } else {
      sb.append("( ");
      for (String descr : descrs) {
        sb.append("'").append(descr.replace("'", "\\27")).append("' ");
      }
      sb.append(") ");
    }
    return sb.toString();
  }


  /**
   * Returns a formatted string to describe the supplied OIDs.
   *
   * @param  oids  to format
   *
   * @return  formatted string
   */
  public static String formatOids(final String... oids)
  {
    final StringBuilder sb = new StringBuilder();
    if (oids.length == 1) {
      sb.append(oids[0]).append(" ");
    } else {
      sb.append("( ");
      for (int i = 0; i < oids.length; i++) {
        sb.append(oids[i]);
        if (i < oids.length - 1) {
          sb.append(" $ ");
        } else {
          sb.append(" ");
        }
      }
      sb.append(") ");
    }
    return sb.toString();
  }


  /**
   * Returns a formatted string to describe the supplied numbers.
   *
   * @param  numbers  to format
   *
   * @return  formatted string
   */
  public static String formatNumbers(final int... numbers)
  {
    final StringBuilder sb = new StringBuilder();
    if (numbers.length == 1) {
      sb.append(numbers[0]).append(" ");
    } else {
      sb.append("( ");
      for (int number : numbers) {
        sb.append(number).append(" ");
      }
      sb.append(") ");
    }
    return sb.toString();
  }
}
