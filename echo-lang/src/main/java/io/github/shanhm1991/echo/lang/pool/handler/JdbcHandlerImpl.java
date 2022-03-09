package io.github.shanhm1991.echo.lang.pool.handler;

import java.util.List;
import java.util.Map;

import io.github.shanhm1991.echo.lang.pool.JdbcHelper;

/**
 * 
 * @author shanhm1991@163.com
 *
 */
public class JdbcHandlerImpl extends JdbcHelper implements JdbcHandler {
	
	@Override
	public List<Map<String, Object>> queryForList(String poolName, String sql, Map<String, Object> paramMap) throws Exception {
		return _queryForList(poolName, sql, paramMap);
	}

	@Override
	public int execute(String poolName, String sql, Map<String, Object> paramMap) throws Exception {
		return _execute(poolName, sql, paramMap); 
	}

	@Override
	public int[] batchExecute(String poolName, String sql, List<Map<String, Object>> paramMaps) throws Exception {
		return _batchExecute(poolName, sql, paramMaps);
	}
	
	public void startTransaction(String poolName) throws Exception {
		_startTransaction(poolName); 
	}
	
	public void endTransaction(String poolName) throws Exception {
		_endTransaction(poolName); 
	}
}
