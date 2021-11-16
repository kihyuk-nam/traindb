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

package traindb.schema;

import static org.apache.calcite.linq4j.Nullness.castNonNull;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.apache.calcite.adapter.jdbc.JdbcConvention;
import org.apache.calcite.adapter.jdbc.JdbcImplementor;
import org.apache.calcite.adapter.jdbc.JdbcRel;
import org.apache.calcite.plan.RelOptCluster;
import org.apache.calcite.plan.RelOptTable;
import org.apache.calcite.plan.RelTraitSet;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.core.TableScan;

/**
 * Relational expression representing a scan of a table in a JDBC data source.
 */
public class JdbcTableScan extends TableScan implements JdbcRel {
  public final TrainDBJdbcTable jdbcTable;

  protected JdbcTableScan(RelOptCluster cluster, RelOptTable table, TrainDBJdbcTable jdbcTable,
                          JdbcConvention jdbcConvention) {
    super(cluster, cluster.traitSetOf(jdbcConvention), ImmutableList.of(), table);
    this.jdbcTable = jdbcTable;
  }

  @Override
  public RelNode copy(RelTraitSet traitSet, List<RelNode> inputs) {
    assert inputs.isEmpty();
    return new JdbcTableScan(
        getCluster(), table, jdbcTable, (JdbcConvention) castNonNull(getConvention()));
  }

  @Override
  public JdbcImplementor.Result implement(JdbcImplementor implementor) {
    return implementor.result(jdbcTable.tableName(),
        ImmutableList.of(JdbcImplementor.Clause.FROM), this, null);
  }
}