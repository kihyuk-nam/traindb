package traindb.jdbc;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.Ref;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.time.FastDateFormat;
import org.verdictdb.VerdictSingleResult;
import org.verdictdb.jdbc41.VerdictResultSet;
import org.verdictdb.jdbc41.VerdictStreamResultSetMetaData;
import traindb.common.TrainDBLogger;


public class TrainDBStreamResultSet extends VerdictResultSet {

  private TrainDBLogger LOG = TrainDBLogger.getLogger(this.getClass());

  private static final String verdictStreamSequenceColumn = "seq";
  boolean hasReadAllQueryResults = false;
  BlockingDeque<VerdictSingleResult> queryResults = new LinkedBlockingDeque<>();
  VerdictSingleResult queryResult;
  private TrainDBStatement.ExecuteStream runnable;
  private boolean isClosed = false;
  private long rowIndex = 0;
  private HashMap<String, Integer> colNameIdx = new HashMap<>();
  private int lastQueryResultIndex = 0;

  private VerdictStreamResultSetMetaData metadata;

  public TrainDBStreamResultSet() {
    super();
  }

  public TrainDBStreamResultSet(VerdictSingleResult queryResult) {
    super(queryResult);
    queryResults.add(queryResult);
  }

  public void appendSingleResult(VerdictSingleResult queryResult) {
    queryResults.add(queryResult);
  }

  void setCompleted() {
    hasReadAllQueryResults = true;
  }

  void setRunnable(TrainDBStatement.ExecuteStream r) {
    this.runnable = r;
  }

  @Override
  public boolean absolute(int row) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void afterLast() throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void beforeFirst() throws SQLException {
    rowIndex = 0;
    lastQueryResultIndex = 0;
  }

  @Override
  public void cancelRowUpdates() throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  private void checkIndex(int index) throws SQLException {
    if (index == 1) {
      throw new SQLException(
          "The first column of a streaming query result is the sequence number of each result set; "
          + "thus, the values can be retrieved only in the types compatible with integer.");
    } else if (index < 1) {
      throw new SQLException("A column index must be a positive integer.");
    }
  }

  @Override
  public void clearWarnings() throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void close() {
    isClosed = true;
    runnable.abort();
  }

  @Override
  public void deleteRow() throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public int findColumn(String columnLabel) throws SQLException {
    if (columnLabel.equals(standardizedLabel(verdictStreamSequenceColumn))) {
      return 1;
    }
    return colNameIdx.get(standardizedLabel(columnLabel.toLowerCase())) + 2;
  }

  @Override
  public boolean first() throws SQLException {
    if (queryResults.size() == 0) {
      return false;
    } else {
      rowIndex = 1;
      lastQueryResultIndex = 0;
      return true;
    }
    //    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public Array getArray(int columnIndex) throws SQLException {
    checkIndex(columnIndex);

    return queryResult.getArray(columnIndex - 2);
  }

  @Override
  public Array getArray(String columnLabel) throws SQLException {
    if (colNameIdx.containsKey(standardizedLabel(columnLabel))) {
      return queryResult.getArray(standardizedLabel(columnLabel.toLowerCase()));
    } else {
      throw new SQLException("ColumnLabel does not exist.");
    }
  }

  @Override
  public InputStream getAsciiStream(int columnIndex) throws SQLException {
    checkIndex(columnIndex);
    return queryResult.getAsciiStream(columnIndex - 2);
  }

  @Override
  public InputStream getAsciiStream(String columnLabel) throws SQLException {
    if (colNameIdx.containsKey(standardizedLabel(columnLabel))) {
      return queryResult.getAsciiStream(standardizedLabel(columnLabel.toLowerCase()));
    } else {
      throw new SQLException("ColumnLabel does not exist.");
    }
  }

  @Override
  public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
    if (columnIndex == 1) {
      return BigDecimal.valueOf(lastQueryResultIndex);
    }
    checkIndex(columnIndex);

    return queryResult.getBigDecimal(columnIndex - 2);
  }

  @Override
  public BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException {
    if (columnIndex == 1) {
      return BigDecimal.valueOf(lastQueryResultIndex, scale);
    }
    checkIndex(columnIndex);

    return queryResult.getBigDecimal(columnIndex - 2, scale);
  }

  @Override
  public BigDecimal getBigDecimal(String columnLabel) throws SQLException {
    if (columnLabel.equals(verdictStreamSequenceColumn)) {
      return getBigDecimal(1);
    }
    if (colNameIdx.containsKey(standardizedLabel(columnLabel))) {
      return queryResult.getBigDecimal(standardizedLabel(columnLabel.toLowerCase()));
    } else {
      throw new SQLException("ColumnLabel does not exist.");
    }
  }

  @Override
  public BigDecimal getBigDecimal(String columnLabel, int scale) throws SQLException {
    if (columnLabel.equals(verdictStreamSequenceColumn)) {
      return getBigDecimal(1, scale);
    }
    if (colNameIdx.containsKey(standardizedLabel(columnLabel))) {
      return queryResult.getBigDecimal(standardizedLabel(columnLabel.toLowerCase()), scale);
    } else {
      throw new SQLException("ColumnLabel does not exist.");
    }
  }

  @Override
  public InputStream getBinaryStream(int columnIndex) throws SQLException {
    checkIndex(columnIndex);
    return queryResult.getBinaryStream(columnIndex - 2);
  }

  @Override
  public InputStream getBinaryStream(String columnLabel) throws SQLException {
    if (colNameIdx.containsKey(standardizedLabel(columnLabel))) {
      return queryResult.getBinaryStream(standardizedLabel(columnLabel.toLowerCase()));
    } else {
      throw new SQLException("ColumnLabel does not exist.");
    }
  }

  @Override
  public Blob getBlob(int columnIndex) throws SQLException {
    checkIndex(columnIndex);

    return queryResult.getBlob(columnIndex - 2);
  }

  @Override
  public Blob getBlob(String columnLabel) throws SQLException {
    if (colNameIdx.containsKey(standardizedLabel(columnLabel))) {
      return queryResult.getBlob(standardizedLabel(columnLabel.toLowerCase()));
    } else {
      throw new SQLException("ColumnLabel does not exist.");
    }
  }

  @Override
  public boolean getBoolean(int columnIndex) throws SQLException {
    checkIndex(columnIndex);
    return queryResult.getBoolean(columnIndex - 2);
  }

  @Override
  public boolean getBoolean(String columnLabel) throws SQLException {
    if (colNameIdx.containsKey(standardizedLabel(columnLabel))) {
      return queryResult.getBoolean(standardizedLabel(columnLabel.toLowerCase()));
    } else {
      throw new SQLException("ColumnLabel does not exist.");
    }
  }

  @Override
  public byte getByte(int columnIndex) throws SQLException {
    checkIndex(columnIndex);
    return queryResult.getByte(columnIndex - 2);
  }

  @Override
  public byte getByte(String columnLabel) throws SQLException {
    if (colNameIdx.containsKey(standardizedLabel(columnLabel))) {
      return queryResult.getByte(standardizedLabel(columnLabel.toLowerCase()));
    } else {
      throw new SQLException("ColumnLabel does not exist.");
    }
  }

  @Override
  public byte[] getBytes(int columnIndex) throws SQLException {
    checkIndex(columnIndex);
    return queryResult.getBytes(columnIndex - 2);
  }

  @Override
  public byte[] getBytes(String columnLabel) throws SQLException {
    if (colNameIdx.containsKey(standardizedLabel(columnLabel))) {
      return queryResult.getBytes(standardizedLabel(columnLabel.toLowerCase()));
    } else {
      throw new SQLException("ColumnLabel does not exist.");
    }
  }

  @Override
  public Reader getCharacterStream(int columnIndex) throws SQLException {
    checkIndex(columnIndex);

    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public Reader getCharacterStream(String columnLabel) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public Clob getClob(int columnIndex) throws SQLException {
    checkIndex(columnIndex);

    return queryResult.getClob(columnIndex - 2);
  }

  @Override
  public Clob getClob(String columnLabel) throws SQLException {
    if (colNameIdx.containsKey(standardizedLabel(columnLabel))) {
      return queryResult.getClob(standardizedLabel(columnLabel.toLowerCase()));
    } else {
      throw new SQLException("ColumnLabel does not exist.");
    }
  }

  @Override
  public int getConcurrency() throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public String getCursorName() throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public Date getDate(int columnIndex) throws SQLException {
    checkIndex(columnIndex);
    return queryResult.getDate(columnIndex - 2);
  }

  @Override
  public Date getDate(int columnIndex, Calendar cal) throws SQLException {
    try {
      FastDateFormat dateFormat =
          FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss.SSS", cal.getTimeZone());
      return new Date(dateFormat.parse(queryResult.getDate(columnIndex - 2).toString()).getTime());
    } catch (Exception e) {
      SQLException error = new SQLException("Error parsing date");
      throw error;
    }
  }

  @Override
  public Date getDate(String columnLabel) throws SQLException {
    if (colNameIdx.containsKey(standardizedLabel(columnLabel))) {
      return queryResult.getDate(standardizedLabel(columnLabel.toLowerCase()));
    } else {
      throw new SQLException("ColumnLabel does not exist.");
    }
  }

  @Override
  public Date getDate(String columnLabel, Calendar cal) throws SQLException {
    if (colNameIdx.containsKey(standardizedLabel(columnLabel))) {
      int index = columnLabel.indexOf(standardizedLabel(columnLabel)) + 1;
      return getDate(index, cal);
    } else {
      throw new SQLException("ColumnLabel does not exist.");
    }
  }

  @Override
  public double getDouble(int columnIndex) throws SQLException {
    if (columnIndex == 1) {
      return lastQueryResultIndex;
    }
    checkIndex(columnIndex);
    return queryResult.getDouble(columnIndex - 2);
  }

  @Override
  public double getDouble(String columnLabel) throws SQLException {
    if (columnLabel.equals(verdictStreamSequenceColumn)) {
      return getDouble(1);
    }
    if (colNameIdx.containsKey(standardizedLabel(columnLabel))) {
      return queryResult.getDouble(standardizedLabel(columnLabel.toLowerCase()));
    } else {
      throw new SQLException("ColumnLabel does not exist.");
    }
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
  public float getFloat(int columnIndex) throws SQLException {
    if (columnIndex == 1) {
      return lastQueryResultIndex;
    }
    checkIndex(columnIndex);
    return queryResult.getFloat(columnIndex - 2);
  }

  @Override
  public float getFloat(String columnLabel) throws SQLException {
    if (columnLabel.equals(verdictStreamSequenceColumn)) {
      return getFloat(1);
    }
    if (colNameIdx.containsKey(standardizedLabel(columnLabel))) {
      return queryResult.getFloat(standardizedLabel(columnLabel.toLowerCase()));
    } else {
      throw new SQLException("ColumnLabel does not exist.");
    }
  }

  @Override
  public int getHoldability() throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public int getInt(int columnIndex) throws SQLException {
    if (columnIndex == 1) {
      return lastQueryResultIndex;
    }
    checkIndex(columnIndex);
    return queryResult.getInt(columnIndex - 2);
  }

  @Override
  public int getInt(String columnLabel) throws SQLException {
    if (columnLabel.equals(verdictStreamSequenceColumn)) {
      return getInt(1);
    }
    if (colNameIdx.containsKey(standardizedLabel(columnLabel))) {
      return queryResult.getInt(standardizedLabel(columnLabel.toLowerCase()));
    } else {
      throw new SQLException("ColumnLabel does not exist.");
    }
  }

  @Override
  public long getLong(int columnIndex) throws SQLException {
    if (columnIndex == 1) {
      return lastQueryResultIndex;
    }
    checkIndex(columnIndex);
    return queryResult.getLong(columnIndex - 2);
  }

  @Override
  public long getLong(String columnLabel) throws SQLException {
    if (columnLabel.equals(verdictStreamSequenceColumn)) {
      return getLong(1);
    }
    if (colNameIdx.containsKey(standardizedLabel(columnLabel))) {
      return queryResult.getLong(standardizedLabel(columnLabel.toLowerCase()));
    } else {
      throw new SQLException("ColumnLabel does not exist.");
    }
  }

  @Override
  public ResultSetMetaData getMetaData() throws SQLException {
    if (metadata == null && queryResult == null) {
      try {
        queryResult = queryResults.take();
        metadata = new VerdictStreamResultSetMetaData(queryResult);
        lastQueryResultIndex++;
      } catch (InterruptedException e) {
        /* do nothing */
      }
    } else if (metadata == null && queryResult != null) {
      metadata = new VerdictStreamResultSetMetaData(queryResult);
    }
    return metadata;
  }

  @Override
  public Reader getNCharacterStream(int columnIndex) throws SQLException {
    return getCharacterStream(columnIndex);
  }

  @Override
  public Reader getNCharacterStream(String columnLabel) throws SQLException {
    return getCharacterStream(standardizedLabel(columnLabel));
  }

  @Override
  public NClob getNClob(int columnIndex) throws SQLException {
    checkIndex(columnIndex);
    return queryResult.getNClob(columnIndex - 2);
  }

  @Override
  public NClob getNClob(String columnLabel) throws SQLException {
    if (colNameIdx.containsKey(standardizedLabel(columnLabel))) {
      return queryResult.getNClob(standardizedLabel(columnLabel.toLowerCase()));
    } else {
      throw new SQLException("ColumnLabel does not exist.");
    }
  }

  @Override
  public String getNString(int columnIndex) throws SQLException {
    return getString(columnIndex);
  }

  @Override
  public String getNString(String columnLabel) throws SQLException {
    return getString(standardizedLabel(columnLabel));
  }

  @Override
  public Object getObject(int columnIndex) throws SQLException {
    checkIndex(columnIndex);
    return queryResult.getValue(columnIndex - 2);
  }

  @Override
  public <T> T getObject(int columnIndex, Class<T> type) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public Object getObject(int columnIndex, Map<String, Class<?>> map) throws SQLException {
    return getObject(columnIndex);
  }

  @Override
  public Object getObject(String columnLabel) throws SQLException {
    if (colNameIdx.containsKey(standardizedLabel(columnLabel))) {
      return getObject(colNameIdx.get(standardizedLabel(columnLabel.toLowerCase())) + 1);
    } else {
      throw new SQLException("ColumnLabel does not exist.");
    }
  }

  @Override
  public <T> T getObject(String columnLabel, Class<T> type) throws SQLException {
    return (T) getObject(standardizedLabel(columnLabel));
  }

  @Override
  public Object getObject(String columnLabel, Map<String, Class<?>> map) throws SQLException {
    return getObject(standardizedLabel(columnLabel));
  }

  @Override
  public Ref getRef(int columnIndex) throws SQLException {
    return queryResult.getRef(columnIndex - 2);
  }

  @Override
  public Ref getRef(String columnLabel) throws SQLException {
    if (colNameIdx.containsKey(standardizedLabel(columnLabel))) {
      return queryResult.getRef(standardizedLabel(columnLabel.toLowerCase()));
    } else {
      throw new SQLException("ColumnLabel does not exist.");
    }
  }

  @Override
  public int getRow() throws SQLException {
    return ((Long) rowIndex).intValue();
  }

  @Override
  public RowId getRowId(int columnIndex) throws SQLException {
    return queryResult.getRowId(columnIndex - 2);
  }

  @Override
  public RowId getRowId(String columnLabel) throws SQLException {
    if (colNameIdx.containsKey(standardizedLabel(columnLabel))) {
      return queryResult.getRowId(standardizedLabel(columnLabel.toLowerCase()));
    } else {
      throw new SQLException("ColumnLabel does not exist.");
    }
  }

  @Override
  public short getShort(int columnIndex) throws SQLException {
    if (columnIndex == 1) {
      return Integer.valueOf(lastQueryResultIndex).shortValue();
    }
    checkIndex(columnIndex);
    return queryResult.getShort(columnIndex - 2);
  }

  @Override
  public short getShort(String columnLabel) throws SQLException {
    if (columnLabel.equals(verdictStreamSequenceColumn)) {
      return getShort(1);
    }
    if (colNameIdx.containsKey(standardizedLabel(columnLabel))) {
      return queryResult.getShort(standardizedLabel(columnLabel.toLowerCase()));
    } else {
      throw new SQLException("ColumnLabel does not exist.");
    }
  }

  @Override
  public SQLXML getSQLXML(int columnIndex) throws SQLException {
    return queryResult.getSQLXML(columnIndex - 2);
  }

  @Override
  public SQLXML getSQLXML(String columnLabel) throws SQLException {
    if (colNameIdx.containsKey(standardizedLabel(columnLabel))) {
      return queryResult.getSQLXML(standardizedLabel(columnLabel.toLowerCase()));
    } else {
      throw new SQLException("ColumnLabel does not exist.");
    }
  }

  @Override
  public Statement getStatement() throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public String getString(int columnIndex) throws SQLException {
    if (columnIndex == 1) {
      return Integer.valueOf(lastQueryResultIndex).toString();
    }
    return queryResult.getString(columnIndex - 2);
  }

  @Override
  public String getString(String columnLabel) throws SQLException {
    if (columnLabel.equals(verdictStreamSequenceColumn)) {
      return getString(1);
    }
    if (colNameIdx.containsKey(standardizedLabel(columnLabel))) {
      return queryResult.getString(standardizedLabel(columnLabel.toLowerCase()));
    } else {
      throw new SQLException("ColumnLabel does not exist.");
    }
  }

  @Override
  public Time getTime(int columnIndex) throws SQLException {
    checkIndex(columnIndex);
    return queryResult.getTime(columnIndex - 2);
  }

  @Override
  public Time getTime(int columnIndex, Calendar cal) throws SQLException {
    try {
      FastDateFormat dateFormat =
          FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss.SSS", cal.getTimeZone());
      return new Time(dateFormat.parse(queryResult.getTime(columnIndex - 2).toString()).getTime());
    } catch (Exception e) {
      SQLException error = new SQLException("Error parsing time");
      throw error;
    }
  }

  @Override
  public Time getTime(String columnLabel) throws SQLException {
    if (colNameIdx.containsKey(standardizedLabel(columnLabel))) {
      return queryResult.getTime(standardizedLabel(columnLabel.toLowerCase()));
    } else {
      throw new SQLException("ColumnLabel does not exist.");
    }
  }

  @Override
  public Time getTime(String columnLabel, Calendar cal) throws SQLException {
    if (colNameIdx.containsKey(standardizedLabel(columnLabel))) {
      int index = columnLabel.indexOf(standardizedLabel(columnLabel)) + 1;
      return getTime(index, cal);
    } else {
      throw new SQLException("ColumnLabel does not exist.");
    }
  }

  @Override
  public Timestamp getTimestamp(int columnIndex) throws SQLException {
    checkIndex(columnIndex);
    return queryResult.getTimestamp(columnIndex - 2);
  }

  @Override
  public Timestamp getTimestamp(int columnIndex, Calendar cal) throws SQLException {
    try {
      FastDateFormat dateFormat =
          FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss.SSS", cal.getTimeZone());
      return new Timestamp(
          dateFormat.parse(queryResult.getTimestamp(columnIndex - 2).toString()).getTime());
    } catch (Exception e) {
      SQLException error = new SQLException("Error parsing time stamp");
      throw error;
    }
  }

  @Override
  public Timestamp getTimestamp(String columnLabel) throws SQLException {
    if (colNameIdx.containsKey(standardizedLabel(columnLabel))) {
      return queryResult.getTimestamp(standardizedLabel(columnLabel.toLowerCase()));
    } else {
      throw new SQLException("ColumnLabel does not exist.");
    }
  }

  @Override
  public Timestamp getTimestamp(String columnLabel, Calendar cal) throws SQLException {
    if (colNameIdx.containsKey(standardizedLabel(columnLabel))) {
      int index = columnLabel.indexOf(standardizedLabel(columnLabel)) + 1;
      return getTimestamp(index, cal);
    } else {
      throw new SQLException("ColumnLabel does not exist.");
    }
  }

  @Override
  public int getType() throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public InputStream getUnicodeStream(int columnIndex) throws SQLException {
    checkIndex(columnIndex);
    return queryResult.getUnicodeStream(columnIndex - 2);
  }

  @Override
  public InputStream getUnicodeStream(String columnLabel) throws SQLException {
    if (colNameIdx.containsKey(standardizedLabel(columnLabel))) {
      return queryResult.getUnicodeStream(standardizedLabel(columnLabel.toLowerCase()));
    } else {
      throw new SQLException("ColumnLabel does not exist.");
    }
  }

  @Override
  public URL getURL(int columnIndex) throws SQLException {
    return queryResult.getURL(columnIndex - 2);
  }

  @Override
  public URL getURL(String columnLabel) throws SQLException {
    if (colNameIdx.containsKey(standardizedLabel(columnLabel))) {
      return queryResult.getURL(standardizedLabel(columnLabel.toLowerCase()));
    } else {
      throw new SQLException("ColumnLabel does not exist.");
    }
  }

  @Override
  public SQLWarning getWarnings() throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void insertRow() throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public boolean isAfterLast() throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public boolean isBeforeFirst() throws SQLException {
    return rowIndex == 0;
  }

  @Override
  public boolean isClosed() throws SQLException {
    return isClosed;
  }

  @Override
  public boolean isFirst() throws SQLException {
    return rowIndex == 1;
  }

  @Override
  public boolean isLast() throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  private boolean isValidType(String expected, int columnindex) {
    return true;
  }

  @Override
  public boolean isWrapperFor(Class<?> iface) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public boolean last() throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void moveToCurrentRow() throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void moveToInsertRow() throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public boolean next() throws SQLException {
    if (isClosed) {
      return false;
    }
    try {
      if (queryResult == null && (!hasReadAllQueryResults || !queryResults.isEmpty())) {
        LOG.debug("Trying to take a queryResult out of blockingDeque");
        queryResult = queryResults.take();
        lastQueryResultIndex++;
      }
      boolean hasMore = queryResult.next();
      if (hasMore) {
        rowIndex++;
        return true;
      } else if (queryResults.peek() == null && hasReadAllQueryResults) {
        return false;
      } else {
        LOG.debug("Trying to take a queryResult out of blockingDeque");
        queryResult = queryResults.poll(2, TimeUnit.MINUTES);
        if (queryResult == null && hasReadAllQueryResults) {
          return false;
        } else if (queryResult == null) {
          queryResult = queryResults.take();
        }
        lastQueryResultIndex++;
        return next();
      }
    } catch (InterruptedException e) {
      return false;
    }
  }

  @Override
  public boolean previous() throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void refreshRow() throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public boolean relative(int rows) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public boolean rowDeleted() throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public boolean rowInserted() throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public boolean rowUpdated() throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  String standardizedLabel(String label) {
    return label.toLowerCase();
  }

  @Override
  public <T> T unwrap(Class<T> iface) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateArray(int columnIndex, Array x) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateArray(String columnLabel, Array x) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateAsciiStream(int columnIndex, InputStream x) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateAsciiStream(int columnIndex, InputStream x, int length) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateAsciiStream(int columnIndex, InputStream x, long length) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateAsciiStream(String columnLabel, InputStream x) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateAsciiStream(String columnLabel, InputStream x, int length) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateAsciiStream(String columnLabel, InputStream x, long length)
      throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateBigDecimal(int columnIndex, BigDecimal x) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateBigDecimal(String columnLabel, BigDecimal x) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateBinaryStream(int columnIndex, InputStream x) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateBinaryStream(int columnIndex, InputStream x, int length) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateBinaryStream(int columnIndex, InputStream x, long length) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateBinaryStream(String columnLabel, InputStream x) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateBinaryStream(String columnLabel, InputStream x, int length)
      throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateBinaryStream(String columnLabel, InputStream x, long length)
      throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateBlob(int columnIndex, Blob x) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateBlob(int columnIndex, InputStream inputStream) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateBlob(int columnIndex, InputStream inputStream, long length)
      throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateBlob(String columnLabel, Blob x) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateBlob(String columnLabel, InputStream inputStream) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateBlob(String columnLabel, InputStream inputStream, long length)
      throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateBoolean(int columnIndex, boolean x) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateBoolean(String columnLabel, boolean x) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateByte(int columnIndex, byte x) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateByte(String columnLabel, byte x) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateBytes(int columnIndex, byte[] x) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateBytes(String columnLabel, byte[] x) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateCharacterStream(int columnIndex, Reader x) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateCharacterStream(int columnIndex, Reader x, int length) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateCharacterStream(int columnIndex, Reader x, long length) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateCharacterStream(String columnLabel, Reader reader) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateCharacterStream(String columnLabel, Reader reader, int length)
      throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateCharacterStream(String columnLabel, Reader reader, long length)
      throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateClob(int columnIndex, Clob x) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateClob(int columnIndex, Reader reader) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateClob(int columnIndex, Reader reader, long length) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateClob(String columnLabel, Clob x) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateClob(String columnLabel, Reader reader) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateClob(String columnLabel, Reader reader, long length) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateDate(int columnIndex, Date x) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateDate(String columnLabel, Date x) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateDouble(int columnIndex, double x) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateDouble(String columnLabel, double x) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateFloat(int columnIndex, float x) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateFloat(String columnLabel, float x) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateInt(int columnIndex, int x) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateInt(String columnLabel, int x) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateLong(int columnIndex, long x) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateLong(String columnLabel, long x) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateNCharacterStream(int columnIndex, Reader x) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateNCharacterStream(int columnIndex, Reader x, long length) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateNCharacterStream(String columnLabel, Reader reader) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateNCharacterStream(String columnLabel, Reader reader, long length)
      throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateNClob(int columnIndex, NClob nClob) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateNClob(int columnIndex, Reader reader) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateNClob(int columnIndex, Reader reader, long length) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateNClob(String columnLabel, NClob nClob) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateNClob(String columnLabel, Reader reader) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateNClob(String columnLabel, Reader reader, long length) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateNString(int columnIndex, String nString) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateNString(String columnLabel, String nString) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateNull(int columnIndex) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateNull(String columnLabel) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateObject(int columnIndex, Object x) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateObject(int columnIndex, Object x, int scaleOrLength) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateObject(String columnLabel, Object x) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateObject(String columnLabel, Object x, int scaleOrLength) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateRef(int columnIndex, Ref x) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateRef(String columnLabel, Ref x) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateRow() throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateRowId(int columnIndex, RowId x) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateRowId(String columnLabel, RowId x) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateShort(int columnIndex, short x) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateShort(String columnLabel, short x) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateSQLXML(int columnIndex, SQLXML xmlObject) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateSQLXML(String columnLabel, SQLXML xmlObject) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateString(int columnIndex, String x) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateString(String columnLabel, String x) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateTime(int columnIndex, Time x) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateTime(String columnLabel, Time x) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateTimestamp(int columnIndex, Timestamp x) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void updateTimestamp(String columnLabel, Timestamp x) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public boolean wasNull() throws SQLException {
    return queryResults.isEmpty();
  }

}
