/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.ext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import org.ldaptive.AddOperation;
import org.ldaptive.AddRequest;
import org.ldaptive.AttributeModification;
import org.ldaptive.Connection;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.DeleteOperation;
import org.ldaptive.DeleteRequest;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.ldaptive.LdapException;
import org.ldaptive.ModifyOperation;
import org.ldaptive.ModifyRequest;
import org.ldaptive.OperationHandle;
import org.ldaptive.Result;
import org.ldaptive.ResultCode;
import org.ldaptive.SearchOperation;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchResponse;
import org.ldaptive.handler.ResultPredicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The merge operation performs the LDAP operations necessary to synchronize the data in an {@link LdapEntry} with its
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

  /** Search operation used to find the entry. */
  private SearchOperation searchOperation;

  /** Add operation used to add a new entry. */
  private AddOperation addOperation;

  /** Modify operation used to update an entry. */
  private ModifyOperation modifyOperation;

  /** Delete operation used to remove an entry. */
  private DeleteOperation deleteOperation;

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


  public SearchOperation getSearchOperation()
  {
    return searchOperation;
  }


  public void setSearchOperation(final SearchOperation operation)
  {
    searchOperation = operation;
  }


  public AddOperation getAddOperation()
  {
    return addOperation;
  }


  public void setAddOperation(final AddOperation operation)
  {
    addOperation = operation;
  }


  public ModifyOperation getModifyOperation()
  {
    return modifyOperation;
  }


  public void setModifyOperation(final ModifyOperation operation)
  {
    modifyOperation = operation;
  }


  public DeleteOperation getDeleteOperation()
  {
    return deleteOperation;
  }


  public void setDeleteOperation(final DeleteOperation operation)
  {
    deleteOperation = operation;
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
    final LdapEntry sourceEntry = request.getEntry();

    // search for existing entry
    final SearchOperation operation = searchOperation != null ?
      SearchOperation.copy(searchOperation) : new SearchOperation();
    operation.setConnectionFactory(connectionFactory);
    operation.setThrowCondition(
      r -> r.getResultCode() != ResultCode.SUCCESS && r.getResultCode() != ResultCode.NO_SUCH_OBJECT);
    final SearchResponse searchResult = operation.execute(
      SearchRequest.objectScopeSearchRequest(sourceEntry.getDn(), request.getSearchAttributes()));

    final Result result;
    if (searchResult.entrySize() == 0) {
      if (request.getDeleteEntry()) {
        logger.info("Target entry does not exist, no delete performed for request {}", request);
        result = null;
      } else {
        // entry does not exist, add it
        result = add(request, sourceEntry);
        if (throwCondition != null) {
          throwCondition.testAndThrow(result);
        }
      }
    } else if (request.getDeleteEntry()) {
      // delete entry
      result = delete(request, sourceEntry);
      if (throwCondition != null) {
        throwCondition.testAndThrow(result);
      }
    } else {
      // entry exists, merge attributes
      result = modify(request, sourceEntry, searchResult.getEntry());
      if (throwCondition != null) {
        throwCondition.testAndThrow(result);
      }
    }
    return result;
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
   *
   * @deprecated  use {@link #modify(MergeRequest, LdapEntry, LdapEntry)}
   */
  @Deprecated
  protected Result modify(
    final Connection conn,
    final MergeRequest request,
    final LdapEntry source,
    final LdapEntry target)
    throws LdapException
  {
    logger.warn("Use of deprecated method, connection parameter is ignored");
    return modify(request, source, target);
  }


  /**
   * Retrieves the attribute modifications from {@link LdapEntry#computeModifications(LdapEntry, LdapEntry)} and
   * executes a {@link ModifyOperation} with those results. If no modifications are necessary, no operation is
   * performed.
   *
   * @param  request  merge request
   * @param  source  ldap entry to merge into the LDAP
   * @param  target  ldap entry that exists in the LDAP
   *
   * @return  response of the modify operation or a null response if no operation is performed. If batching is
   *          enabled in the request, returns the response of the last operation performed
   *
   * @throws  LdapException  if an error occurs executing the modify operation
   */
  protected Result modify(
    final MergeRequest request,
    final LdapEntry source,
    final LdapEntry target)
    throws LdapException
  {
    final AttributeModification[] modifications =
      LdapEntry.computeModifications(source, target, request.isUseReplace());
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
        // if batching attribute values, create new attribute modifications as necessary
        if (request.getAttributeValuesBatchSize() > 0) {
          final List<AttributeModification> attrValuesModifications = new ArrayList<>(resultModifications.size());
          for (AttributeModification am : resultModifications) {
            if (request.getAttributeValuesBatchSize() < am.getAttribute().size()) {
              divideList(
                new ArrayList<>(am.getAttribute().getBinaryValues()),
                request.getAttributeValuesBatchSize(),
                values -> attrValuesModifications.add(
                  new AttributeModification(
                    am.getOperation(),
                    LdapAttribute.builder().name(am.getAttribute().getName()).binaryValues(values).build())));
            } else {
              attrValuesModifications.add(am);
            }
          }
          if (!attrValuesModifications.equals(resultModifications)) {
            resultModifications.clear();
            resultModifications.addAll(attrValuesModifications);
          }
        }
        logger.info(
          "Modifying target entry {} with modifications {} from source entry {} for request {}",
          target,
          resultModifications,
          source,
          request);

        Result result = null;
        // if batching modifications, execute a modify operation for each batch
        if (request.getModificationBatchSize() > 0 && request.getModificationBatchSize() < resultModifications.size()) {
          final List<List<AttributeModification>> batchedResultModifications =
            new ArrayList<>(resultModifications.size());
          divideList(resultModifications, request.getModificationBatchSize(), batchedResultModifications::add);
          for (List<AttributeModification> batch : batchedResultModifications) {
            final ModifyOperation operation = modifyOperation != null ?
              ModifyOperation.copy(modifyOperation) : new ModifyOperation();
            operation.setConnectionFactory(connectionFactory);
            result = operation.execute(
              ModifyRequest.builder()
                .dn(target.getDn())
                .modifications(batch.toArray(AttributeModification[]::new))
                .build());
          }
        } else {
          final ModifyOperation operation = modifyOperation != null ?
            ModifyOperation.copy(modifyOperation) : new ModifyOperation();
          operation.setConnectionFactory(connectionFactory);
          result = operation.execute(
            ModifyRequest.builder()
              .dn(target.getDn())
              .modifications(resultModifications.toArray(AttributeModification[]::new))
              .build());
        }
        logger.info(
          "Modified target entry {} with modifications {} from source entry {} for request {}",
          target,
          resultModifications,
          source,
          request);
        return result;
      }
    }
    logger.info(
      "Target entry {} equals source entry {}, no modification performed for request {}",
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
   *
   * @deprecated  use {@link #add(MergeRequest, LdapEntry)}
   */
  @Deprecated
  protected Result add(final Connection conn, final MergeRequest request, final LdapEntry entry)
    throws LdapException
  {
    logger.warn("Use of deprecated method, connection parameter is ignored");
    return add(request, entry);
  }


  /**
   * Executes an {@link AddOperation} for the supplied entry.
   *
   * @param  request  merge request
   * @param  entry  to add to the LDAP
   *
   * @return  response of the add operation
   *
   * @throws  LdapException  if an error occurs executing the add operation
   */
  protected Result add(final MergeRequest request, final LdapEntry entry)
    throws LdapException
  {
    final AddOperation operation = addOperation != null ?
      AddOperation.copy(addOperation) : new AddOperation();
    operation.setConnectionFactory(connectionFactory);
    final Result result = operation.execute(
      AddRequest.builder()
        .dn(entry.getDn())
        .attributes(entry.getAttributes())
        .build());
    logger.info("Added entry {} for request {}", entry, request);
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
   *
   * @deprecated  use {@link #delete(MergeRequest, LdapEntry)}
   */
  @Deprecated
  protected Result delete(final Connection conn, final MergeRequest request, final LdapEntry entry)
    throws LdapException
  {
    logger.warn("Use of deprecated method, connection parameter is ignored");
    return delete(request, entry);
  }


  /**
   * Executes a {@link DeleteOperation} for the supplied entry.
   *
   * @param  request  merge request
   * @param  entry  to delete from the LDAP
   *
   * @return  response of the delete operation
   *
   * @throws  LdapException  if an error occurs executing the deleting operation
   */
  protected Result delete(final MergeRequest request, final LdapEntry entry)
    throws LdapException
  {
    final DeleteOperation operation = deleteOperation != null ?
      DeleteOperation.copy(deleteOperation) : new DeleteOperation();
    operation.setConnectionFactory(connectionFactory);
    final Result result = operation.execute(new DeleteRequest(entry.getDn()));
    logger.info("Delete entry {} for request {}", entry, request);
    return result;
  }


  /**
   * Divides the supplied list into sub lists by the supplied divisor and passes each sub list to the consumer.
   *
   * @param  <T>  type of list element
   * @param  list  to divide
   * @param  divisor  to divide list by
   * @param  consumer  to process each sub list
   */
  private <T> void divideList(final List<T> list, final int divisor, final Consumer<List<T>> consumer)
  {
    for (int i = 0; i < list.size() / divisor; i++) {
      final int start = i * divisor;
      final int end = (i + 1) * divisor;
      consumer.accept(list.subList(start, end > list.size() ? list.size() : end));
    }
  }
}
