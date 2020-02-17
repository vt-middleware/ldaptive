---
layout: default
title: Ldaptive - modify
redirect_from: "/docs/guide/operations/modify/"
---

# Modify Operation

Changes the attributes of an LDAP entry.

{% highlight java %}
{% include source/operations/modify/1.java %}
{% endhighlight %}

The operation can be configured to throw so the result code doesn't need to be checked.

{% highlight java %}
{% include source/operations/modify/2.java %}
{% endhighlight %}
