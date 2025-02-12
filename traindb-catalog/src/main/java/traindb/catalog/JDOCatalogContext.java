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

package traindb.catalog;

import java.util.Collection;
import java.util.List;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.Transaction;
import org.checkerframework.checker.nullness.qual.Nullable;
import traindb.catalog.pm.MModel;
import traindb.catalog.pm.MModeltype;
import traindb.catalog.pm.MQueryLog;
import traindb.catalog.pm.MSynopsis;
import traindb.catalog.pm.MTask;
import traindb.common.TrainDBLogger;

public final class JDOCatalogContext implements CatalogContext {

  private static final TrainDBLogger LOG = TrainDBLogger.getLogger(JDOCatalogContext.class);
  private final PersistenceManager pm;

  public JDOCatalogContext(PersistenceManager persistenceManager) {
    pm = persistenceManager;
  }

  @Override
  public boolean modeltypeExists(String name) {
    return getModeltype(name) != null;
  }

  @Override
  public @Nullable MModeltype getModeltype(String name) {
    try {
      Query query = pm.newQuery(MModeltype.class);
      query.setFilter("name == modeltypeName");
      query.declareParameters("String modeltypeName");
      query.setUnique(true);

      return (MModeltype) query.execute(name);
    } catch (RuntimeException e) {
    }
    return null;
  }

  @Override
  public Collection<MModeltype> getModeltypes() throws CatalogException {
    try {
      Query query = pm.newQuery(MModeltype.class);
      return (List<MModeltype>) query.execute();
    } catch (RuntimeException e) {
      throw new CatalogException("failed to get modeltypes", e);
    }
  }

  @Override
  public MModeltype createModeltype(String name, String type, String location, String className,
                                    String uri) throws CatalogException {
    try {
      MModeltype mModeltype = new MModeltype(name, type, location, className, uri);
      pm.makePersistent(mModeltype);
      return mModeltype;
    } catch (RuntimeException e) {
      throw new CatalogException("failed to create modeltype '" + name + "'", e);
    }
  }

  @Override
  public void dropModeltype(String name) throws CatalogException {
    Transaction tx = pm.currentTransaction();
    try {
      tx.begin();

      pm.deletePersistent(getModeltype(name));

      tx.commit();
    } catch (RuntimeException e) {
      throw new CatalogException("failed to drop modeltype '" + name + "'", e);
    } finally {
      if (tx.isActive()) {
        tx.rollback();
      }
    }
  }

  @Override
  public MModel trainModel(
      String modeltypeName, String modelName, String schemaName, String tableName,
      List<String> columnNames, @Nullable Long baseTableRows, @Nullable Long trainedRows,
      @Nullable String options) throws CatalogException {
    try {
      MModel mModel = new MModel(
          getModeltype(modeltypeName), modelName, schemaName, tableName, columnNames,
          baseTableRows, trainedRows, options == null ? "" : options);
      pm.makePersistent(mModel);
      return mModel;
    } catch (RuntimeException e) {
      throw new CatalogException("failed to train model '" + modelName + "'", e);
    }
  }

  @Override
  public void dropModel(String name) throws CatalogException {
    Transaction tx = pm.currentTransaction();
    try {
      tx.begin();

      pm.deletePersistent(getModel(name));

      tx.commit();
    } catch (RuntimeException e) {
      throw new CatalogException("failed to drop model '" + name + "'", e);
    } finally {
      if (tx.isActive()) {
        tx.rollback();
      }
    }
  }

  @Override
  public Collection<MModel> getModels() throws CatalogException {
    try {
      Query query = pm.newQuery(MModel.class);
      return (List<MModel>) query.execute();
    } catch (RuntimeException e) {
      throw new CatalogException("failed to get models", e);
    }
  }

  @Override
  public Collection<MModel> getInferenceModels(String baseSchema, String baseTable)
      throws CatalogException {
    try {
      Query query = pm.newQuery(MModel.class);
      query.setFilter(
          "schemaName == baseSchema && tableName == baseTable && modeltype.type == \"INFERENCE\"");
      query.declareParameters("String baseSchema, String baseTable");
      return (List<MModel>) query.execute(baseSchema, baseTable);
    } catch (RuntimeException e) {
      throw new CatalogException("failed to get models", e);
    }
  }

  @Override
  public boolean modelExists(String name) {
    return getModel(name) != null;
  }

  @Override
  public @Nullable MModel getModel(String name) {
    try {
      Query query = pm.newQuery(MModel.class);
      query.setFilter("name == modelName");
      query.declareParameters("String modelName");
      query.setUnique(true);

      return (MModel) query.execute(name);
    } catch (RuntimeException e) {
    }
    return null;
  }

  @Override
  public MSynopsis createSynopsis(String synopsisName, String modelName, Integer rows,
                                  @Nullable Double ratio) throws CatalogException {
    try {
      MSynopsis mSynopsis = new MSynopsis(synopsisName, rows, ratio, getModel(modelName));
      pm.makePersistent(mSynopsis);
      return mSynopsis;
    } catch (RuntimeException e) {
      throw new CatalogException("failed to create synopsis '" + synopsisName + "'", e);
    }
  }

  @Override
  public Collection<MSynopsis> getAllSynopses() throws CatalogException {
    try {
      Query query = pm.newQuery(MSynopsis.class);
      return (List<MSynopsis>) query.execute();
    } catch (RuntimeException e) {
      throw new CatalogException("failed to get synopses", e);
    }
  }

  @Override
  public Collection<MSynopsis> getAllSynopses(String baseSchema, String baseTable)
      throws CatalogException {
    try {
      Query query = pm.newQuery(MSynopsis.class);
      query.setFilter("model.schemaName == baseSchema && model.tableName == baseTable");
      query.declareParameters("String baseSchema, String baseTable");
      Collection<MSynopsis> ret = (List<MSynopsis>) query.execute(baseSchema, baseTable);
      return ret;
    } catch (RuntimeException e) {
      throw new CatalogException("failed to get synopses", e);
    }
  }

  @Override
  public boolean synopsisExists(String name) {
    return getSynopsis(name) != null;
  }

  @Override
  public @Nullable MSynopsis getSynopsis(String name) {
    try {
      Query query = pm.newQuery(MSynopsis.class);
      query.setFilter("name == synopsisName");
      query.declareParameters("String synopsisName");
      query.setUnique(true);

      return (MSynopsis) query.execute(name);
    } catch (RuntimeException e) {
    }
    return null;
  }

  @Override
  public void dropSynopsis(String name) throws CatalogException {
    Transaction tx = pm.currentTransaction();
    try {
      tx.begin();

      pm.deletePersistent(getSynopsis(name));

      tx.commit();
    } catch (RuntimeException e) {
      throw new CatalogException("failed to drop synopsis '" + name + "'", e);
    } finally {
      if (tx.isActive()) {
        tx.rollback();
      }
    }
  }

  @Override
  public Collection<MQueryLog> getQueryLog() throws CatalogException {
    try {
      Query query = pm.newQuery(MQueryLog.class);
      // query.setFilter("user == user");
      // query.declareParameters("String user");
      // Collection<MQueryLog> ret = (List<MQueryLog>) query.execute(user);
      // return ret;
      return (List<MQueryLog>) query.execute();
    } catch (RuntimeException e) {
      throw new CatalogException("failed to get query log", e);
    }
  }

  @Override
  public MQueryLog insertQueryLog(String start, String user, String query) throws CatalogException {
    try {
      MQueryLog mQuery = new MQueryLog(start, user, query);
      pm.makePersistent(mQuery);
      return mQuery;
    } catch (RuntimeException e) {
      throw new CatalogException("failed to create query log '" + query + "'", e);
    }
  }

  @Override
  public void deleteQueryLogs(Integer cnt) throws CatalogException {
    Transaction tx = pm.currentTransaction();
    try {
      tx.begin();

      Query query = pm.newQuery(MQueryLog.class);

      if (cnt > 0) {
        query.range(0, cnt);
      }

      Collection<MTask> ret = (List<MTask>) query.execute();
      pm.deletePersistentAll(ret);

      //pm.deletePersistent(getQueryLog());
      tx.commit();
    } catch (RuntimeException e) {
      throw new CatalogException("failed to delete query log", e);
    } finally {
      if (tx.isActive()) {
        tx.rollback();
      }
    }
  }

  @Override
  public Collection<MTask> getTaskLog() throws CatalogException {
    try {
      Query query = pm.newQuery(MTask.class);
      // query.setFilter("user == user");
      // query.declareParameters("String user");
      // Collection<MQueryLog> ret = (List<MQueryLog>) query.execute(user);
      // return ret;
      return (List<MTask>) query.execute();
    } catch (RuntimeException e) {
      throw new CatalogException("failed to get task log", e);
    }
  }

  @Override
  public MTask insertTask(String time, Integer idx, String task, String status)
      throws CatalogException {
    try {
      MTask mtask = new MTask(time, idx, task, status);
      pm.makePersistent(mtask);
      return mtask;
    } catch (RuntimeException e) {
      throw new CatalogException("failed to create task log '" + task + "'", e);
    }
  }

  @Override
  public void deleteTasks(Integer cnt) throws CatalogException {
    Transaction tx = pm.currentTransaction();
    try {
      tx.begin();

      Query query = pm.newQuery(MTask.class);

      if (cnt > 0) {
        query.range(0, cnt);
      }

      Collection<MTask> ret = (List<MTask>) query.execute();

      pm.deletePersistentAll(ret);
      //pm.deletePersistent(getTaskLog());

      tx.commit();
    } catch (RuntimeException e) {
      throw new CatalogException("failed to delete task log", e);
    } finally {
      if (tx.isActive()) {
        tx.rollback();
      }
    }
  }

  @Override
  public void close() {
    pm.close();
  }
}
