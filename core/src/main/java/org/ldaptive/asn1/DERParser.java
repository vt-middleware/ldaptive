/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.asn1;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides a SAX-like parsing facility for DER-encoded data where elements of interest in the parse tree may
 * be registered to handlers via the {@link #registerHandler} methods. {@link DERPath} strings are used to map handlers
 * to elements of interest.
 *
 * @author  Middleware Services
 * @see  DERPath
 */
public class DERParser
{

  /** Maximum supported path depth. */
  private static final int MAX_PATH_DEPTH = 16;

  /** Maximum number of tag permutations allowed. */
  private static final int MAX_PERMUTATIONS = (int) Math.pow(2, MAX_PATH_DEPTH);

  /** Logger for this class. */
  protected final Logger logger = LoggerFactory.getLogger(getClass());

  /** Handlers for DER paths. */
  private final Map<DERPath, ParseHandler> handlerMap = new HashMap<>();

  /** Permutations of the current path. */
  private final Queue<DERPath> permutations = new ArrayDeque<>();


  /**
   * Registers the supplied handler to fire when the supplied path is encountered.
   *
   * @param  path  to register
   * @param  handler  to associate with the path
   */
  public void registerHandler(final DERPath path, final ParseHandler handler)
  {
    handlerMap.put(path, handler);
  }


  /**
   * Parse a DER-encoded data structure by calling registered handlers when points of interest are encountered in the
   * parse tree.
   *
   * @param  encoded  DER-encoded bytes.
   */
  public void parse(final DERBuffer encoded)
  {
    parseTags(encoded);
  }


  /**
   * Reads a DER tag from a single byte at the current position of the given buffer. The buffer position is naturally
   * advanced one byte in this operation.
   *
   * @param  encoded  Buffer containing DER-encoded bytes positioned at tag.
   *
   * @return  Tag or null if no universal tag or application-specific tag is known that matches the byte read in.
   */
  public DERTag readTag(final DERBuffer encoded)
  {
    if (encoded.position() >= encoded.limit()) {
      return null;
    }

    final DERTag tag;
    final byte b = encoded.get();
    // CheckStyle:MagicNumber OFF
    final int tagNo = b & 0x1F;
    final boolean constructed = (b & 0x20) == 0x20;
    // Read class from first two high-order bits
    switch (b & 0xC0) {

    case UniversalDERTag.TAG_CLASS:
      tag = UniversalDERTag.fromTagNo(tagNo);
      break;

    case ApplicationDERTag.TAG_CLASS:
      tag = new ApplicationDERTag(tagNo, constructed);
      break;

    case ContextDERTag.TAG_CLASS:
      tag = new ContextDERTag(tagNo, constructed);
      break;

    default:
      // Private class (class 11b)
      throw new IllegalArgumentException("Private classes not supported.");
    }
    // CheckStyle:MagicNumber ON
    return tag;
  }


  /**
   * Reads the length of a DER-encoded value from the given byte buffer. The buffer is expected to be positioned at the
   * byte immediately following the tag byte, which is where the length byte(s) begin(s). Invocation of this method has
   * two generally beneficial side effects:
   *
   * <ol>
   *   <li>Buffer is positioned at <em>start</em> of value bytes.</li>
   *   <li>Buffer limit is set to the <em>end</em> of value bytes.</li>
   * </ol>
   *
   * @param  encoded  buffer containing DER-encoded bytes positioned at start of length byte(s).
   *
   * @return  number of bytes occupied by tag value.
   */
  public int readLength(final DERBuffer encoded)
  {
    int length = 0;
    final byte b = encoded.get();
    // CheckStyle:MagicNumber OFF
    if ((b & 0x80) == 0x80) {
      final int len = b & 0x7F;
      if (len > 0) {
        final int limit = encoded.limit();
        encoded.limit(encoded.position() + len);
        length = IntegerType.decodeUnsignedPrimitive(encoded);
        encoded.limit(limit);
      }
    } else {
      length = b;
    }
    // CheckStyle:MagicNumber ON
    return length;
  }


  /**
   * Reads the supplied DER encoded bytes and invokes handlers as configured paths are encountered.
   *
   * @param  encoded  to parse
   */
  private void parseTags(final DERBuffer encoded)
  {
    int index = 0;
    while (encoded.position() < encoded.limit() && !handlerMap.isEmpty()) {
      final DERTag tag = readTag(encoded);
      if (tag != null) {
        addTag(tag, index++);
        parseTag(tag, encoded);
        removeTag();
      }
    }
  }


  /**
   * Invokes the parse handler for the current path and advances to the next position in the encoded bytes.
   *
   * @param  tag  to inspect for internal tags
   * @param  encoded  to parse
   */
  private void parseTag(final DERTag tag, final DERBuffer encoded)
  {
    final int limit = encoded.limit();
    final int lengthPos = encoded.position();
    final int length;
    try {
      length = readLength(encoded);
    } catch (Exception e) {
      throw new IllegalArgumentException("Could not parse length of tag " + tag + " at position " + lengthPos, e);
    }
    final int end = length + encoded.position();
    final int start = encoded.position();

    if (end > limit) {
      throw new IllegalArgumentException(
        "Tag " + tag + " with length " + length + " at position " + encoded.position() + " exceeds limit " + limit);
    }

    // Invoke handlers for all permutations of current path
    ParseHandler handler;
    for (DERPath p : permutations) {
      handler = handlerMap.get(p);
      if (handler != null) {
        encoded.positionAndLimit(start, end);
        handler.handle(this, encoded);
      }
    }

    if (tag.isConstructed()) {
      parseTags(encoded.positionAndLimit(start, end));
    }
    encoded.positionAndLimit(end, limit);
  }


  /**
   * Add the given tag at the specified index to all permutations of the current parser path and increases the number of
   * permutations as necessary to satisfy the following relation:
   *
   * <pre>size = 2^n</pre>
   *
   * <p>where n is the path length.</p>
   *
   * @param  tag  to add to path.
   * @param  index  of tag relative to parent.
   */
  private void addTag(final DERTag tag, final int index)
  {
    if (permutations.isEmpty()) {
      permutations.add(new DERPath().pushNode(tag.name()));
      permutations.add(new DERPath().pushNode(tag.name(), index));
    } else {
      if (permutations.size() * 2 > MAX_PERMUTATIONS) {
        throw new IllegalArgumentException("Maximum permutations exceeded.");
      }
      final Collection<DERPath> generation = new ArrayDeque<>(permutations.size());
      for (DERPath p : permutations) {
        generation.add(new DERPath(p).pushNode(tag.name()));
        p.pushNode(tag.name(), index);
      }
      permutations.addAll(generation);
    }
  }


  /**
   * Removes the tag at the leaf position of all permutations of the current parser path, and reduces the number of
   * permutations as necessary to satisfy the following relation:
   *
   * <pre>size = 2^n</pre>
   *
   * <p>where n is the path length.</p>
   */
  private void removeTag()
  {
    final int half = permutations.size() / 2;
    while (permutations.size() > half) {
      permutations.remove();
    }
    permutations.forEach(DERPath::popNode);
  }
}
