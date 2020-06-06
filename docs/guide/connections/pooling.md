---
layout: default
title: Ldaptive - pooling
redirect_from: "/docs/guide/connections/pooling/"
---

# Pooling

Pooling ldap connections provides a way to mitigate the overhead of creating LDAP connections. A connection pool is controlled by the following properties:

Name | Default Value | Description
minPoolSize | 3 | size the pool should be initialized to and pruned to
maxPoolSize | 10 | maximum size the pool can grow to
validateOnCheckIn | false | whether connections should be validated when returned to the pool
validateOnCheckOut | false | whether connections should be validated when loaned out from the pool
validatePeriodically | false | whether connections should be validated periodically when the pool is idle
activator | null | class to activate connections as they are checked out of the pool for use
passivator | null | class to passivate connections as they are returned to the pool
validator | `SearchConnectionValidator` | class to validate that a connection is viable for use
pruneStrategy | `IdlePruneStrategy` | class to remove unneeded connections from the pool

Ldaptive provides a `PooledConnectionFactory` implementation which uses a blocking connection pool.

{% highlight java %}
{% include source/connections/pooling/1.java %}
{% endhighlight %}

Unlike a `DefaultConnectionFactory`, this implementation must be initialized before use and closed to free the pool resources.

## Validation

Ldaptive supports validating a connection on check out and check in. By default a `SearchConnectionValidator` is used if validation is configured. Connections that fail validation are evicted from the pool. The interface for validators looks like:

{% highlight java %}
public interface ConnectionValidator extends Function<Connection, Boolean>
{
  /** Returns the interval at which the validation task will be executed. */
  Duration getValidatePeriod();

  /** Returns the time at which a validate operation should be abandoned. */
  Duration getValidateTimeout();
}
{% endhighlight %}

Ldaptive provides the following validator implementations:

### CompareConnectionValidator

Validates a connection by performing a compare operation. By default this validator performs a rootDSE compare on objectClass: top.

{% highlight java %}
{% include source/connections/pooling/2.java %}
{% endhighlight %}

Validation is successful if the compare operation returns any result.

### SearchConnectionValidator

Validates a connection by performing a search operation. By default this validator performs an object level rootDSE search for _(objectClass=*)_.

{% highlight java %}
{% include source/connections/pooling/3.java %}
{% endhighlight %}

Validation is successful if the search operation returns any result.

## Periodic Validation

You can also configure validation to occur when the pool is idle instead of during check outs and check ins. By performing validation periodically rather than for every checkIn/checkOut you will improve performance during peak periods of load. This functionality can also serve as a keep-alive for long lived connections.

{% highlight java %}
{% include source/connections/pooling/4.java %}
{% endhighlight %}

## Pruning

Extra connections are removed from the pool using a *PruneStrategy*. The interface for prune strategy looks like:

{% highlight java %}
public interface PruneStrategy extends Function<PooledConnectionProxy, Boolean>
{
  /** Returns the statistical sample size to store for this prune strategy. */
  int getStatisticsSize();

  /** Returns the prune period for this prune strategy. */
  Duration getPrunePeriod();
}
{% endhighlight %}

### IdlePruneStrategy

Prunes connections from the pool based on how long they have been idle. This is the default prune strategy and it has the following properties:

Name | Default Value | Description
prunePeriod | PT5M | period at which pool should be pruned
idleTime | PT10M | time at which a connection should be considered idle and become a candidate for removal from the pool

A custom idle prune strategy can be configured by setting the prune strategy on the connection pool.

{% highlight java %}
{% include source/connections/pooling/5.java %}
{% endhighlight %}

