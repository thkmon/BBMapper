package com.bb.mapper;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.bb.mapper.prototype.BBColumnNameList;
import com.bb.mapper.prototype.BBColumnValueList;
import com.bb.mapper.prototype.BBEntity;
import com.bb.mapper.prototype.BBEntityList;
import com.bb.mapper.util.BBMapperUtil;

public class BBMapper {

	private String dbUrl = "";
	private String dbUser = "";
	private String dbPassword = "";
	
	private Connection privateConn = null;
	private String sql = null;
	private BBColumnValueList columnValueList = null;

	
	public BBMapper(String url, int port, String dbName, String dbUser, String dbPassword) {
		// "jdbc:mysql://localhost:3306/ddoc?useUnicode=true&characterEncoding=utf8";
		this.dbUrl = "jdbc:mysql://" + url + ":" + port + "/" + dbName + "?useUnicode=true&characterEncoding=utf8";
		this.dbUser = dbUser;
		this.dbPassword = dbPassword;
	}
	
	
	public String getSqlText() {
		if (sql == null || sql.length() == 0) {
			return "";
		}
		
		if (columnValueList == null || columnValueList.size() == 0) {
			return sql.trim();
		}
		
		String result = sql;
		
		int columnValueCount = columnValueList.size();
		for (int i=0; i<columnValueCount; i++) {
			Object columnValue = columnValueList.get(i);
			result = BBMapperUtil.replaceOne(result, "?", "'" + BBMapperUtil.convertToString(columnValue) + "'");
		}
		
		return result.trim();
	}
	
	
	public Connection getConnection() {
		return privateConn;
	}
	
	
	public void setConnection() {

		try {
			if (privateConn != null) {
				if (!privateConn.isClosed()) {
					privateConn.rollback();
					privateConn.close();
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			
		} finally {
			privateConn = null;
		}
		
		try {
			Class.forName("com.mysql.jdbc.Driver");
			privateConn = DriverManager.getConnection(this.dbUrl, this.dbUser, this.dbPassword);
			privateConn.setAutoCommit(false);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	private void rollbackAndClose(Connection conn) {
		try {
			if (conn != null) {
				conn.rollback();
				conn.close();
			}
			
		} catch (Exception e) {
			conn = null;
			
		} finally {
			conn = null;
		}
	}
	
	
	private void close(Connection conn) {
		try {
			if (conn != null) {
				conn.close();
			}
			
		} catch (Exception e) {
			conn = null;
			
		} finally {
			conn = null;
		}
	}
	
	
	private void close(ResultSet resultSet) {
		try {
			if (resultSet != null) {
				resultSet.close();
			}
			
		} catch (Exception e) {
			resultSet = null;
			
		} finally {
			resultSet = null;
		}
	}
	
	
	private void close(PreparedStatement pstmt) {
		try {
			if (pstmt != null) {
				pstmt.close();
			}
			
		} catch (Exception e) {
			pstmt = null;
			
		} finally {
			pstmt = null;
		}
	}
	
	
	public BBEntity selectSingleRow(BBEntity bbEntity) throws Exception {
		String sql = "";
		
		String tableName = BBMapperUtil.getTableName(bbEntity);
		if (tableName == null || tableName.length() == 0) {
			throw new Exception("DataMapper select : tableName is null or empty");
		}
		
		String primaryKeyName = BBMapperUtil.getPrimaryKeyName(bbEntity);
		if (tableName == null || tableName.length() == 0) {
			throw new Exception("DataMapper select : primaryKeyName is null or empty");
		}
		
		Object primaryKeyValue = BBMapperUtil.getPrimaryKeyValue(bbEntity);
		if (tableName == null || tableName.length() == 0) {
			throw new Exception("DataMapper select : primaryKeyValue is null or empty");
		}
		
		sql = "SELECT * FROM " + tableName + " WHERE " + primaryKeyName + " = ?";
		BBColumnValueList bindVector = new BBColumnValueList();
		bindVector.add(primaryKeyValue);
				
		BBEntityList list = select(bbEntity, sql, bindVector, false);
		if (list == null || list.size() == 0) {
			return null;
		}
		
		if (list.size() > 1) {
			throw new Exception("BBMapper selectSingleRow : count == [" + list.size() + "]");
		}
		
		return list.get(0);
	}
	

	public BBEntityList select(BBEntity bbEntity, String sql) throws Exception {
		return select(bbEntity, sql, null, false);
	}
	
	
	public BBEntityList select(BBEntity bbEntity, String sql, BBColumnValueList columnValueList) throws Exception {
		return select(bbEntity, sql, columnValueList, false);
	}
	
	
	private BBEntityList select(BBEntity bbEntity, String sql, BBColumnValueList columnValueList, boolean bTransaction) throws Exception {
		return selectList(bbEntity, sql, columnValueList, false);
	}
	
	
	private BBEntityList selectList(BBEntity inputEntity, String sql, BBColumnValueList columnValueList, boolean bTransaction) throws Exception {
		BBEntityList entityList = null;
		
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		
		try {
			if (!bTransaction || privateConn == null) {
				setConnection();
			}
			
			pstmt = privateConn.prepareStatement(sql);
			BBMapperUtil.bind(pstmt, columnValueList);
			
			// save sql and columnValueList
			this.sql = sql;
			this.columnValueList = columnValueList;
			
			resultSet = pstmt.executeQuery();
			
			if (resultSet != null) {
				while (resultSet.next()) {
					
					BBEntity oneEntity = (BBEntity) (Class.forName(inputEntity.getClass().getName()).newInstance());
					
					Field[] fields = oneEntity.getClass().getDeclaredFields();
					int fieldCount = fields.length;
					for (int i=0; i<fieldCount; i++) {
						Field field = fields[i];
						if (field == null) {
							continue;
						}
						
						BBMapperUtil.setFieldValue(oneEntity, field, resultSet);
					}
					
					if (entityList == null) {
						entityList = new BBEntityList();
					}
					
					entityList.add(oneEntity);
				}
			}
			
		} catch (Exception e) {
			rollbackAndClose(privateConn);
			throw e;
			
		} finally {
			close(resultSet);
			close(pstmt);
			
			if (!bTransaction) {
				close(privateConn);
			}			
		}
		
		return entityList;
	}
	
	
	public int insert(BBEntity bbEntity) throws Exception {
		return insert(bbEntity, false);
	}
	
	
	public int insert(BBEntity bbEntity, boolean bTransaction) throws Exception {
		
		int bResult = -1;
		
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		
		try {
			
			String tableName = BBMapperUtil.getTableName(bbEntity);
			if (tableName == null || tableName.length() == 0) {
				throw new Exception("DataMapper insert : tableName is null or empty");
			}
			
			BBColumnNameList columnNameList = BBMapperUtil.getColumnNameList(bbEntity);
			if (columnNameList == null || columnNameList.size() == 0) {
				throw new Exception("DataMapper insert : columnNameList is null or empty");
			}
			
			int columnNameCount = columnNameList.size();
			
			StringBuffer sqlBuff = new StringBuffer();
			sqlBuff.append("INSERT INTO ");
			sqlBuff.append(tableName);
			sqlBuff.append(" (");
			sqlBuff.append(BBMapperUtil.join(columnNameList, ", "));
			sqlBuff.append(") ");
			sqlBuff.append(" values ");
			sqlBuff.append(" (");
			sqlBuff.append(BBMapperUtil.join("?", ", ", columnNameCount));
			sqlBuff.append(")");
			
			if (!bTransaction || privateConn == null) {
				setConnection();
			}
			
			String sql = sqlBuff.toString();
			BBColumnValueList columnValueList = columnNameList.convertToColumnValueList(bbEntity);
			
			pstmt = privateConn.prepareStatement(sql);
			BBMapperUtil.bind(pstmt, columnValueList);
			
			// save sql and columnValueList
			this.sql = sql;
			this.columnValueList = columnValueList;
			
			bResult = pstmt.executeUpdate();
			if (!bTransaction) {
				privateConn.commit();
			}
			
		} catch (Exception e) {
			rollbackAndClose(privateConn);
			throw e;
			
		} finally {
			close(resultSet);
			close(pstmt);
			
			if (!bTransaction) {
				close(privateConn);
			}			
		}
		
		return bResult;
	}
	
	
	public int update(BBEntity bbEntity) throws Exception {
		return update(bbEntity, false);
	}
	
	
	public int update(BBEntity bbEntity, boolean bTransaction) throws Exception {
		
		int bResult = -1;
		
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		
		try {
			
			String tableName = BBMapperUtil.getTableName(bbEntity);
			if (tableName == null || tableName.length() == 0) {
				throw new Exception("DataMapper update : tableName is null or empty");
			}
			
			BBColumnNameList columnNameList = BBMapperUtil.getColumnNameList(bbEntity);
			if (columnNameList == null || columnNameList.size() == 0) {
				throw new Exception("DataMapper update : columnNameList is null or empty");
			}
			
			String primaryKeyName = BBMapperUtil.getPrimaryKeyName(bbEntity);
			
			StringBuffer sqlBuff = new StringBuffer();
			sqlBuff.append("UPDATE ");
			sqlBuff.append(tableName);
			sqlBuff.append(" SET ");
			
			int columnNameCount = columnNameList.size();
			int lastIndex = columnNameCount - 1;
			for (int i=0; i<columnNameCount; i++) {
				String columnName = columnNameList.get(i);
				sqlBuff.append(columnName);
				if (i < lastIndex) {
					sqlBuff.append(" = ?, ");
				} else {
					sqlBuff.append(" = ? ");
				}
			}
			
			sqlBuff.append(" WHERE ");
			sqlBuff.append(primaryKeyName);
			sqlBuff.append(" = ?");
			
			if (!bTransaction || privateConn == null) {
				setConnection();
			}
			
			String sql = sqlBuff.toString();
			
			BBColumnValueList columnValueList = columnNameList.convertToColumnValueList(bbEntity);
			Object primaryKeyValue = BBMapperUtil.getPrimaryKeyValue(bbEntity);
			columnValueList.add(primaryKeyValue);
			
			pstmt = privateConn.prepareStatement(sql);
			BBMapperUtil.bind(pstmt, columnValueList);
			
			// save sql and columnValueList
			this.sql = sql;
			this.columnValueList = columnValueList;
			
			bResult = pstmt.executeUpdate();
			if (!bTransaction) {
				privateConn.commit();
			}
			
		} catch (Exception e) {
			rollbackAndClose(privateConn);
			throw e;
			
		} finally {
			close(resultSet);
			close(pstmt);
			
			if (!bTransaction) {
				close(privateConn);
			}			
		}
		
		return bResult;
	}
	
	
	public int delete(BBEntity bbEntity) throws Exception {
		return delete(bbEntity, false);
	}
	
	
	public int delete(BBEntity bbEntity, boolean bTransaction) throws Exception {
		
		int bResult = -1;
		
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		
		try {
			
			String tableName = BBMapperUtil.getTableName(bbEntity);
			if (tableName == null || tableName.length() == 0) {
				throw new Exception("DataMapper delete : tableName is null or empty");
			}
			
			BBColumnNameList columnNameList = BBMapperUtil.getColumnNameList(bbEntity);
			if (columnNameList == null || columnNameList.size() == 0) {
				throw new Exception("DataMapper delete : columnNameList is null or empty");
			}
			
			String primaryKeyName = BBMapperUtil.getPrimaryKeyName(bbEntity);
			
			StringBuffer sqlBuff = new StringBuffer();
			sqlBuff.append("DELETE FROM ");
			sqlBuff.append(tableName);
			sqlBuff.append(" WHERE ");
			sqlBuff.append(primaryKeyName);
			sqlBuff.append(" = ?");
			
			if (!bTransaction || privateConn == null) {
				setConnection();
			}
			
			String sql = sqlBuff.toString();
			
			Object primaryKeyValue = BBMapperUtil.getPrimaryKeyValue(bbEntity);
			BBColumnValueList columnValueList = new BBColumnValueList();
			columnValueList.add(primaryKeyValue);
			
			pstmt = privateConn.prepareStatement(sql);
			BBMapperUtil.bind(pstmt, columnValueList);
			
			// save sql and columnValueList
			this.sql = sql;
			this.columnValueList = columnValueList;
			
			bResult = pstmt.executeUpdate();
			if (!bTransaction) {
				privateConn.commit();
			}
			
		} catch (Exception e) {
			rollbackAndClose(privateConn);
			throw e;
			
		} finally {
			close(resultSet);
			close(pstmt);
			
			if (!bTransaction) {
				close(privateConn);
			}			
		}
		
		return bResult;
	}
}