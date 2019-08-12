---
layout: default
title: Ldaptive - persistent search
redirect_from: "/docs/guide/controls/psearch/"
---

# Persistent Search

Request that the server keep a search operation open and send changes to the client. See [A Simple LDAP Change Notification Mechanism](http://tools.ietf.org/id/draft-ietf-ldapext-psearch-03.txt).

## Using the Persistent Search Client

{% highlight java %}
{% include source/controls/psearch/1.java %}
{% endhighlight %}
