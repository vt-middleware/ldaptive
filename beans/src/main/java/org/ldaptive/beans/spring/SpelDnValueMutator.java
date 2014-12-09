/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.beans.spring;

import org.ldaptive.beans.DnValueMutator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;

/**
 * Uses a SPEL expression and evaluation context to mutate the configured DN of
 * an object.
 *
 * @author  Middleware Services
 */
public class SpelDnValueMutator implements DnValueMutator
{

  /** Logger for this class. */
  protected final Logger logger = LoggerFactory.getLogger(getClass());

  /** SPEL expression to access the DN. */
  private final Expression expression;

  /** Evaluation context. */
  private final EvaluationContext evaluationContext;


  /**
   * Creates a new spel dn value mutator.
   *
   * @param  exp  to access the DN
   * @param  context  containing the DN
   */
  public SpelDnValueMutator(
    final Expression exp,
    final EvaluationContext context)
  {
    expression = exp;
    evaluationContext = context;
  }


  @Override
  public String getValue(final Object object)
  {
    return expression.getValue(evaluationContext, object, String.class);
  }


  @Override
  public void setValue(final Object object, final String value)
  {
    expression.setValue(evaluationContext, object, value);
  }


  @Override
  public String toString()
  {
    return
      String.format(
        "[%s@%d::expression=%s, evaluationContext=%s]",
        getClass().getName(),
        hashCode(),
        expression,
        evaluationContext);
  }
}
