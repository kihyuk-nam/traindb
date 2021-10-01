/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package traindb.jdbc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLWarning;
import org.verdictdb.VerdictResultStream;
import org.verdictdb.VerdictSingleResult;
import org.verdictdb.jdbc41.VerdictResultSet;
import traindb.TrainDBContext;
import traindb.common.TrainDBException;
import traindb.common.TrainDBLogger;
import traindb.engine.TrainDBExecContext;

public class TrainDBStatement implements java.sql.Statement {

  private TrainDBLogger LOG = TrainDBLogger.getLogger(this.getClass());

  Connection conn;
  TrainDBContext tc;
  TrainDBExecContext exCtx;
  VerdictSingleResult result;

  public TrainDBStatement(Connection conn, TrainDBContext context) {
    this.conn = conn;
    this.tc = context;
    this.exCtx = context.createTrainDBExecContext();
  }

  private Boolean checkStreamQuery(String query) {
    if (query.trim().toLowerCase().startsWith("stream")) {
      return true;
    }
    return false;
  }

  @Override
  public boolean execute(String sql) throws SQLException {
    try {
      result = exCtx.sql(sql, false);
      if (result == null) {
        return false;
      }
      return !result.isEmpty();
    } catch (TrainDBException e) {
      throw new SQLException(e);
    }
  }

  @Override
  public ResultSet executeQuery(String sql) throws SQLException {
    try {
      if (checkStreamQuery(sql)) {
        TrainDBStreamResultSet resultSet = new TrainDBStreamResultSet();
        sql = sql.replaceFirst("(?i)stream", "");
        VerdictResultStream resultStream = exCtx.streamsql(sql);
        ExecuteStream executeStream = new ExecuteStream(resultStream, resultSet, exCtx);
        resultSet.setRunnable(executeStream);
        new Thread(executeStream).start();
        return resultSet;
      }
      result = exCtx.sql(sql);
      return new VerdictResultSet(result);
    } catch (TrainDBException e) {
      throw new SQLException(e);
    }
  }

  @Override
  public int executeUpdate(String sql) throws SQLException {
    try {
      result = exCtx.sql(sql);
      return (int) result.getRowCount();
    } catch (TrainDBException e) {
      throw new SQLException(e);
    }
  }

  @Override
  public void close() throws SQLException {
    tc.removeTrainDBExecContext(exCtx);
  }

  @Override
  public boolean isClosed() throws SQLException {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public ResultSet getResultSet() throws SQLException {
    return new VerdictResultSet(result);
  }

  @Override
  public Connection getConnection() throws SQLException {
    return conn;
  }

  @Override
  public <T> T unwrap(Class<T> iface) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public boolean isWrapperFor(Class<?> iface) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public int getMaxFieldSize() throws SQLException {
    return 0; // no limit
  }

  @Override
  public void setMaxFieldSize(int max) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public int getMaxRows() throws SQLException {
    return 0; // no limit
  }

  @Override
  public void setMaxRows(int max) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void setEscapeProcessing(boolean enable) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public int getQueryTimeout() throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void setQueryTimeout(int seconds) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void cancel() throws SQLException {
    exCtx.terminate();
  }

  @Override
  public SQLWarning getWarnings() throws SQLException {
    return null;
  }

  @Override
  public void clearWarnings() throws SQLException {
  }

  @Override
  public void setCursorName(String name) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public int getUpdateCount() throws SQLException {
    return 0;
  }

  @Override
  public boolean getMoreResults() throws SQLException {
    return false;
  }

  @Override
  public int getFetchDirection() throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void setFetchDirection(int direction) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public int getFetchSize() throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void setFetchSize(int rows) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public int getResultSetConcurrency() throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public int getResultSetType() throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void addBatch(String sql) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void clearBatch() throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public int[] executeBatch() throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public boolean getMoreResults(int current) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public ResultSet getGeneratedKeys() throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public int executeUpdate(String sql, String[] columnNames) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public boolean execute(String sql, int[] columnIndexes) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public boolean execute(String sql, String[] columnNames) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public int getResultSetHoldability() throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public boolean isPoolable() throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void setPoolable(boolean poolable) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void closeOnCompletion() throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public boolean isCloseOnCompletion() throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  class ExecuteStream implements Runnable {

    VerdictResultStream resultStream;

    TrainDBStreamResultSet resultSet;

    TrainDBExecContext exCtx;

    ExecuteStream(VerdictResultStream resultStream, TrainDBStreamResultSet resultSet,
                  TrainDBExecContext exCtx) {
      this.resultStream = resultStream;
      this.resultSet = resultSet;
      this.exCtx = exCtx;
    }

    public void run() {
      while (!resultStream.isCompleted()) {
        VerdictSingleResult singleResult = resultStream.next();
        if (!resultStream.hasNext()) {
          synchronized ((Object) resultSet.hasReadAllQueryResults) {
            resultSet.appendSingleResult(singleResult);
            resultSet.setCompleted();
          }
          LOG.debug("Execution Completed\n");
        } else {
          resultSet.appendSingleResult(singleResult);
        }
      }
    }

    public void abort() {
      exCtx.abort();
    }
  }
}
