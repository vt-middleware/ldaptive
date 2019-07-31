---
layout: default
title: Ldaptive - authentication
redirect_from: "/docs/guide/authentication/"
---

{% include relative %}

# Authentication

Authentication against an LDAP follows this multi-step process:

1. DN Resolution
2. Password Validation
3. Entry Resolution

{% highlight java %}
{% include source/authentication/1.java %}
{% endhighlight %}

## DN Resolution

Authentication typically begins by gathering an identifier from the user that matches some attribute on their LDAP entry. The DN of the LDAP entry can then be resolved using that identifier. The interface for DN resolvers looks like:

{% highlight java %}
public interface DnResolver
{
  String resolve(User user) throws LdapException;
}
{% endhighlight %}

Ldaptive provides the following DN resolution implementations:

### SearchDnResolver / PooledSearchDnResolver

Resolves an entry DN by performing an LDAP search. This resolver has the following properties:

Name | Default Value | Description
baseDn | "" | baseDN to search on
userFilter | null | search filter to execute; e.g. (mail={user})
userFilterArgs | null | search filter arguments
allowMultipleDns | false | whether to throw an exception if multiple entries are found with the search filter
subtreeSearch | false | whether a subtree search should be performed; by default a onelevel search is used

The {user} search filter argument is always assigned the user value from AuthenticationRequest#getUser(), so the userFilterArgs property only needs to be set when you specify custom arguments. Note that the SearchDnResolver will open and close a connection for every authentication.

If your directory does not allow anonymous access to the attribute used for DN resolution then you can configure a `BindConnectionInitializer`:

{% highlight java %}
{% include source/authentication/2.java %}
{% endhighlight %}

### FormatDnResolver

Resolves an entry DN by using String#format(String, Object[]). This resolver is typically used when an entry DN can be formatted directly from the user identifier. For instance, entry DNs of the form uid=dfisher,ou=people,dc=ldaptive,dc=org could be formatted from uid=%s,ou=people,dc=ldaptive,dc=org. This resolver has the following properties:

Name | Default Value | Description 
formatString | null | format of the DN
formatArgs | null | format arguments

The %1$s format argument is always assigned the user value from AuthenticationRequest#getUser(). So any arguments supplied in formatArgs will begin at %2$s.

### NoOpDnResolver

Does not perform any resolution. The user value from AuthenticationRequest#getUser() is returned as the DN. Used by authentication mechanisms that do not leverage an entry DN, such as DIGEST-MD5.

### AggregateDnResolver
Uses multiple DN resolvers to look up a user's DN. Each DN resolver is invoked on a separate thread. If multiple DNs are allowed then the first one retrieved is returned. Note that you must use the AggregateDnResolver#AuthenticationHandler inner class with this implementation. The labels provided must link a single DN resolver to a single authentication handler.

{% highlight java %}
{% include source/authentication/3.java %}
{% endhighlight %}

#### Use cases

* Multiple directories where each user existing only in one directory.
* Multiple directories with synchronized passwords. Users may exist in more than one directory, but their passwords are the same.

## Password Validation

Password validation is done by an AuthenticationHandler. It's purpose is to use the entry DN and the credential to determine if authentication should succeed. The interface for authentication handlers looks like:

{% highlight java %}
public interface AuthenticationHandler
{
  AuthenticationHandlerResponse authenticate(AuthenticationCriteria criteria) throws LdapException;
}
{% endhighlight %}

Ldaptive provides the following authentication handler implementations:

### BindAuthenticationHandler / PooledBindAuthenticationHandler

Authenticates an entry DN by performing an LDAP bind operation with that DN and the credential. This is the most common method of authentication against an LDAP and should be used in most circumstances. Note that the BindAuthenticationHandler will open and close a connection for every authentication.

### CompareAuthenticationHandler / PooledCompareAuthenticationHandler

Authenticates an entry DN by performing an LDAP compare operation on the userPassword attribute. This authentication handler should be used in cases where you do not have authorization to perform binds, but do have authorization to read the userPassword attribute. Note that the CompareAuthenticationHandler will open and close a connection for every authentication. This authentication handler has the following properties:

Name | Default Value | Description
passwordScheme | SHA | hash algorithm used by the LDAP for userPassword; Must be a valid Java MessageDigest algorithm.

## Entry Resolution

The authentication process always returns an LDAP entry for the DN that attempted authentication. By default a new LdapEntry is simply created with the DN, no LDAP interaction occurs. However, you may wish to return some or all of the user's LDAP attributes after authentication. The interface for entry resolvers looks like:

{% highlight java %}
public interface EntryResolver
{
  LdapEntry resolve(AuthenticationCriteria criteria, AuthenticationHandlerResponse response) throws LdapException;
}
{% endhighlight %}

The connection supplied is the connection that authentication occurred on and the criteria is what was used to authenticate. The entry resolver is only invoked if authentication succeeds. Ldaptive provides the following entry resolver implementations:

### SearchEntryResolver

Performs an object level search on the same connection that authentication occurred on and returns any requested attributes. This entry resolver has the following properties:

Name | Default Value | Description
returnAttributes | null | attributes to return from the search; null means return all attributes

### NoOpEntryResolver

Returns a new LdapEntry that contains only the authenticated DN. This is the default entry resolver.

## Response Processing

Authentication response handlers are an optional component of the authenticator which can be used to post process authentication responses. The interface for authentication response handlers looks like:

{% highlight java %}
public interface AuthenticationResponseHandler
{
  void process(AuthenticationResponse response);
}
{% endhighlight %}

Potential use cases for authentication response handlers include:

- injecting custom attributes into the response ldap entry
- processing the response message
- updating a database on success or failure
- sending notifications after a number of authentication failures
- storing authentication statistics

See the [account state]({{ relative }}docs/guide/authentication/accountstate.html) documentation for examples on how response handlers can be leveraged with various password policy implementations.

