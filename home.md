---
layout: default
title: Ldaptive
permalink: /
redirect_from: "/home/"
---

# LDAP library for Java.
Ldaptive is a simple, extensible Java API for interacting with LDAP servers. It was designed to provide easy LDAP integration for application developers.

## Features
* Netty based asynchronous networking
* Reactive API
* Connection pooling
* Authentication API with support for password policy
* JAAS modules for authentication and authorization
* SSL/startTLS support with easy configuration of trust and key material
* Input/output of LDIF
* Supported controls:
  * Authorization Identity (RFC 3829)
  * Content Synchronization (RFC 4533)
  * Entry Change Notification (draft-ietf-ldapext-psearch-03)
  * ManageDsaIT (RFC 3296)
  * Matched Values (RFC 3876)
  * Paged Results (RFC 2696)
  * Password Policy (draft-behera-ldap-password-policy-10 and draft-vchu-ldap-pwd-policy-00)
  * Persistent Search (draft-ietf-ldapext-psearch-03)
  * Proxy Authorization (RFC 4370)
  * Server Side Sorting (RFC 2891)
  * Session Tracking (draft-wahl-ldap-session-03)
  * Tree Delete (draft-armijo-ldap-treedelete)
  * Virtual List View (draft-ietf-ldapext-ldapv3-vlv-09)

## Quick Start Guide

### Searching
{% highlight java %}
{% include source/home/1.java %}
{% endhighlight %}

### StartTLS
{% highlight java %}
{% include source/home/2.java %}
{% endhighlight %}

### Binding
{% highlight java %}
{% include source/home/3.java %}
{% endhighlight %}

These search examples all leverage the SearchOperation class. For more details on searching and more control over the search operation in general, see the [search operation documentation](docs/guide/operations/search.html).

### Authentication
{% highlight java %}
{% include source/home/4.java %}
{% endhighlight %}

For more details on authentication, see the [authentication documentation](docs/guide/authentication.html).

## What changed between v1 and v2?
* Ldaptive is no longer a wrapper API around other Java libraries. It now includes it's own LDAP protocol implementation.
* The try-finally paradigm used for connection management has been removed. All operations use a connection factory and implement that pattern internally.
* Most classes now include static builders.
* The `SearchExecutor` functionality is now included in `SearchOperation`, that class has been removed.
* `SortBehavior` has been removed in favor of providing static sort methods on individual classes.
* Search result cache implementations have been removed. It's trivial for application to do their own caching.
* Transcoders have been moved to the `transcode` package.
* `SearchFilter` has been renamed `FilterTemplate`.
* Exceptions occur for I/O or configuration related problems, not LDAP server messages. To configure exception behavior see `setThrowCondition` on operations and `throwIf` on handles.

### [v1 docs](v1/) are still available.

