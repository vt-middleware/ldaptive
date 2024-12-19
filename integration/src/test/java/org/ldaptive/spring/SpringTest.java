/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.spring;

import org.ldaptive.Connection;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.pool.BlockingConnectionPool;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.testng.annotations.Test;
import static org.assertj.core.api.Assertions.*;

/**
 * Unit test for Spring integration.
 *
 * @author  Middleware Services
 */
public class SpringTest
{


  /**
   * Attempts to load all Spring application context XML files to verify proper wiring.
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = "spring")
  public void testSpringWiring()
    throws Exception
  {
    final ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
      new String[] {"/spring-context.xml", });
    assertThat(context.getBeanDefinitionCount()).isGreaterThan(0);

    ConnectionFactory cf = context.getBean("connectionFactory", ConnectionFactory.class);
    Connection conn = cf.getConnection();
    try {
      conn.open();
    } finally {
      conn.close();
    }

    final ClassPathXmlApplicationContext factoryContext = new ClassPathXmlApplicationContext(
      new String[] {"/spring-factory-context.xml", });
    assertThat(factoryContext.getBeanDefinitionCount()).isGreaterThan(0);
    cf = factoryContext.getBean("connectionFactory", ConnectionFactory.class);
    conn = cf.getConnection();
    try {
      conn.open();
    } finally {
      conn.close();
    }

    final ClassPathXmlApplicationContext poolContext = new ClassPathXmlApplicationContext(
      new String[] {"/spring-pool-context.xml", });
    assertThat(poolContext.getBeanDefinitionCount()).isGreaterThan(0);

    BlockingConnectionPool pool = null;
    try {
      pool = poolContext.getBean("pool", BlockingConnectionPool.class);
    } finally {
      pool.close();
    }

    final ClassPathXmlApplicationContext docs1 = new ClassPathXmlApplicationContext(
      new String[] {"/spring-docs1-context.xml", });
    assertThat(docs1.getBeanDefinitionCount()).isGreaterThan(0);

    final ClassPathXmlApplicationContext docs2 = new ClassPathXmlApplicationContext(
      new String[] {"/spring-docs2-context.xml", });
    assertThat(docs2.getBeanDefinitionCount()).isGreaterThan(0);
  }
}
