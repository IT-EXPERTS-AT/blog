package at.itexperts.datasourceproxy;

import static org.assertj.core.api.Assertions.assertThat;

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

  private Set<QueryType> queryTypes;
  private Map<QueryType, List<ParsedQuery>> queries;

  public QueryTracker(Set<QueryType> queryTypes) {
    setQueryCountStrategy(new SingleQueryCountHolder());
    this.queryTypes = queryTypes;
    this.queries = new ConcurrentHashMap<>();
  }

  public void track(Set<QueryType> typesToTrack) {
    clear();
    this.queryTypes.addAll(typesToTrack);
  }

  private void clear() {
    ((SingleQueryCountHolder) getQueryCountStrategy()).clear();
    this.queryTypes.clear();
    this.queries.clear();
  }

  public void track(QueryType... typesToTrack) {
    track(Set.of(typesToTrack));
  }

  @Override
  public void afterQuery(ExecutionInfo execInfo, List<QueryInfo> queryInfoList) {
    super.afterQuery(execInfo, queryInfoList);

    for (var queryInfo : queryInfoList) {
      var type = QueryUtils.getQueryType(queryInfo.getQuery());
      if (queryTypes.contains(type)) {
        queries
            .computeIfAbsent(type, k -> new java.util.concurrent.CopyOnWriteArrayList<>())
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

  public AbstractLongAssert<?> assertInsertIntoCount(String tableName) {
    return assertThat(
        queries.getOrDefault(QueryType.INSERT, List.of()).stream()
            .filter(isInsertIntoTable(tableName))
            .count());
  }

  private static Predicate<ParsedQuery> isInsertIntoTable(String tableName) {
    return parsedQuery -> {
      if (parsedQuery.statement() instanceof net.sf.jsqlparser.statement.insert.Insert insert) {
        return insert.getTable().getName().equalsIgnoreCase(tableName);
      }
      return false;
    };
  }
}
