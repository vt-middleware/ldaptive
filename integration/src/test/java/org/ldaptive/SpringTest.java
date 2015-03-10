/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import org.ldaptive.pool.BlockingConnectionPool;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

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
  @Test(groups = {"spring"})
  public void testSpringWiring()
    throws Exception
  {
    final ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
      new String[] {"/spring-context.xml", });
    AssertJUnit.assertTrue(context.getBeanDefinitionCount() > 0);

    ConnectionFactory cf = context.getBean("connectionFactory", ConnectionFactory.class);
    Connection conn = cf.getConnection();
    try {
      conn.open();
    } finally {
      conn.close();
    }

    final ClassPathXmlApplicationContext factoryContext = new ClassPathXmlApplicationContext(
      new String[] {"/spring-factory-context.xml", });
    AssertJUnit.assertTrue(factoryContext.getBeanDefinitionCount() > 0);
    cf = factoryContext.getBean("connectionFactory", ConnectionFactory.class);
    conn = cf.getConnection();
    try {
      conn.open();
    } finally {
      conn.close();
    }

    final ClassPathXmlApplicationContext poolContext = new ClassPathXmlApplicationContext(
      new String[] {"/spring-pool-context.xml", });
    AssertJUnit.assertTrue(poolContext.getBeanDefinitionCount() > 0);

    BlockingConnectionPool pool = null;
    try {
      pool = poolContext.getBean("pool", BlockingConnectionPool.class);
    } finally {
      pool.close();
    }
  }
}
