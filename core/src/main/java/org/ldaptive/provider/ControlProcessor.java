/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.provider;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import org.ldaptive.control.RequestControl;
import org.ldaptive.control.ResponseControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for invoking provider specific control processors.
 *
 * @param  <T>  type of provider specific control
 *
 * @author  Middleware Services
 */
public class ControlProcessor<T>
{

  /** Logger for this class. */
  protected final Logger logger = LoggerFactory.getLogger(getClass());

  /** Control handler. */
  private final ControlHandler<T> controlHandler;


  /**
   * Creates a new control processor.
   *
   * @param  handler  to handle controls with
   */
  public ControlProcessor(final ControlHandler<T> handler)
  {
    controlHandler = handler;
  }


  /**
   * Converts the supplied request controls to a provider specific request controls.
   *
   * @param  requestControls  to convert
   *
   * @return  provider specific controls
   */
  @SuppressWarnings("unchecked")
  public T[] processRequestControls(final RequestControl[] requestControls)
  {
    if (requestControls == null || requestControls.length == 0) {
      return null;
    }
    logger.trace("processing request controls: {}", new Object[] {requestControls});

    final List<T> providerCtls = new ArrayList<>(requestControls.length);
    for (RequestControl c : requestControls) {
      final T providerCtl = processRequest(c);
      if (providerCtl != null) {
        providerCtls.add(providerCtl);
      }
    }
    logger.trace("produced provider request controls: {}", providerCtls);
    return
      !providerCtls.isEmpty()
      ? providerCtls.toArray((T[]) Array.newInstance(controlHandler.getControlType(), providerCtls.size()))
      : null;
  }


  /**
   * Converts the supplied control to a provider control.
   *
   * @param  ctl  to convert
   *
   * @return  provider control
   */
  protected T processRequest(final RequestControl ctl)
  {
    if (ctl == null) {
      return null;
    }

    final T providerCtl = controlHandler.handleRequest(ctl);
    if (providerCtl == null) {
      logger.info("Unsupported request control {}", ctl);
    }
    return providerCtl;
  }


  /**
   * Converts the supplied provider controls to a response controls. The supplied request controls were used to produce
   * the response.
   *
   * @param  responseControls  to convert
   *
   * @return  controls
   */
  public ResponseControl[] processResponseControls(final T[] responseControls)
  {
    if (responseControls == null || responseControls.length == 0) {
      return null;
    }
    logger.trace("processing provider response controls: {}", responseControls);

    final List<ResponseControl> ctls = new ArrayList<>(responseControls.length);
    for (T c : responseControls) {
      final ResponseControl ctl = processResponse(c);
      if (ctl != null) {
        ctls.add(ctl);
      }
    }
    logger.trace("produced response controls: {}", ctls);
    return ctls.toArray(new ResponseControl[ctls.size()]);
  }


  /**
   * Converts the supplied provider control to a control.
   *
   * @param  providerCtl  to convert
   *
   * @return  control
   */
  protected ResponseControl processResponse(final T providerCtl)
  {
    if (providerCtl == null) {
      return null;
    }

    final ResponseControl ctl = controlHandler.handleResponse(providerCtl);
    if (ctl == null) {
      logger.info("Unsupported response control {}", providerCtl);
    }
    return ctl;
  }
}
