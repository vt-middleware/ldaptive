---
layout: default
title: Ldaptive - add
redirect_from: "/docs/guide/operations/add/"
---

# Add Operation

Creates a new LDAP entry in the directory.

{% highlight java %}
{% include source/operations/add/1.java %}
{% endhighlight %}

The operation can be configured to throw so the result code doesn't need to be checked.

{% highlight java %}
{% include source/operations/add/2.java %}
{% endhighlight %}
