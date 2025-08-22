package at.itexperts.datasourceproxy;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.ttddyy.dsproxy.ExecutionInfo;
import net.ttddyy.dsproxy.QueryInfo;
import net.ttddyy.dsproxy.QueryType;
import net.ttddyy.dsproxy.listener.DataSourceQueryCountListener;
import net.ttddyy.dsproxy.listener.QueryUtils;
import net.ttddyy.dsproxy.listener.SingleQueryCountHolder;
import org.assertj.core.api.AbstractLongAssert;

public class QueryTracker extends DataSourceQueryCountListener {

  private final Set<QueryType> queryTypes;
  private final Map<QueryType, List<ParsedQuery>> queries;

  public QueryTracker(Set<QueryType> queryTypes) {
    setQueryCountStrategy(new SingleQueryCountHolder());

    this.queryTypes = Collections.synchronizedSet(new HashSet<>());
    this.queryTypes.addAll(queryTypes);

    this.queries = new ConcurrentHashMap<>();
  }

  public void track(QueryType... typesToTrack) {
    track(Set.of(typesToTrack));
  }

  public void track(Set<QueryType> typesToTrack) {
    clear();
    this.queryTypes.addAll(typesToTrack);
  }

  public void clear() {
    ((SingleQueryCountHolder) getQueryCountStrategy()).clear();
    this.queryTypes.clear();
    this.queries.clear();
  }

  @Override
  public void afterQuery(ExecutionInfo execInfo, List<QueryInfo> queryInfoList) {
    super.afterQuery(execInfo, queryInfoList);

    for (var queryInfo : queryInfoList) {
      var type = QueryUtils.getQueryType(queryInfo.getQuery());
      if (queryTypes.contains(type)) {
        queries
            .computeIfAbsent(type, _ -> new java.util.concurrent.CopyOnWriteArrayList<>())
            .add(toParsedQuery(queryInfo));
      }
    }
  }

  private static ParsedQuery toParsedQuery(QueryInfo queryInfo) {
    try {
      return new ParsedQuery(queryInfo.getQuery(), CCJSqlParserUtil.parse(queryInfo.getQuery()));
    } catch (JSQLParserException e) {
      throw new RuntimeException(e);
    }
  }

  private AbstractLongAssert<?> assertQueryCount(QueryType type, String tableName) {
    return assertThat(
        queries.getOrDefault(type, List.of()).stream()
            .filter(getTablePredicate(type, tableName))
            .count());
  }

  private static Predicate<ParsedQuery> getTablePredicate(QueryType type, String tableName) {
    return parsedQuery -> {
      switch (type) {
        case INSERT -> {
          if (parsedQuery.statement() instanceof net.sf.jsqlparser.statement.insert.Insert insert) {
            return insert.getTable().getName().equalsIgnoreCase(tableName);
          }
        }
        case DELETE -> {
          if (parsedQuery.statement() instanceof net.sf.jsqlparser.statement.delete.Delete delete) {
            return delete.getTable().getName().equalsIgnoreCase(tableName);
          }
        }
        case UPDATE -> {
          if (parsedQuery.statement() instanceof net.sf.jsqlparser.statement.update.Update update) {
            return update.getTable().getName().equalsIgnoreCase(tableName);
          }
        }
        default -> throw new IllegalArgumentException("Not supported for type: " + type);
      }
      return false;
    };
  }

  public AbstractLongAssert<?> assertInsertIntoCount(String tableName) {
    return assertQueryCount(QueryType.INSERT, tableName);
  }

  public AbstractLongAssert<?> assertDeleteCount(String tableName) {
    return assertQueryCount(QueryType.DELETE, tableName);
  }

  public AbstractLongAssert<?> assertUpdateCount(String tableName) {
    return assertQueryCount(QueryType.UPDATE, tableName);
  }
}
