---
layout: default
title: Ldaptive - bind
redirect_from: "/docs/guide/operations/bind/"
---

{% include relative %}

# Bind Operation

Authenticates to the LDAP and if successful, changes the authentication context of the connection. Further LDAP operations will be performed as the authenticated principal. Typically it's desirable to configure bind properties for all connections rather than on a connection-by-connection basis. The ConnectionConfig object contains properties such that the appropriate bind operation is automatically performed when a connection is opened. See the [connection documentation]({{ relative }}docs/guide/connections.html). If you need to support changing the authenticated principal after a connection has been opened, the following operations are available:

## Simple Bind

{% highlight java %}
{% include source/operations/bind/1.java %}
{% endhighlight %}

Note that the `DefaultConnectionFactory` implementation will close the connection when the operation is complete and may not be a suitable choice depending on your use case.

## Anonymous Bind

{% highlight java %}
{% include source/operations/bind/2.java %}
{% endhighlight %}

## External Bind

{% highlight java %}
{% include source/operations/bind/3.java %}
{% endhighlight %}

## BindConnectionInitializer

It is often desirable to bind as a specific principal immediately after a connection is opened. A `BindConnectionInitializer` is configured on a `ConnectionConfig` and can be used for this purpose.

### Simple Bind initializer

{% highlight java %}
{% include source/operations/bind/4.java %}
{% endhighlight %}

### External Bind initializer

{% highlight java %}
{% include source/operations/bind/5.java %}
{% endhighlight %}

### GSSAPI Bind initializer

Note that arbitrary SASL properties can be passed into the `SaslConfig` object. In addition, properties that are prefixed with _org.ldaptive.sasl.gssapi.jaas._ are passed to the JAAS module for GSSAPI.

{% highlight java %}
{% include source/operations/bind/6.java %}
{% endhighlight %}
