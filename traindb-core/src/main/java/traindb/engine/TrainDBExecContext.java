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

package traindb.engine;

import java.util.List;
import org.verdictdb.VerdictSingleResult;
import org.verdictdb.connection.DbmsConnection;
import org.verdictdb.coordinator.ExecutionContext;
import traindb.catalog.CatalogStore;
import traindb.common.TrainDBConfiguration;
import traindb.common.TrainDBException;
import traindb.common.TrainDBLogger;
import traindb.schema.SchemaManager;
import traindb.sql.TrainDBSql;
import traindb.sql.TrainDBSqlCommand;
import traindb.sql.TrainDBSqlRunner;


public class TrainDBExecContext {

  private TrainDBLogger LOG = TrainDBLogger.getLogger(this.getClass());

  TrainDBSqlRunner engine;
  String contextId;
  long serialNumber;
  ExecutionContext executionContext;

  public TrainDBExecContext(
      DbmsConnection conn, CatalogStore catalogStore, SchemaManager schemaManager,
      String contextId, long serialNumber, TrainDBConfiguration conf) {
    engine = new TrainDBQueryEngine(conn, catalogStore, schemaManager, conf);
    this.contextId = contextId;
    this.serialNumber = serialNumber;
  }

  public VerdictSingleResult sql(String query) throws TrainDBException {
    return this.sql(query, true);
  }

  public VerdictSingleResult sql(String query, boolean getResult) throws TrainDBException {
    LOG.debug("query=" + query);

    // Check input query with TrainDB sql grammar
    List<TrainDBSqlCommand> commands = null;
    try {
      commands = TrainDBSql.parse(query);
    } catch (Exception e) {
      if (commands != null) {
        commands.clear();
      }
      LOG.debug("not a TrainDB statement -> bypass");
    }

    if (commands != null && commands.size() > 0) {
      try {
        return TrainDBSql.run(commands.get(0), engine);
      } catch (Exception e) {
        throw new TrainDBException(
            "failed to run statement: " + query + "\nerror msg: " + e.getMessage());
      }
    }

    // Pass input query to VerdictDB
    try {
      return engine.processQuery(query);
    } catch (Exception e) {
      throw new TrainDBException(e.getMessage());
    }
  }

  public void abort() {
  }

  public void terminate() {
  }

}
