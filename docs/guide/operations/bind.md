---
layout: default
title: Ldaptive - bind
redirect_from: "/docs/guide/operations/bind/"
---

{% include relative %}

# Bind Operation

Authenticates to the LDAP and if successful, changes the authentication context of the connection. Further LDAP operations will be performed as the authenticated principal. Typically it's desirable to configure bind properties for all connections rather than on a connection-by-connection basis. The ConnectionConfig object contains properties such that the appropriate bind operation is automatically performed when a connection is opened. See the [connection documentation]({{ relative }}docs/guide/connections). If you need to support changing the authenticated principal after a connection has been opened, the following operations are available:

## Simple Bind

{% highlight java %}
{% include source/operations/bind/1.java %}
{% endhighlight %}

To perform an anonymous bind, execute a bind operation with an empty bind request.

## SASL Bind

The following SASL mechanisms are supported:
- CRAM-MD5
- DIGEST-MD5
- EXTERNAL
- GSSAPI

Note that the default JNDI provider supports all these mechanisms, but others may not. UnsupportedOperationException will be thrown by providers if a SASL mechanism is attempted but not supported.

{% highlight java %}
{% include source/operations/bind/2.java %}
{% endhighlight %}

