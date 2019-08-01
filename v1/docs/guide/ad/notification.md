---
layout: default_v1
title: Ldaptive - notification
redirect_from: "/v1/docs/guide/ad/notification/"
---

# Directory Notification

Active Directory provides a control for clients to register to receive changes notifications. Note the following constraints when configuring your search:

scope | Must be one-level or object-level
filter | Must be '(objectClass=*)'
attributes | list of attributes to be returned when a change occurs. Does not specify attributes which will generate notifications.

A notification client is provided to encapsulate the asychronous search and expose a blocking queue.

## NotificationClient

{% highlight java %}
{% include source_v1/ad/notification/1.java %}
{% endhighlight %}

#### Useful links

- [Change Notifications in Active Directory](http://msdn.microsoft.com/en-us/library/windows/desktop/aa772153%28v=vs.85%29.aspx)

