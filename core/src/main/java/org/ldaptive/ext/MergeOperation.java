/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.ext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.ldaptive.AddOperation;
import org.ldaptive.AddRequest;
import org.ldaptive.AttributeModification;
import org.ldaptive.Connection;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.DeleteOperation;
import org.ldaptive.DeleteRequest;
import org.ldaptive.LdapEntry;
import org.ldaptive.LdapException;
import org.ldaptive.ModifyOperation;
import org.ldaptive.ModifyRequest;
import org.ldaptive.OperationHandle;
import org.ldaptive.Result;
import org.ldaptive.ResultCode;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchResponse;
import org.ldaptive.handler.ResultPredicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The merge operation performs the LDAP operations necessary to synchronize the data in an {@link LdapEntry} with it's
 * corresponding entry in the LDAP. The following logic is executed:
 *
 * <ul>
 *   <li>if the entry does not exist in the LDAP, execute an add</li>
 *   <li>if the request is for a delete, execute a delete</li>
 *   <li>if the entry exists in the LDAP, execute a modify</li>
 * </ul>
 *
 * <p>{@link LdapEntry#computeModifications(LdapEntry, LdapEntry)} is used to determine the list of attribute
 * modifications that are necessary to perform the merge. Either {@link MergeRequest#getIncludeAttributes()} or {@link
 * MergeRequest#getExcludeAttributes()} will be used, but not both.</p>
 *
 * @author  Middleware Services
 */
public class MergeOperation
{

  /** Logger for this class. */
  private final Logger logger = LoggerFactory.getLogger(getClass());

  /** Connection factory. */
  private ConnectionFactory connectionFactory;

  /** Function to test results. */
  private ResultPredicate throwCondition;


  /**
   * Default constructor.
   */
  public MergeOperation() {}


  /**
   * Creates a new merge operation.
   *
   * @param  factory  connection factory
   */
  public MergeOperation(final ConnectionFactory factory)
  {
    connectionFactory = factory;
  }


  public ConnectionFactory getConnectionFactory()
  {
    return connectionFactory;
  }


  public void setConnectionFactory(final ConnectionFactory factory)
  {
    connectionFactory = factory;
  }


  public ResultPredicate getThrowCondition()
  {
    return throwCondition;
  }


  public void setThrowCondition(final ResultPredicate function)
  {
    throwCondition = function;
  }


  /**
   * Executes a merge request. See {@link OperationHandle#execute()}.
   *
   * @param  request  merge request
   *
   * @return  merge result
   *
   * @throws  LdapException  if the connection cannot be opened
   */
  public Result execute(final MergeRequest request)
    throws LdapException
  {
    try (Connection conn = connectionFactory.getConnection()) {
      conn.open();

      final LdapEntry sourceEntry = request.getEntry();

      // search for existing entry
      final SearchResponse searchResult = conn.operation(
        SearchRequest.objectScopeSearchRequest(sourceEntry.getDn(), request.getSearchAttributes())).execute();
      if (searchResult.getResultCode() != ResultCode.SUCCESS &&
          searchResult.getResultCode() != ResultCode.NO_SUCH_OBJECT) {
        throw new LdapException(
          searchResult.getResultCode(),
          String.format(
            "Error searching for entry: %s, response did not return success or no_such_object: %s",
            sourceEntry,
            searchResult));
      }

      final Result result;
      if (searchResult.entrySize() == 0) {
        if (request.getDeleteEntry()) {
          logger.info("target entry does not exist, no delete performed for request {}", request);
          result = null;
        } else {
          // entry does not exist, add it
          result = add(conn, request, sourceEntry);
          if (throwCondition != null) {
            throwCondition.testAndThrow(result);
          }
        }
      } else if (request.getDeleteEntry()) {
        // delete entry
        result = delete(conn, request, sourceEntry);
        if (throwCondition != null) {
          throwCondition.testAndThrow(result);
        }
      } else {
        // entry exists, merge attributes
        result = modify(conn, request, sourceEntry, searchResult.getEntry());
        if (throwCondition != null) {
          throwCondition.testAndThrow(result);
        }
      }
      return result;
    }
  }


  /**
   * Retrieves the attribute modifications from {@link LdapEntry#computeModifications(LdapEntry, LdapEntry)} and
   * executes a {@link ModifyOperation} with those results. If no modifications are necessary, no operation is
   * performed.
   *
   * @param  conn  connection to perform operation on
   * @param  request  merge request
   * @param  source  ldap entry to merge into the LDAP
   * @param  target  ldap entry that exists in the LDAP
   *
   * @return  response of the modify operation or an empty response if no operation is performed
   *
   * @throws  LdapException  if an error occurs executing the modify operation
   */
  protected Result modify(
    final Connection conn,
    final MergeRequest request,
    final LdapEntry source,
    final LdapEntry target)
    throws LdapException
  {
    final AttributeModification[] modifications = LdapEntry.computeModifications(source, target);
    if (modifications != null && modifications.length > 0) {
      final List<AttributeModification> resultModifications = new ArrayList<>(modifications.length);
      final String[] includeAttrs = request.getIncludeAttributes();
      final String[] excludeAttrs = request.getExcludeAttributes();
      if (includeAttrs != null && includeAttrs.length > 0) {
        final List<String> l = Arrays.asList(includeAttrs);
        for (AttributeModification am : modifications) {
          if (l.contains(am.getAttribute().getName())) {
            resultModifications.add(am);
          }
        }
      } else if (excludeAttrs != null && excludeAttrs.length > 0) {
        final List<String> l = Arrays.asList(excludeAttrs);
        for (AttributeModification am : modifications) {
          if (!l.contains(am.getAttribute().getName())) {
            resultModifications.add(am);
          }
        }
      } else {
        Collections.addAll(resultModifications, modifications);
      }
      if (!resultModifications.isEmpty()) {
        logger.info(
          "modifying target entry {} with modifications {} from source entry " +
          "{} for request {}",
          target,
          resultModifications,
          source,
          request);

        final Result result = conn.operation(
          ModifyRequest.builder()
            .dn(target.getDn())
            .modificiations(resultModifications.toArray(new AttributeModification[0]))
            .build()).execute();
        logger.info(
          "modified target entry {} with modifications {} from source entry " +
          "{} for request {}",
          target,
          resultModifications,
          source,
          request);
        return result;
      }
    }
    logger.info(
      "target entry {} equals source entry {}, no modification performed for " +
      "request {}",
      target,
      source,
      request);
    return null;
  }


  /**
   * Executes an {@link AddOperation} for the supplied entry.
   *
   * @param  conn  connection to perform operation on
   * @param  request  merge request
   * @param  entry  to add to the LDAP
   *
   * @return  response of the add operation
   *
   * @throws  LdapException  if an error occurs executing the add operation
   */
  protected Result add(final Connection conn, final MergeRequest request, final LdapEntry entry)
    throws LdapException
  {
    final Result result = conn.operation(
      AddRequest.builder()
        .dn(entry.getDn())
        .attributes(entry.getAttributes())
        .build()).execute();
    logger.info("added entry {} for request {}", entry, request);
    return result;
  }


  /**
   * Executes a {@link DeleteOperation} for the supplied entry.
   *
   * @param  conn  connection to perform operation on
   * @param  request  merge request
   * @param  entry  to delete from the LDAP
   *
   * @return  response of the delete operation
   *
   * @throws  LdapException  if an error occurs executing the deleting operation
   */
  protected Result delete(final Connection conn, final MergeRequest request, final LdapEntry entry)
    throws LdapException
  {
    final Result result = conn.operation(new DeleteRequest(entry.getDn())).execute();
    logger.info("delete entry {} for request {}", entry, request);
    return result;
  }
}
