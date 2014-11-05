/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.beans.spring;

import org.ldaptive.beans.AbstractClassDescriptor;
import org.ldaptive.beans.Attribute;
import org.ldaptive.beans.DnValueMutator;
import org.ldaptive.beans.Entry;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.spel.SpelParseException;
import org.springframework.expression.spel.standard.SpelExpressionParser;

/**
 * Spring implementation of a class descriptor. Uses an {@link
 * EvaluationContext} with SPEL expressions to find property values.
 *
 * @author  Middleware Services
 */
public class SpringClassDescriptor extends AbstractClassDescriptor
{

  /** Context for evaluating spring expressions. */
  private final EvaluationContext evaluationContext;


  /**
   * Creates a new spring class descriptor.
   *
   * @param  context  to use for SPEL evaluation
   */
  public SpringClassDescriptor(final EvaluationContext context)
  {
    evaluationContext = context;
  }


  /** {@inheritDoc} */
  @Override
  public void initialize(final Class<?> type)
  {
    // check for entry annotation
    final Entry entryAnnotation = AnnotationUtils.findAnnotation(
      type,
      Entry.class);
    if (entryAnnotation != null) {
      if (!"".equals(entryAnnotation.dn())) {
        setDnValueMutator(createDnValueMutator(entryAnnotation.dn()));
      }
      for (final Attribute attr : entryAnnotation.attributes()) {
        if ("".equals(attr.property()) && attr.values().length > 0) {
          addAttributeValueMutator(
            new SimpleAttributeValueMutator(
              attr.name(),
              attr.values(),
              attr.binary(),
              attr.sortBehavior()));
        } else {
          addAttributeValueMutator(
            new SpelAttributeValueMutator(attr, evaluationContext));
        }
      }
    }
  }


  /**
   * Creates a dn value mutator for the supplied SPEL dn property expression. If
   * an expression cannot be created, a simple dn value mutator is returned.
   *
   * @param  dnProperty  SPEL expression
   *
   * @return  {@link SpelDnValueMutator} if dnProperty can be parsed. Otherwise
   * returns simple dn value mutator
   */
  protected DnValueMutator createDnValueMutator(final String dnProperty)
  {
    try {
      return
        new SpelDnValueMutator(
          new SpelExpressionParser().parseExpression(dnProperty),
          evaluationContext);
    } catch (SpelParseException e) {
      logger.debug(
        "Could not parse dn expression, using SimpleDnValueMutator",
        e);
      return new SimpleDnValueMutator(dnProperty);
    }
  }
}
