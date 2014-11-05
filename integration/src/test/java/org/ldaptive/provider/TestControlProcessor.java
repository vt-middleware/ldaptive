/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.provider;

import javax.naming.ldap.Control;
import org.ldaptive.provider.jndi.JndiControlHandler;

/**
 * Control processor for testing.
 *
 * @author  Middleware Services
 * @version  $Revision: 3001 $ $Date: 2014-06-16 09:53:31 -0400 (Mon, 16 Jun 2014) $
 */
public class TestControlProcessor extends ControlProcessor<Control>
{

  /**
   * Default constructor.
   */
  public TestControlProcessor()
  {
    super(new JndiControlHandler());
  }
}
