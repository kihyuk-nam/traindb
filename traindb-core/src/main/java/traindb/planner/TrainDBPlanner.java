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

package traindb.planner;

import org.apache.calcite.plan.Context;
import org.apache.calcite.plan.ConventionTraitDef;
import org.apache.calcite.plan.RelOptCostFactory;
import org.apache.calcite.plan.RelOptUtil;
import org.apache.calcite.plan.volcano.VolcanoPlanner;
import org.apache.calcite.rel.RelCollationTraitDef;
import org.apache.calcite.runtime.Hook;
import org.checkerframework.checker.nullness.qual.Nullable;
import traindb.catalog.CatalogContext;
import traindb.planner.rules.TrainDBRules;

public class TrainDBPlanner extends VolcanoPlanner {

  private CatalogContext catalogContext;

  public TrainDBPlanner(CatalogContext catalogContext) {
    this(catalogContext, null, null);
  }

  public TrainDBPlanner(CatalogContext catalogContext,
                        @Nullable RelOptCostFactory costFactory,
                        @Nullable Context externalContext) {
    super(costFactory, externalContext);
    this.catalogContext = catalogContext;
    initPlanner();
  }

  public void initPlanner() {
    // TrainDB rules
    addRule(TrainDBRules.APPROX_AGGREGATE_SYNOPSIS);

    RelOptUtil.registerDefaultRules(this, true, Hook.ENABLE_BINDABLE.get(false));
    addRelTraitDef(ConventionTraitDef.INSTANCE);
    addRelTraitDef(RelCollationTraitDef.INSTANCE);
    setTopDownOpt(false);

    Hook.PLANNER.run(this); // allow test to add or remove rules
  }
}
