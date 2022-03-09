package io.github.shanhm1991.echo.lang.pool;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 
 * @author shanhm1991@163.com
 *
 */
@Slf4j
public class JdbcHelper {

	private static final int MAXLEN = 30;
	
	private static final int BUFFER = 1024;
	
	private static final int COL_TYPE_12 = 12;
	
	private static final int COL_TYPE_2005 = 2005;
	
	private static final int INDEX_DEX = 46;

	protected JdbcHelper(){

	}

	protected final void _startTransaction(String poolName) throws Exception {
		JdbcPool pool = getPool(poolName);
		JdbcPool.JdbcNode node = (JdbcPool.JdbcNode)pool.acquire();
		if(node.isInTransaction){
			log.warn("transaction started[Connection was already in Transaction]");
			return;
		}
		node.isInTransaction = true;
		Connection con = node.v;
		try{
			con.commit();
			con.setAutoCommit(false); 
			if (log.isDebugEnabled()) {
				log.debug("transaction started");
			}
		}catch(Exception e){
			node.isInTransaction = false;
			con.setAutoCommit(true); 
			pool.release();
			log.error("transaction start failed", e);
		}
	}

	protected final void _endTransaction(String poolName) throws Exception {
		JdbcPool pool = getPool(poolName);
		JdbcPool.JdbcNode node = (JdbcPool.JdbcNode)pool.acquire();
		if(!node.isInTransaction){
			pool.release();
			log.warn("transaction commit failed[Connection is not in Transaction]");
			return;
		}

		Connection con = node.v;
		try{
			con.commit();
			if (log.isDebugEnabled()) {
				log.debug("transaction commited");
			}
		}catch(Exception e){
			log.error("transaction commit failed, rollback now", e); 
			con.rollback();
		}finally{
			node.isInTransaction = false;
			con.setAutoCommit(true); 
			pool.release();
		}
	}

	private boolean isInTransaction(JdbcPool pool) throws Exception{ 
		JdbcPool.JdbcNode node = (JdbcPool.JdbcNode)pool.acquire();
		return node.isInTransaction;
	}

	/**
	 * 取消事务，直接回滚,释放连接
	 * @param poolName
	 * @throws Exception
	 */
	private void cancelTransaction(String poolName) throws Exception {
		if(!isInTransaction(getPool(poolName))){
			return;
		}

		JdbcPool pool = getPool(poolName);
		try{
			JdbcPool.JdbcNode node = (JdbcPool.JdbcNode)pool.acquire();
			if(!isInTransaction(getPool(poolName))){
				log.warn("transaction canceled[Connection was not in Transaction]");
				return;
			}
			Connection con = node.v;
			if (log.isDebugEnabled()) {
				log.debug("transaction canceled and rollback");
			}
			con.rollback();
			con.setAutoCommit(true); 
		}finally{
			pool.release();
		}
	}

	protected final List<Map<String, Object>> _queryForList(String poolName, String sql, Map<String, Object> paramMap) throws Exception {
		StringBuilder sqlBuilder = new StringBuilder(sql);
		Map<String, Integer> indexMap = praseSQL(sqlBuilder);
		sql = sqlBuilder.toString();
		if(!isInTransaction(getPool(poolName))){
			return queryForList(poolName, sql, paramMap, indexMap);
		}else{
			return queryForListTransaction(poolName, sql, paramMap, indexMap);
		}
	}

	private List<Map<String, Object>> queryForList(String poolName, String sql, Map<String, Object> paramMap, Map<String, Integer> indexMap) throws Exception {
		JdbcPool pool = getPool(poolName);
		PreparedStatement ps = null;
		ResultSet resultSet = null;
		try{
			Connection con = pool.acquire().v;
			ps = con.prepareStatement(sql);
			setParams(con, ps, paramMap, indexMap);
			if (log.isDebugEnabled()) {
				log.debug("[query]=" + sql);
				log.debug("[param]=" + logParam(ps, paramMap, indexMap)); 
			}
			long eTime = System.currentTimeMillis();
			resultSet = ps.executeQuery();
			List<Map<String, Object>> list = praseResultSet(resultSet);
			if (log.isDebugEnabled()) {
				log.debug("[affects=" + list.size() + ",cost=" + (System.currentTimeMillis() - eTime) + "ms]");
			}
			return list;
		}finally{
			pool.release();
			close(resultSet); 
			close(ps); 
		}
	}

	private List<Map<String, Object>> queryForListTransaction(String poolName, String sql, Map<String, Object> paramMap, Map<String, Integer> indexMap) throws Exception {
		JdbcPool pool = getPool(poolName);
		PreparedStatement ps = null;
		ResultSet resultSet = null;
		try{
			Connection con = pool.acquire().v;
			ps = con.prepareStatement(sql);
			setParams(con, ps, paramMap, indexMap);
			if (log.isDebugEnabled()) {
				log.debug("[query]=" + sql);
				log.debug("[param]=" + logParam(ps, paramMap, indexMap)); 
			}
			long eTime = System.currentTimeMillis();
			List<Map<String, Object>> list = praseResultSet(ps.executeQuery());
			if (log.isDebugEnabled()) {
				log.debug("[affects=" + list.size() + ",cost=" + (System.currentTimeMillis() - eTime) + "ms]");
			}
			return list;
		}catch(Exception e){
			cancelTransaction(poolName);
			throw new Exception("transaction faile, data rollbacked", e);
		}finally{
			close(resultSet); 
			close(ps); 
		}
	}

	protected final int _execute(String poolName, String sql, Map<String, Object> paramMap) throws Exception {
		StringBuilder sqlBuilder = new StringBuilder(sql);
		Map<String, Integer> indexMap = praseSQL(sqlBuilder);
		sql = sqlBuilder.toString();
		if(isInTransaction(getPool(poolName))){
			return executeTransaction(poolName, sql, paramMap, indexMap);
		}else{
			return execute(poolName, sql, paramMap, indexMap);
		}
	}

	private int execute(String poolName, String sql, Map<String, Object> paramMap, Map<String, Integer> indexMap) throws Exception { 
		JdbcPool pool = getPool(poolName);
		PreparedStatement ps = null;
		try{
			Connection con = pool.acquire().v;
			ps = con.prepareStatement(sql);
			setParams(con, ps, paramMap, indexMap);
			if (log.isDebugEnabled()) {
				log.debug("[execute]=" + sql);
				log.debug("[param]=" + logParam(ps, paramMap, indexMap)); 
			}
			long eTime = System.currentTimeMillis();
			int num = ps.executeUpdate();
			if (log.isDebugEnabled()) {
				log.debug("[affects=" + num + ",cost=" + (System.currentTimeMillis() - eTime) + "ms]");
			}
			return num;
		}finally{
			close(ps); 
			pool.release();
		}
	}

	private int executeTransaction(String poolName, String sql, Map<String, Object> paramMap, Map<String, Integer> indexMap) throws Exception { 
		PreparedStatement ps = null;
		try{
			Connection con = getPool(poolName).acquire().v;
			ps = con.prepareStatement(sql);
			setParams(con, ps, paramMap, indexMap);
			if (log.isDebugEnabled()) {
				log.debug("[execute in transaction]=" + sql);
				log.debug("[param] = " + logParam(ps, paramMap, indexMap)); 
			}
			long eTime = System.currentTimeMillis();
			int num = ps.executeUpdate();
			if (log.isDebugEnabled()) {
				log.debug("[affects=" + num + ",cost=" + (System.currentTimeMillis() - eTime) + "ms]");
			}
			return num;
		}catch(Exception e) {
			cancelTransaction(poolName);
			throw new Exception("transaction faile, data rollbacked", e);
		}finally{
			close(ps); 
		}
	}

	protected final int[] _batchExecute(String poolName, String sql, List<Map<String, Object>> paramMaps) throws Exception {
		StringBuilder sqlBuilder = new StringBuilder(sql);
		Map<String, Integer> indexMap = praseSQL(sqlBuilder);
		sql = sqlBuilder.toString();
		if(isInTransaction(getPool(poolName))){
			return batchExecuteTransaction(poolName, sql, paramMaps, indexMap);
		}else{
			return batchExecute(poolName, sql, paramMaps, indexMap);
		}

	}

	private int[] batchExecute(String poolName, String sql, List<Map<String, Object>> paramMaps, Map<String, Integer> indexMap) throws Exception {
		JdbcPool pool = getPool(poolName);
		PreparedStatement ps = null;
		try{
			Connection con = pool.acquire().v;
			ps = con.prepareStatement(sql);
			for(Map<String, Object> map : paramMaps){
				setParams(con, ps, map, indexMap);
				ps.addBatch();
			}
			if (log.isDebugEnabled()) {
				log.debug("[batch execute]=" + sql);
			}
			long eTime = System.currentTimeMillis();
			int[] nums = ps.executeBatch();
			int num = 0;
			for(int i : nums){
				num += i;
			}
			if (log.isDebugEnabled()) {
				log.debug("[affects=" + num + ",cost=" + (System.currentTimeMillis() - eTime) + "ms]");
			}
			return nums;
		}finally{
			close(ps); 
			pool.release();
		}
	}

	private int[] batchExecuteTransaction(String poolName, String sql, List<Map<String, Object>> paramMaps, Map<String, Integer> indexMap) throws Exception {
		PreparedStatement ps = null;
		try{
			Connection con = getPool(poolName).acquire().v;
			ps = con.prepareStatement(sql);
			for(Map<String, Object> map : paramMaps){
				setParams(con, ps, map, indexMap);
				ps.addBatch();
			}
			if (log.isDebugEnabled()) {
				log.debug("[batch execute in transaction]=" + sql);
			}
			long eTime = System.currentTimeMillis();
			int[] nums = ps.executeBatch();
			int num = 0;
			for(int i : nums){
				num += i;
			}
			if (log.isDebugEnabled()) {
				log.debug("[affects=" + num + ",cost=" + (System.currentTimeMillis() - eTime) + "ms]");
			}
			return nums;
		}catch(Exception e){
			cancelTransaction(poolName);
			throw new Exception("transaction faile, data rollbacked", e);
		}finally{
			close(ps); 
		}
	}

	private JdbcPool getPool(String poolName) { 
		JdbcPool pool = (JdbcPool)PoolManager.get(poolName);
		if(pool == null){
			throw new RuntimeException("pool[" + poolName + "] not exist.");
		}
		return pool;
	}

	private Map<String,Integer> praseSQL(StringBuilder sqlBuilder){
		Map<String,Integer> indexMap = new HashMap<>();
		String sql = sqlBuilder.toString();
		if(!sql.contains("#")){
			return indexMap;
		}

		int index = 0;
		while(sql.contains("#")){
			int start = sql.indexOf("#");
			int end = sql.indexOf("#", start + 1);
			String key = sql.substring(start + 1, end);
			index++;
			indexMap.put(key, index);
			sql = sql.substring(0, start) + "?" + sql.substring(end + 1);
		}

		sqlBuilder.delete(0, sqlBuilder.length()).append(sql);
		return indexMap;
	}

	private void setParams(Connection con, PreparedStatement ps, Map<String, Object> paramMap, Map<String, Integer> indexMap) throws Exception{ 
		for(Entry<String, Integer> entry : indexMap.entrySet()){
			String key = entry.getKey();
			Integer index = entry.getValue();
			Object value = paramMap.get(key);
			if(value == null){
				throw new IllegalArgumentException("param[" + key + "] not valued.");
			}
			ps.setObject(index, value);
		}
	}

	private String logParam(PreparedStatement ps, Map<String, Object> paramMap, Map<String, Integer> indexMap) {
		StringBuilder builder = new StringBuilder("[");
		if(indexMap == null || indexMap.isEmpty()){
			return builder.append("]").toString();
		}else{
			int count = 0;
			for(Entry<String, Integer> entry : indexMap.entrySet()){
				String key = entry.getKey();
				builder.append(key);
				builder.append("=");
				String value = String.valueOf(paramMap.get(key));
				if(value.length() > MAXLEN){
					value = value.substring(0, MAXLEN) + "...";
				}
				builder.append(value);
				count++;
				if(count < indexMap.size()){
					builder.append(",");
				}
			}
		}
		//		暂时不用AbstractMethodError
		//		try {
		//			ParameterMetaData pdata = ps.getParameterMetaData();
		//			int count = pdata.getParameterCount();
		//			for(int i = 1; i <= count; i++){
		//				//String type = pdata.getParameterTypeName(i); 不支持的特性
		//				String value = getParamValue(i, paramMap, indexMap);
		//				if(i < count){
		//					builder.append(value).append(", ");
		//					continue;
		//				}
		//				builder.append(value);
		//			}
		//		} catch (SQLException e) {
		//			log.error(e); 
		//		}
		return builder.append("]").toString();
	}

	private List<Map<String, Object>> praseResultSet(ResultSet resultSet) throws SQLException{
		List<Map<String, Object>> list = new ArrayList<>();
		while (resultSet.next()) {
			list.add(praseResult(resultSet));
		}
		return list;
	}

	private Map<String, Object> praseResult(ResultSet resultSet) throws SQLException {
		Map<String, Object> result = new HashMap<>();
		ResultSetMetaData rsmd = resultSet.getMetaData();

		int cols = rsmd.getColumnCount();
		for (int i = 1; i <= cols; ++i) {
			String colName = rsmd.getColumnLabel(i);
			if ((colName == null) || (colName.length() == 0)) {
				colName = rsmd.getColumnName(i);
			}

			Object colValue = resultSet.getObject(i);
			int colType = rsmd.getColumnType(i);
			if ((colValue == null) && ((1 == colType) || (COL_TYPE_12 == colType) || (-1 == colType))) {
				colValue = "";
			} else if ((COL_TYPE_2005 == colType) || (colValue instanceof Clob)) {
				colValue = processClob(resultSet.getClob(i));
			} else {
				colValue = processBigDecimal(colValue, resultSet, i);
			}
			result.put(colName, colValue);
		}
		return result;
	}

	private Object processBigDecimal(Object obj, ResultSet rs, int columIndex) {
		if (obj instanceof BigInteger) {
			obj = ((BigInteger) obj).longValue();
		} else if (obj instanceof BigDecimal) {
			BigDecimal bd = (BigDecimal) obj;
			if ((bd.scale() == 0) || (bd.toString().indexOf(INDEX_DEX) == -1)) {
				obj = bd.longValue();
			} else
				obj = bd.floatValue();

		}
		return obj;
	}

	private String processClob(Clob clob) throws SQLException {
		if (clob == null) {
			return "";
		}

		BufferedReader reader = new BufferedReader(clob.getCharacterStream());
		StringBuilder sb = new StringBuilder();
		try {
			char[] buffer = new char[BUFFER];
			int c;
			while ((c = reader.read(buffer)) != -1)
				sb.append(buffer, 0, c);
		} catch (IOException e) {
			log.error("", e); 
		} finally {
			close(reader);
		}
		return sb.toString();
	}
	
	private static void close(AutoCloseable con){
		if(con == null){
			return;
		}
		try{
			con.close();
		}catch(Exception ignored){
			
		}
	}
}
