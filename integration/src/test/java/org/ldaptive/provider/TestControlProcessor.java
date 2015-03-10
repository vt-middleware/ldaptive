/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.provider;

import javax.naming.ldap.Control;
import org.ldaptive.provider.jndi.JndiControlHandler;

/**
 * Control processor for testing.
 *
 * @author  Middleware Services
 */
public class TestControlProcessor extends ControlProcessor<Control>
{

  /** Default constructor. */
  public TestControlProcessor()
  {
    super(new JndiControlHandler());
  }
}
