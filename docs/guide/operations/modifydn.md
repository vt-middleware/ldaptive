---
layout: default
title: Ldaptive - modify dn
redirect_from: "/docs/guide/operations/modifydn/"
---

# Modify DN Operation

Changes the DN of an LDAP entry.

{% highlight java %}
{% include source/operations/modifydn/1.java %}
{% endhighlight %}

The operation can be configured to throw so the result code doesn't need to be checked.

{% highlight java %}
{% include source/operations/modifydn/2.java %}
{% endhighlight %}
