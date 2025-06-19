/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.schema;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.ldaptive.LdapUtils;

/**
 * Base class for default definition functions.
 *
 * @param  <T>  type of schema element
 *
 * @author  Middleware Services
 */
public abstract class AbstractDefaultDefinitionFunction<T extends SchemaElement<?>> implements DefinitionFunction<T>
{


  /**
   * Validates that the supplied definition is generally of the correct form. Must start with an open parenthesis and
   * end with a close parenthesis.
   *
   * @param  definition  to validate
   *
   * @return  buffer without opening and closing parenthesis
   *
   * @throws  SchemaParseException  if the buffer is invalid
   */
  protected CharBuffer validate(final String definition)
    throws SchemaParseException
  {
    if (definition == null || definition.isEmpty()) {
      throw new SchemaParseException("Definition cannot be null or empty");
    }
    CharBuffer buffer;
    try {
      buffer = StandardCharsets.UTF_8.newDecoder().decode(ByteBuffer.wrap(LdapUtils.utf8Encode(definition)));
    } catch (CharacterCodingException e) {
      throw new SchemaParseException("Error decoding definition", e);
    }
    if (buffer.get() != '(') {
      throw new SchemaParseException("Definition '" + definition + "' must start with '('");
    }
    if (buffer.get(buffer.limit() - 1) != ')') {
      throw new SchemaParseException("Definition '" + definition + "' must end with ')'");
    }
    if (!buffer.hasRemaining()) {
      throw new SchemaParseException("Definition '" + definition + "' does not contain an expression");
    }
    buffer = buffer.limit(buffer.limit() - 1).slice();
    if (!buffer.hasRemaining()) {
      throw new SchemaParseException("Definition '" + definition + "' does not contain an expression");
    }
    return buffer;
  }


  /**
   * Reads the buffer until a space is encountered.
   *
   * @param  cb  to read from
   *
   * @return  oid
   */
  protected String readOID(final CharBuffer cb)
  {
    return readUntilSpace(cb);
  }


  /**
   * Reads the supplied buffer for $ delimited data between an open and closed parenthesis. Returns an array of
   * integers containing each rule ID that was read. If the buffer doesn't start with an open parenthesis, an array of
   * a single oid is returned. Advances the buffer to the position after the string.
   *
   * @param  cb  to read from
   *
   * @return  oids
   */
  protected String[] readOIDs(final CharBuffer cb)
  {
    if (!cb.hasRemaining()) {
      throw new IllegalArgumentException("Cannot read oids from empty buffer");
    }
    char c = cb.get();
    if (c != '(') {
      return new String[] {readOID(cb.position(cb.position() - 1))};
    }
    if (!cb.hasRemaining()) {
      throw new IllegalArgumentException("Cannot read oids with empty content");
    }

    final int startPos = cb.position();
    final int limit = cb.limit();
    c = readUntil(cb, ')');
    if (c != ')') {
      throw new IllegalArgumentException("oids must end with a close paren");
    }
    final int endPos = cb.position() - 1;
    final CharBuffer slice = cb.limit(endPos).position(startPos).slice();
    cb.limit(limit).position(endPos + 1);
    final String[] oids = SchemaUtils.parseOIDs(slice.toString().trim());
    if (oids.length == 0) {
      throw new IllegalArgumentException("oids cannot be empty");
    }
    return oids;
  }


  /**
   * Reads the buffer until a space is encountered. Converts the read string into an integer.
   *
   * @param  cb  to read from
   *
   * @return  rule id
   */
  protected int readRuleID(final CharBuffer cb)
  {
    final String id = readUntilSpace(cb);
    return Integer.parseInt(id);
  }


  /**
   * Reads the supplied buffer for space delimited data between an open and closed parenthesis. Returns an array of
   * integers containing each rule ID that was read. Advances the buffer to the position after the string.
   *
   * @param  cb  to read from
   *
   * @return  rule ids
   */
  protected int[] readRuleIDs(final CharBuffer cb)
  {
    if (!cb.hasRemaining()) {
      throw new IllegalArgumentException("Cannot read ruleids from empty buffer");
    }
    char c = cb.get();
    if (c != '(') {
      return new int[] {readRuleID(cb.position(cb.position() - 1))};
    }
    if (!cb.hasRemaining()) {
      throw new IllegalArgumentException("Cannot read ruleids with empty content");
    }

    final int startPos = cb.position();
    final int limit = cb.limit();
    c = readUntil(cb, ')');
    if (c != ')') {
      throw new IllegalArgumentException("ruleids must end with a close paren");
    }
    final int endPos = cb.position() - 1;
    final CharBuffer slice = cb.limit(endPos).position(startPos).slice();
    cb.limit(limit).position(endPos + 1);
    final int[] ids = SchemaUtils.parseNumbers(slice.toString().trim());
    if (ids.length == 0) {
      throw new IllegalArgumentException("ruleids cannot be empty");
    }
    return ids;
  }


  /**
   * Reads the supplied buffer for content between two single quotes. Returns a string for the portion of the buffer
   * that was read. Advances the buffer to the position after the string.
   *
   * @param  cb  to read from
   *
   * @return  string read from the buffer
   */
  protected String readQDString(final CharBuffer cb)
  {
    if (!cb.hasRemaining()) {
      throw new IllegalArgumentException("Cannot read qdstring from empty buffer");
    }
    char c = cb.get();
    if (c != '\'') {
      throw new IllegalArgumentException("qdstring must start with a single quote");
    }
    if (!cb.hasRemaining()) {
      throw new IllegalArgumentException("Cannot read qdstring with empty content");
    }

    final int startPos = cb.position();
    final int limit = cb.limit();
    c = readUntil(cb, '\'');
    if (c != '\'') {
      throw new IllegalArgumentException("qdstring must end with a single quote");
    }
    final int endPos = cb.position() - 1;
    final CharBuffer slice = cb.limit(endPos).position(startPos).slice();
    cb.limit(limit).position(endPos + 1);
    return SchemaUtils.parseQDString(slice.toString());
  }


  /**
   * Reads the supplied buffer for single quoted data between an open and closed parenthesis. Returns an array of
   * strings containing each qdstring that was read. If the buffer contains only data between single quotes, an array of
   * a single qdstring is returned. Advances the buffer to the position after the string.
   *
   * @param  cb  to read from
   *
   * @return  string read from the buffer
   */
  protected String[] readQDStrings(final CharBuffer cb)
  {
    if (!cb.hasRemaining()) {
      throw new IllegalArgumentException("Cannot read qdstrings from empty buffer");
    }
    char c = cb.get();
    if (c == '\'') {
      return new String[] {readQDString(cb.position(cb.position() - 1))};
    } else if (c != '(') {
      throw new IllegalArgumentException("qdstrings must start with a single quote or an open paren");
    }
    if (!cb.hasRemaining()) {
      throw new IllegalArgumentException("Cannot read qdstrings with empty content");
    }

    final List<String> values = new ArrayList<>();
    final int limit = cb.limit();
    while (cb.hasRemaining()) {
      c = cb.get();
      if (c == ')') {
        break;
      } else if (c == '\'') {
        final int startValue = cb.position();
        c = readUntil(cb, '\'');
        if (c != '\'') {
          throw new IllegalArgumentException("qdstring must end with a single quote");
        }
        final int endPos = cb.position() - 1;
        final CharBuffer slice = cb.limit(endPos).position(startValue).slice();
        cb.limit(limit).position(endPos + 1);
        values.add(SchemaUtils.parseQDString(slice.toString()));
      }
    }
    if (c != ')') {
      throw new IllegalArgumentException("qdstrings must end with a close paren");
    }
    if (values.isEmpty()) {
      throw new IllegalArgumentException("qdstrings cannot be empty");
    }
    return values.toArray(new String[0]);
  }


  /**
   * Reads the supplied buffer until a space is found. Returns a string for the portion of the buffer that was read.
   * Advances the buffer to the position after the string.
   *
   * @param  cb  to read from
   *
   * @return  string read from the buffer or empty string if the buffer has no remaining characters
   */
  protected String readUntilSpace(final CharBuffer cb)
  {
    if (!cb.hasRemaining()) {
      return "";
    }
    final int startPos = cb.position();
    final int limit = cb.limit();
    readUntil(cb, ' ');
    final int endPos = cb.position() - 1;
    final CharBuffer slice = cb.limit(endPos).position(startPos).slice();
    cb.limit(limit).position(endPos);
    return slice.toString();
  }


  /**
   * Advances the buffer position to the first character that is not a space or the end of the buffer is reached. No-op
   * if the buffer has no remaining characters.
   *
   * @param  cb  to read from
   */
  protected void skipSpaces(final CharBuffer cb)
  {
    if (!cb.hasRemaining()) {
      return;
    }
    while (cb.hasRemaining()) {
      if (cb.get() != ' ') {
        break;
      }
    }
    if (!cb.hasRemaining()) {
      return;
    }
    cb.position(cb.position() - 1);
  }


  /**
   * Advances the buffer position until the supplied character is found or the end of the buffer is reached.
   *
   * @param  cb  to read from
   * @param  c  to stop advancing at
   *
   * @return  the last character read
   */
  private char readUntil(final CharBuffer cb, final char c)
  {
    char bufferChar = 0;
    while (cb.hasRemaining()) {
      bufferChar = cb.get();
      if (bufferChar == c) {
        break;
      }
    }
    return bufferChar;
  }
}
