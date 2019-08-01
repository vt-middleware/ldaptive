---
layout: default_v1
title: Ldaptive - virtual list view
redirect_from: "/v1/docs/guide/controls/virtuallistview/"
---

{% include relative %}

# Virtual List View

Request that the server return a subset of the full search results. Unlike the [PagedResultControl]({{ relative }}docs/guide/controls/pagedresults), this control allows for moving forward and backward in the result set. See [LDAP Extensions for Scrolling View Browsing of Search Results](http://tools.ietf.org/html/draft-ietf-ldapext-ldapv3-vlv-09).

## Using the Virtual List View Client

{% highlight java %}
{% include source_v1/controls/virtuallistview/1.java %}
{% endhighlight %}

## Provider Support

| JNDI | JLDAP | Apache LDAP | UnboundID | OpenDJ
| <font color="#6aa84f">✓</font> | <font color="#6aa84f">✓</font> | <font color="#f1c232">✶</font> | <font color="#6aa84f">✓</font> | <font color="#6aa84f">✓</font>

{% include provider-support-legend.md %}

