---
layout: default
title: Ldaptive - pooling
redirect_from: "/docs/guide/connections/pooling/"
---

# Pooling

Pooling ldap connections provides a way to mitigate the overhead of creating LDAP connections. A connection pool is controlled by the following properties:

Name | Default Value | Description
blockWaitTime | PT1M | length of time to wait for a connection to be returned from the pool
minPoolSize | 3 | size the pool should be initialized to and pruned to
maxPoolSize | 10 | maximum size the pool can grow to
validateOnCheckIn | false | whether connections should be validated when returned to the pool
validateOnCheckOut | false | whether connections should be validated when loaned out from the pool
validatePeriodically | false | whether connections should be validated periodically when the pool is idle
activator | null | class to activate connections as they are checked out of the pool for use
passivator | null | class to passivate connections as they are returned to the pool
validator | `SearchConnectionValidator` | class to validate that a connection is viable for use
pruneStrategy | `IdlePruneStrategy` | class to remove unneeded connections from the pool
validationExceptionHandler | `RetryValidationExceptionHandler` | class to handle validation exceptions when validateOnCheckOut is true

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

## Check out Validation

Connections can be validated before they are returned from `getConnection()`. This option may be useful if periodic validation is not a reliable mechanism to keep the connection pool healthy. When validation fails it is handled by a `ValidationExceptionHandler`.

### RetryValidationExceptionHandler

This is the default handler used for check out validation. This handler will ask the pool for another connection which will also be validated. This process will attempt `maxPoolSize + 1` attempts in order to find a valid connection or create a new one. If `blockWaitTime` occurs before a valid connection is returned a `ValidationException` is thrown.

## Check in Validation

Connections can be validated when they are returned to the pool. If a connection fails validation, it is not put back into the pool, instead it is closed.

## Pruning

Extra connections are removed from the pool using a *PruneStrategy*. The interface for prune strategy looks like:

{% highlight java %}
public interface PruneStrategy extends Consumer<Supplier<Iterator<PooledConnectionProxy>>>
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
ageTime | PT0S | time at which a connection has aged out and should be removed from the pool. Zero means no age is enforced.
prunePriority | -1 | for prioritized connections, these should be pruned using the age time. -1 means no prune priority is enforced.
prunePriorityFactor | 0 | in conjunction with prunePriority, used to provide inverse backoff with the configured ageTime. The formula for inverse backoff is ageTime / (factor * (priority + 1)). 0 means no prune priority factor is enabled.

Only available connections can be pruned from a pool, if a pool is under heavy load the prune strategy will not attempt to alter the pool size. Idle connections will be removed until the minimum pool size is reached. This may produce undesirable results if you're using a connection strategy that will connect to standby servers and you want to reduce the time that those specific connections stay in the pool. If you are using an `ActivePassiveConnectionStrategy` or a `DnsSrvConnectionStrategy` the prunePriority can be used to prioritize the removal of less desirable connections. For instance, by setting prunePriority=1 and ageTime=PT60M with an `ActivePassiveConnectionStrategy`, you can guarantee that connections with a priority = 1 are removed from the pool after 30 minutes (if load allows). Priority is zero based, so the first connection created by the strategy will have a priority of zero. The formula for ageTime in this scenario is ageTime / (priority + 1), so higher priority connections are pruned faster. Setting an ageTime may prune the pool below its minimum size, but it will attempt to grow back to its minimum at the end of the prune process.

A custom idle prune strategy can be configured by setting the prune strategy on the connection pool.

{% highlight java %}
{% include source/connections/pooling/5.java %}
{% endhighlight %}

