---
layout: default
title: Ldaptive - virtual list view
redirect_from: "/docs/guide/controls/virtuallistview/"
---

# Virtual List View

Request that the server return a subset of the full search results. Unlike the [PagedResultControl](docs/guide/controls/pagedresults), this control allows for moving forward and backward in the result set. See [LDAP Extensions for Scrolling View Browsing of Search Results](http://tools.ietf.org/html/draft-ietf-ldapext-ldapv3-vlv-09).

## Using the Virtual List View Client

{% highlight java %}
{% include source/controls/virtuallistview/1.java %}
{% endhighlight %}

## Provider Support

JNDI | JLDAP | Apache LDAP | UnboundID | OpenDJ
✓    | ✓     | ✓           | ✓         | ✓

{% include provider-support-legend.md %}

