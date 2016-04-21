/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.auth;

import java.util.Arrays;
import org.ldaptive.Credential;
import org.ldaptive.ReturnAttributes;

/**
 * Contains the data required to perform an ldap authentication.
 *
 * @author  Middleware Services
 */
public class AuthenticationRequest
{

  /** User. */
  private User user;

  /** User credential. */
  private Credential credential;

  /** User attributes to return. */
  private String[] returnAttributes = ReturnAttributes.NONE.value();


  /** Default constructor. */
  public AuthenticationRequest() {}


  /**
   * Creates a new authentication request.
   *
   * @param  id  that identifies the user
   * @param  c  credential to authenticate the user
   */
  public AuthenticationRequest(final String id, final Credential c)
  {
    setUser(new User(id));
    setCredential(c);
  }


  /**
   * Creates a new authentication request.
   *
   * @param  id  that identifies the user
   * @param  c  credential to authenticate the user
   * @param  attrs  attributes to return
   */
  public AuthenticationRequest(final String id, final Credential c, final String... attrs)
  {
    setUser(new User(id));
    setCredential(c);
    setReturnAttributes(attrs);
  }


  /**
   * Creates a new authentication request.
   *
   * @param  u  that identifies the user
   * @param  c  credential to authenticate the user
   */
  public AuthenticationRequest(final User u, final Credential c)
  {
    setUser(u);
    setCredential(c);
  }


  /**
   * Creates a new authentication request.
   *
   * @param  u  that identifies the user
   * @param  c  credential to authenticate the user
   * @param  attrs  attributes to return
   */
  public AuthenticationRequest(final User u, final Credential c, final String... attrs)
  {
    setUser(u);
    setCredential(c);
    setReturnAttributes(attrs);
  }


  /**
   * Returns the user.
   *
   * @return  user identifier
   */
  public User getUser()
  {
    return user;
  }


  /**
   * Sets the user.
   *
   * @param  u  user
   */
  public void setUser(final User u)
  {
    user = u;
  }


  /**
   * Returns the credential.
   *
   * @return  user credential
   */
  public Credential getCredential()
  {
    return credential;
  }


  /**
   * Sets the credential.
   *
   * @param  c  user credential
   */
  public void setCredential(final Credential c)
  {
    credential = c;
  }


  /**
   * Returns the return attributes.
   *
   * @return  attributes to return
   */
  public String[] getReturnAttributes()
  {
    return returnAttributes;
  }


  /**
   * Sets the return attributes.
   *
   * @param  attrs  return attributes
   */
  public void setReturnAttributes(final String... attrs)
  {
    returnAttributes = ReturnAttributes.parse(attrs);
  }


  /**
   * Returns an authentication request initialized with the supplied request.
   *
   * @param  request  authentication request to read properties from
   *
   * @return  authentication request
   */
  public static AuthenticationRequest newAuthenticationRequest(final AuthenticationRequest request)
  {
    final AuthenticationRequest r = new AuthenticationRequest();
    r.setUser(request.getUser());
    r.setCredential(request.getCredential());
    r.setReturnAttributes(request.getReturnAttributes());
    return r;
  }


  @Override
  public String toString()
  {
    return
      String.format(
        "[%s@%d::user=%s, returnAttributes=%s]",
        getClass().getName(),
        hashCode(),
        user,
        Arrays.toString(returnAttributes));
  }
}
