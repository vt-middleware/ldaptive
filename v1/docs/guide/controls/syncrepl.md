---
layout: default_v1
title: Ldaptive - syncrepl
redirect_from: "/v1/docs/guide/controls/syncrepl/"
---

# Content Synchronization (Sync Repl)

Request that the server send the client updates in order for the client to stay in sync with the server. See [RFC 4533](http://www.ietf.org/rfc/rfc4533.txt).

Note that these examples use the _DefaultCookieManager which stores cookie data in memory_. Most implementers will want to provider a custom implementation to persist cookie data. The interface for CookieManager looks like:

{% highlight java %}
public interface CookieManager
{
  byte[] readCookie(); // invoked when the operation begins

  void writeCookie(byte[] cookie); // invoked whenever a cookie is seen in a control or message
}
{% endhighlight %}

## Refresh Only

Creates a SyncReplClient to use for a content synchronization without an existing cookie. By setting the persist property to false we expect the server will send the client all content followed by a response. Which means we do not expect to block indefinitely.

{% highlight java %}
{% include source_v1/controls/syncrepl/1.java %}
{% endhighlight %}

## Refresh and Persist

Creates a SyncReplClient to use for a content synchronization without an existing cookie. By setting the persist property to true we expect the server will continue sending us content updates until the operation is cancelled.

{% highlight java %}
{% include source_v1/controls/syncrepl/2.java %}
{% endhighlight %}

## Provider Support

| JNDI | JLDAP | Apache LDAP | UnboundID | OpenDJ
| <font color="#cc0000">✗</font> | <font color="#6aa84f">✓</font> | <font color="#6aa84f">✓</font> | <font color="#6aa84f">✓</font> | <font color="#f1c232">✶</font>

{% include provider-support-legend.md %}

