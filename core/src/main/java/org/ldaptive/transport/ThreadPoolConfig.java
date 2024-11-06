/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.transport;

import org.ldaptive.AbstractConfig;

/**
 * Contains configuration properties for creating thread pools used by transports.
 *
 * @author  Middleware Services
 */
public final class ThreadPoolConfig extends AbstractConfig
{

  /** Enum to describe how thread pools should be shutdown. */
  public enum ShutdownStrategy {

    /** Shutdown thread pool when the connection is closed. */
    CONNECTION_CLOSE,

    /** Shutdown thread pool when the connection factory is closed. */
    CONNECTION_FACTORY_CLOSE,

    /** Never shutdown the thread pool, it is intended to run for the life of the JVM. */
    NEVER
  }

  /** Name of the thread pool. */
  private String threadPoolName;

  /** Number of I/O threads. 0 uses the default number of threads. */
  private int ioThreads;

  /**
   * Number of message threads.
   * 0 uses the default number of threads.
   * -1 means do not configure a separate message thread pool
   */
  private int messageThreads = -1;

  /** Thread pool shutdown strategy. */
  private ShutdownStrategy shutdownStrategy = ShutdownStrategy.CONNECTION_CLOSE;


  /**
   * Returns the thread pool name.
   *
   * @return  thread pool name
   */
  public String getThreadPoolName()
  {
    return threadPoolName;
  }


  /**
   * Sets the thread pool name.
   *
   * @param  name  thread pool name
   */
  public void setThreadPoolName(final String name)
  {
    assertMutable();
    logger.trace("setting threadPoolName: {}", name);
    threadPoolName = name;
  }


  /**
   * Returns the number of I/O threads.
   *
   * @return  number of I/O threads
   */
  public int getIoThreads()
  {
    return ioThreads;
  }


  /**
   * Sets the number of I/O threads.
   *
   * @param  count  number of I/O threads
   */
  public void setIoThreads(final int count)
  {
    assertMutable();
    if (count < 0) {
      throw new IllegalArgumentException("ioThreads must be greater than or equal to 0");
    }
    logger.trace("setting ioThreads: {}", count);
    ioThreads = count;
  }


  /**
   * Returns the number of message threads.
   *
   * @return  number of message threads
   */
  public int getMessageThreads()
  {
    return messageThreads;
  }


  /**
   * Sets the number of message threads.
   *
   * @param  count  number of message threads
   */
  public void setMessageThreads(final int count)
  {
    assertMutable();
    if (count < -1) {
      throw new IllegalArgumentException("messageThreads must be greater than or equal to -1");
    }
    logger.trace("setting messageThreads: {}", count);
    messageThreads = count;
  }


  /**
   * Returns the thread pool shutdown strategy.
   *
   * @return  thread pool shutdown strategy
   */
  public ShutdownStrategy getShutdownStrategy()
  {
    return shutdownStrategy;
  }


  /**
   * Sets thread pool shutdown strategy.
   *
   * @param  strategy  thread pool shutdown strategy
   */
  public void setShutdownStrategy(final ShutdownStrategy strategy)
  {
    assertMutable();
    logger.trace("setting shutdownStrategy: {}", strategy);
    shutdownStrategy = strategy;
  }


  @Override
  public String toString()
  {
    return "[" + getClass().getName() + "@" + hashCode() + "::" +
      "threadPoolName=" + threadPoolName + ", " +
      "ioThreads=" + ioThreads + ", " +
      "messageThreads=" + messageThreads + ", " +
      "shutdownStrategy=" + shutdownStrategy + "]";
  }


  /**
   * Creates a new transport config that uses a single I/O thread.
   *
   * @param  name  of the thread pool
   * @param  strategy  transport shutdown strategy
   *
   * @return  transport config
   */
  public static ThreadPoolConfig singleIoThread(final String name, final ShutdownStrategy strategy)
  {
    return builder()
      .threadPoolName(name)
      .ioThreads(1)
      .shutdownStrategy(strategy)
      .freeze()
      .build();
  }


  /**
   * Creates a new transport config that uses the default number of I/O threads and no message worker threads.
   *
   * @param  name  of the thread pool
   * @param  strategy  transport shutdown strategy
   *
   * @return  transport config
   */
  public static ThreadPoolConfig defaultIoThreads(final String name, final ShutdownStrategy strategy)
  {
    return builder()
      .threadPoolName(name)
      .ioThreads(0)
      .shutdownStrategy(strategy)
      .freeze()
      .build();
  }


  /**
   * Creates a builder for this class.
   *
   * @return  new builder
   */
  public static Builder builder()
  {
    return new Builder();
  }


  // CheckStyle:OFF
  public static final class Builder
  {


    private final ThreadPoolConfig object = new ThreadPoolConfig();


    private Builder() {}


    public Builder freeze()
    {
      object.freeze();
      return this;
    }


    public Builder threadPoolName(final String name)
    {
      object.setThreadPoolName(name);
      return this;
    }


    public Builder ioThreads(final int count)
    {
      object.setIoThreads(count);
      return this;
    }


    public Builder messageThreads(final int count)
    {
      object.setMessageThreads(count);
      return this;
    }


    public Builder shutdownStrategy(final ShutdownStrategy strategy)
    {
      object.setShutdownStrategy(strategy);
      return this;
    }


    public ThreadPoolConfig build()
    {
      return object;
    }
  }
  // CheckStyle:ON
}
