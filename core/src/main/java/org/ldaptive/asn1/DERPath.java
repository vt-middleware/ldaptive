/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.asn1;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.ldaptive.LdapUtils;

/**
 * Describes paths to individual elements of an encoded DER object that may be addressed during parsing to associate a
 * parsed element with a handler to handle that element. Consider the following production rule for a complex type that
 * may be DER encoded:
 *
 * <pre>

     BankAccountSet ::= SET OF {
       account BankAccount
     }

     BankAccount ::= SEQUENCE OF {
       accountNumber OCTET STRING,
       accountName OCTET STRING,
       accountType AccountType,
       balance REAL
     }

     AccountType ::= ENUM {
       checking (0),
       savings (1)
     }

 * </pre>
 *
 * <p>Given an instance of BankAccountSet with two elements, the path to the balance of each bank account in the set is
 * given by the following expression:</p>
 *
 * <pre>/SET/SEQ/REAL</pre>
 *
 * <p>Individual child elements can be accessed by explicitly mentioning the index of the item relative to its parent.
 * For example, the second bank account in the set can be accessed as follows:</p>
 *
 * <pre>/SET/SEQ[1]</pre>
 *
 * <p>Node names in DER paths are constrained to the following:</p>
 *
 * <ul>
 *   <li>{@link UniversalDERTag} tag names</li>
 *   <li>{@link ApplicationDERTag#TAG_NAME}</li>
 *   <li>{@link ContextDERTag#TAG_NAME}</li>
 * </ul>
 *
 * @author  Middleware Services
 * @see  DERParser
 */
public class DERPath
{

  /** Separates nodes in a path specification. */
  public static final String PATH_SEPARATOR = "/";

  /** General pattern for DER path nodes. */
  private static final Pattern NODE_PATTERN;

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 601;

  static {
    final StringBuilder validNames = new StringBuilder();
    validNames.append(ApplicationDERTag.TAG_NAME).append("\\(\\d+\\)|");
    validNames.append(ContextDERTag.TAG_NAME).append("\\(\\d+\\)|");
    for (UniversalDERTag tag : UniversalDERTag.values()) {
      validNames.append('|').append(tag.name());
    }
    NODE_PATTERN = Pattern.compile(String.format("(%s)(\\[(\\d+)\\])?", validNames.toString()));
  }

  /** Describes the path as a FIFO set of nodes. */
  private final Deque<Node> nodeStack = new ArrayDeque<>();


  /** Creates an empty path specification. */
  public DERPath()
  {
    this(PATH_SEPARATOR);
  }


  /**
   * Copy constructor.
   *
   * @param  path  to read nodes from
   */
  public DERPath(final DERPath path)
  {
    nodeStack.addAll(path.nodeStack);
  }


  /**
   * Creates a path specification from its string representation.
   *
   * @param  pathSpec  string representation of a path, e.g. /SEQ[1]/CHOICE.
   */
  public DERPath(final String pathSpec)
  {
    final String[] nodes = pathSpec.split(PATH_SEPARATOR);
    for (String node : nodes) {
      if ("".equals(node)) {
        continue;
      }
      // Normalize node names to upper case
      nodeStack.add(toNode(node.toUpperCase()));
    }
  }


  /**
   * Appends a node to the path.
   *
   * @param  name  of the path element to add
   *
   * @return  This instance with new node appended.
   */
  public DERPath pushNode(final String name)
  {
    nodeStack.addLast(new Node(name));
    return this;
  }


  /**
   * Appends a node to the path with the given child index.
   *
   * @param  name  of the path element to add
   * @param  index  child index
   *
   * @return  This instance with new node appended.
   */
  public DERPath pushNode(final String name, final int index)
  {
    nodeStack.addLast(new Node(name, index));
    return this;
  }


  /**
   * Examines the last node in the path without removing it.
   *
   * @return  last node in the path or null if no nodes remain
   */
  public String peekNode()
  {
    return nodeStack.peek().toString();
  }


  /**
   * Removes the last node in the path.
   *
   * @return  last node in the path or null if no more nodes remain.
   */
  public String popNode()
  {
    if (nodeStack.isEmpty()) {
      return null;
    }
    return nodeStack.removeLast().toString();
  }


  /**
   * Gets the number of nodes in the path.
   *
   * @return  node count.
   */
  public int getSize()
  {
    return nodeStack.size();
  }


  /**
   * Determines whether the path contains any nodes.
   *
   * @return  True if path contains 0 nodes, false otherwise.
   */
  public boolean isEmpty()
  {
    return nodeStack.isEmpty();
  }


  @Override
  public boolean equals(final Object o)
  {
    if (o == this) {
      return true;
    }
    if (o instanceof DERPath) {
      final DERPath v = (DERPath) o;
      return LdapUtils.areEqual(nodeStack.toArray(), v.nodeStack.toArray());
    }
    return false;
  }


  @Override
  public int hashCode()
  {
    return LdapUtils.computeHashCode(HASH_CODE_SEED, nodeStack);
  }


  @Override
  public String toString()
  {
    final StringBuilder sb = new StringBuilder(nodeStack.size() * 10);
    for (Node node : nodeStack) {
      sb.append(PATH_SEPARATOR);
      node.toString(sb);
    }
    return sb.toString();
  }


  /**
   * Converts a string representation of a node into a {@link Node} object.
   *
   * @param  node  String representation of node.
   *
   * @return  Node corresponding to given string representation.
   *
   * @throws  IllegalArgumentException  for an invalid node name.
   */
  static Node toNode(final String node)
  {
    final Matcher matcher = NODE_PATTERN.matcher(node);
    if (!matcher.matches()) {
      throw new IllegalArgumentException("Invalid node: " + node);
    }

    final String name = matcher.group(1);
    final String index = matcher.group(3);
    if (index != null) {
      return new Node(name, Integer.parseInt(index));
    }
    return new Node(name);
  }


  /**
   * DER path node encapsulates the path name and its location among other children that share a common parent.
   *
   * @author  Middleware Services
   */
  static class Node
  {

    /** hash code seed. */
    private static final int HASH_CODE_SEED = 607;

    /** Name of this node. */
    private final String name;

    /** Index of this node. */
    private final int childIndex;


    /**
     * Creates a new node with an indeterminate index.
     *
     * @param  n  name of this node
     */
    Node(final String n)
    {
      name = n;
      childIndex = -1;
    }


    /**
     * Creates a new node with the given index.
     *
     * @param  n  name of this node
     * @param  i  child index location of this node in the path
     */
    Node(final String n, final int i)
    {
      if (i < 0) {
        throw new IllegalArgumentException("Child index cannot be negative.");
      }
      name = n;
      childIndex = i;
    }


    /**
     * Returns the name.
     *
     * @return  name
     */
    public String getName()
    {
      return name;
    }


    /**
     * Returns the child index.
     *
     * @return  child index
     */
    public int getChildIndex()
    {
      return childIndex;
    }


    @Override
    public boolean equals(final Object o)
    {
      if (o == this) {
        return true;
      }
      if (o instanceof Node) {
        final Node v = (Node) o;
        return LdapUtils.areEqual(name, v.name) &&
               LdapUtils.areEqual(childIndex, v.childIndex);
      }
      return false;
    }


    @Override
    public int hashCode()
    {
      return LdapUtils.computeHashCode(HASH_CODE_SEED, name, childIndex);
    }


    @Override
    public String toString()
    {
      // CheckStyle:MagicNumber OFF
      final StringBuilder sb = new StringBuilder(name.length() + 4);
      // CheckStyle:MagicNumber ON
      toString(sb);
      return sb.toString();
    }


    /**
     * Appends the string representation of this instance to the given string builder.
     *
     * @param  builder  Builder to hold string representation of this instance.
     */
    public void toString(final StringBuilder builder)
    {
      builder.append(name);
      if (childIndex < 0) {
        return;
      }
      builder.append('[').append(childIndex).append(']');
    }
  }
}
