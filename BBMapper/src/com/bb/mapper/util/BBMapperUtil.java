package com.bb.mapper.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import com.bb.mapper.annotation.BBColumn;
import com.bb.mapper.annotation.BBPrimaryKey;
import com.bb.mapper.annotation.BBTable;
import com.bb.mapper.prototype.BBColumnNameList;
import com.bb.mapper.prototype.BBColumnValueList;
import com.bb.mapper.prototype.BBEntity;
import com.bb.mapper.prototype.BBEntityList;

public class BBMapperUtil {

	
	public static String getTableName(BBEntity bbEntity) {
		if (bbEntity == null) {
			return null;
		}
		
		Class clsObj = bbEntity.getClass();
		Annotation[] annotations = clsObj.getDeclaredAnnotations();
		if (annotations == null || annotations.length == 0) {
			return null;
		}
		
		int annotationCount = annotations.length;
		
		for (int i=0; i<annotationCount; i++) {
			if (annotations[i] == null) {
				continue;
			}
			
			try {
				String tableName = ((BBTable)annotations[i]).name();
				if (tableName == null || tableName.length() == 0) {
					String clsName = clsObj.getName();
					if (clsName != null && clsName.length() > 0) {
						int lastDotIndex = clsName.lastIndexOf(".");
						if (lastDotIndex > -1) {
							tableName = clsName.substring(lastDotIndex + 1);
						} else {
							tableName = clsName;
						}
					}
				}
				
				if (tableName != null && tableName.length() > 0) {
					return tableName;
				}
				
			} catch (Exception e) {
				// ignore
			}
		}
		
		return null;
	}
	
	
	public static String getPrimaryKeyName(BBEntity inputData) {
		if (inputData == null) {
			return null;
		}
		
		Field[] fields = inputData.getClass().getDeclaredFields();
		if (fields == null || fields.length == 0) {
			return null;
		}
			
		int fieldCount = fields.length;
		for (int k=0; k<fieldCount; k++) {
			Field field = fields[k];
			if (field == null) {
				continue;
			}
			
			Annotation[] annotations = field.getDeclaredAnnotations();
			if (annotations == null || annotations.length == 0) {
				return null;
			}
			
			int annotationCount = annotations.length;
			
			for (int i=0; i<annotationCount; i++) {
				if (annotations[i] == null) {
					continue;
				}
				
				try {
					String primaryKeyName = ((BBPrimaryKey)annotations[i]).name();
					if (primaryKeyName == null || primaryKeyName.length() == 0) {
						primaryKeyName = field.getName();
					}
					
					if (primaryKeyName != null && primaryKeyName.length() > 0) {
						return primaryKeyName;
					}
					
				} catch (Exception e) {
					// ignore
				}
			}
		}
		
		return null;
	}
	
	
	public static Object getPrimaryKeyValue(BBEntity bbEntity) {
		if (bbEntity == null) {
			return null;
		}
		
		Field[] fields = bbEntity.getClass().getDeclaredFields();
		if (fields == null || fields.length == 0) {
			return null;
		}
			
		int fieldCount = fields.length;
		for (int k=0; k<fieldCount; k++) {
			Field field = fields[k];
			if (field == null) {
				continue;
			}
			
			Annotation[] annotations = field.getDeclaredAnnotations();
			if (annotations == null || annotations.length == 0) {
				return null;
			}
			
			int annotationCount = annotations.length;
			
			for (int i=0; i<annotationCount; i++) {
				if (annotations[i] == null) {
					continue;
				}
				
				try {
					BBPrimaryKey primaryKey = (BBPrimaryKey)annotations[i];
					if (primaryKey != null) {
						Object result = getFieldValue(bbEntity, field);
						if (result != null) {
							return result;
						}
					}
				} catch (Exception e) {
					// ignore
				}
			}
		}
		
		return null;
	}
	
	
	private static Object getFieldValue(BBEntity bbEntity, Field field) throws Exception {
		Class fieldClass = BBMapperUtil.getColumnType(field);
		if (fieldClass == null) {
			return null;
		}
		
		String fieldClassName = fieldClass.getName();
		if (fieldClassName == null || fieldClassName.length() == 0) {
			return null;
		}
		
		if (fieldClassName.indexOf("java.lang.String") > -1) {
			field.setAccessible(true);
			String result = (String) field.get(bbEntity);
			field.setAccessible(false);
			return result;
		
		} else if (fieldClassName.indexOf("java.lang.Integer") > -1) {
			field.setAccessible(true);
			Integer result = (Integer) field.get(bbEntity);
			field.setAccessible(false);
			return result;
			
		} else if (fieldClassName.indexOf("java.lang.Long") > -1) {
			field.setAccessible(true);
			Long result = (Long) field.get(bbEntity);
			field.setAccessible(false);
			return result;
			
		} else if (fieldClassName.indexOf("java.sql.Blob") > -1) {
			field.setAccessible(true);
			Blob result = (Blob) field.get(bbEntity);
			field.setAccessible(false);
			return result;
			
		} else if (fieldClassName.indexOf("java.sql.Clob") > -1) {
			field.setAccessible(true);
			Clob result = (Clob) field.get(bbEntity);
			field.setAccessible(false);
			return result;
			
		} else {
			field.setAccessible(true);
			int result = Integer.parseInt(String.valueOf(field.get(bbEntity)));
			field.setAccessible(false);
			return result;
		}
	}
	
	
	public static boolean setFieldValue(BBEntity bbEntity, Field field, ResultSet resultSet) throws Exception {
		String columnName = BBMapperUtil.getColumnName(field);
		if (columnName == null || columnName.length() == 0) {
			columnName = field.getName();
			if (columnName == null || columnName.length() == 0) {
				throw new Exception("BBMapperUtil setFieldValue : columnName is null or empty");
			}
		}
		
		Class fieldClass = BBMapperUtil.getColumnType(field);
		if (fieldClass == null) {
			return false;
		}
		
		String fieldClassName = fieldClass.getName();
		
		if (fieldClassName.indexOf("java.lang.String") > -1) {
			String value = null;
			try {
				value = resultSet.getString(columnName);
			} catch (Exception e) {
				// ignore
				return false;
			}
			
			field.setAccessible(true);
			field.set(bbEntity, value);
			field.setAccessible(false);
			
		} else if (fieldClassName.indexOf("java.lang.Integer") > -1) {
			Integer value = null;
			try {
				value = resultSet.getInt(columnName);
			} catch (Exception e) {
				// ignore
				return false;
			}
			
			field.setAccessible(true);
			field.set(bbEntity, value);
			field.setAccessible(false);
			
		} else if (fieldClassName.indexOf("java.lang.Long") > -1) {
			Long value = null;
			try {
				value = resultSet.getLong(columnName);
			} catch (Exception e) {
				// ignore
				return false;
			}
			
			field.setAccessible(true);
			field.set(bbEntity, value);
			field.setAccessible(false);
			
		} else if (fieldClassName.indexOf("java.sql.Blob") > -1) {
			Blob value = null;
			try {
				value = resultSet.getBlob(columnName);
			} catch (Exception e) {
				// ignore
				return false;
			}
			
			field.setAccessible(true);
			field.set(bbEntity, value);
			field.setAccessible(false);
			
		} else if (fieldClassName.indexOf("java.sql.Clob") > -1) {
			Clob value = null;
			try {
				value = resultSet.getClob(columnName);
			} catch (Exception e) {
				// ignore
				return false;
			}
			
			field.setAccessible(true);
			field.set(bbEntity, value);
			field.setAccessible(false);
			
		} else {
			int value = 0;
			try {
				value = resultSet.getInt(columnName);
			} catch (Exception e) {
				// ignore
				return false;
			}
			
			field.setAccessible(true);
			field.setInt(bbEntity, value);
			field.setAccessible(false);
		}
		
		return true;
	}

	
	public static String getColumnName(Field field) {
		if (field == null) {
			return null;
		}
		
		Annotation[] annotations = field.getDeclaredAnnotations();
		if (annotations == null || annotations.length == 0) {
			return null;
		}
		
		int annotationCount = annotations.length;
		
		for (int i=0; i<annotationCount; i++) {
			if (annotations[i] == null) {
				continue;
			}
			
			try {
				String columnName = ((BBColumn)annotations[i]).name();
				if (columnName == null || columnName.length() == 0) {
					columnName = field.getName();
				}
				
				if (columnName != null && columnName.length() > 0) {
					return columnName;
				}
				
			} catch (Exception e) {
				// ignore
			}
		}
		
		return null;
	}
	
	
	public static Class getColumnType(Field field) {
		if (field == null) {
			return null;
		}

		return field.getType();
	}
	
	
	public static BBColumnNameList getColumnNameList(BBEntity bbEntity) {
		
		BBColumnNameList columnNameList = null;
		
		Field[] fields = bbEntity.getClass().getDeclaredFields();
		if (fields == null || fields.length == 0) {
			return null;
		}
			
		int fieldCount = fields.length;
		for (int k=0; k<fieldCount; k++) {
			Field field = fields[k];
			if (field == null) {
				continue;
			}
		
			Annotation[] annotations = field.getDeclaredAnnotations();
			if (annotations == null || annotations.length == 0) {
				return null;
			}
			
			int annotationCount = annotations.length;
			
			for (int i=0; i<annotationCount; i++) {
				if (annotations[i] == null) {
					continue;
				}
				
				try {
					String dataColumn = ((BBColumn)annotations[i]).name();
					if (dataColumn == null || dataColumn.length() == 0) {
						dataColumn = field.getName();
					}
					
					if (dataColumn != null && dataColumn.length() > 0) {
						if (columnNameList == null) {
							columnNameList = new BBColumnNameList();
						}
						
						columnNameList.add(dataColumn);
					}
					
				} catch (Exception e) {
					// ignore
				}
			}
		}
		
		return columnNameList;
	}
	
	
	public static Object getColumnValue(BBEntity bbEntity, String columnName) throws Exception {
		if (columnName == null || columnName.length() == 0) {
			throw new Exception("DataAnnotationUtil getColumnValue : getColumnValue is null or empty.");
		}
		
		Field[] fields = bbEntity.getClass().getDeclaredFields();
		if (fields == null || fields.length == 0) {
			return null;
		}
			
		int fieldCount = fields.length;
		for (int k=0; k<fieldCount; k++) {
			Field field = fields[k];
			if (field == null) {
				continue;
			}
		
			Annotation[] annotations = field.getDeclaredAnnotations();
			if (annotations == null || annotations.length == 0) {
				return null;
			}
			
			int annotationCount = annotations.length;
			
			for (int i=0; i<annotationCount; i++) {
				if (annotations[i] == null) {
					continue;
				}
				
				try {
					String dataColumn = ((BBColumn)annotations[i]).name();
					if (dataColumn == null || dataColumn.length() == 0) {
						dataColumn = field.getName();
					}
					
					if (dataColumn != null && dataColumn.equals(columnName)) {
						Object result = getFieldValue(bbEntity, field);
						if (result != null) {
							return result;
						}
					}
					
				} catch (Exception e) {
					// ignore
				}
			}
		}
		
		return null;
	}
	
	
	public static void bind(PreparedStatement pstmt, BBColumnValueList columnValueList) throws Exception {
		if (columnValueList == null || columnValueList.size() == 0) {
			return;
		}
		
		Object bindObj = null;
		int bindCount = columnValueList.size();
		for (int i=0; i<bindCount; i++) {
			bindObj = columnValueList.get(i);
			
			if (bindObj == null) {
				pstmt.setNull(i+1, 0);
				
			} else if (bindObj instanceof String) {
				pstmt.setString(i+1, String.valueOf(bindObj));
			
			} else if (bindObj instanceof Integer) {
				pstmt.setLong(i+1, Integer.parseInt(String.valueOf(bindObj)));
				
			} else if (bindObj instanceof Long) {
				pstmt.setLong(i+1, Long.parseLong(String.valueOf(bindObj)));
				
			} else if (bindObj instanceof Blob) {
				pstmt.setBlob(i+1, (Blob)bindObj);
				
			} else if (bindObj instanceof Clob) {
				pstmt.setClob(i+1, (Clob)bindObj);
				
			} else {
				pstmt.setInt(i+1, Integer.parseInt(String.valueOf(bindObj)));
			}
		}
	}

	
	public static String join(String str, String delimiter, int count) {
		
		StringBuffer resultBuff = new StringBuffer();
		
		for (int i=0; i<count; i++) {
			if (resultBuff.length() > 0) {
				resultBuff.append(delimiter);
			}
			
			resultBuff.append(str);
		}
		
		return resultBuff.toString();
	}
	
	
	public static String join(ArrayList<String> stringList, String delimiter) {
		if (stringList == null || stringList.size() == 0) {
			return "";
		}
		
		StringBuffer resultBuff = new StringBuffer();
		
		int count = stringList.size();
		for (int i=0; i<count; i++) {
			if (resultBuff.length() > 0) {
				resultBuff.append(delimiter);
			}
			
			String str = stringList.get(i);
			if (str == null) {
				str = "";
			}
			resultBuff.append(str);
		}
		
		return resultBuff.toString();
	}
	
	
	public static String convertToString(BBEntityList bbEntityList) throws Exception {
		if (bbEntityList == null) {
			return "BBEntityList : null";
		}
		
		return bbEntityList.toString();
	}
	
	
	public static String convertToString(BBEntity bbEntity) throws Exception {
		if (bbEntity == null) {
			return "BBEntity : null";
		}
		
		StringBuffer buff = new StringBuffer();
		
		BBColumnNameList columnNameList = getColumnNameList(bbEntity);
		int columnCount = columnNameList.size();
		for (int i=0; i<columnCount; i++) {
			String columnName = columnNameList.get(i);
			Object columnValue = getColumnValue(bbEntity, columnName);
			
			if (buff.length() > 0) {
				buff.append(", ");
			}
			
			buff.append(columnName);
			buff.append("==[");
			buff.append(convertToString(columnValue));
			buff.append("]");
		}
		
		String tableName = getTableName(bbEntity);
		
		return "BBEntity[" + tableName + "] : " + buff.toString();
	}
	
	
	public static String convertToString(Object columnValue) {
		if (columnValue == null) {
			return "null";
			
		} else if (columnValue instanceof String) {
			return String.valueOf(columnValue);
			
		} else if (columnValue instanceof Integer) {
			return String.valueOf(columnValue);
			
		} else if (columnValue instanceof Long) {
			return String.valueOf(columnValue);
			
		} else if (columnValue instanceof Blob) {
			return "BLOB";
			
		} else if (columnValue instanceof Clob) {
			return "CLOB";
		}
		
		return columnValue.toString();
	}
	
	
	public static String replaceOne(String originStr, String preStr, String postStr) {
		if (originStr == null || originStr.length() == 0) {
			return "";
		}
		
		if (preStr == null || preStr.length() == 0) {
			return originStr;
		}
		
		if (postStr == null) {
			postStr = "";
		}
		
		int index = originStr.indexOf(preStr);
		if (index > -1) {
			return originStr.substring(0, index) + postStr + originStr.substring(index + preStr.length());
		}
		
		return originStr;
	}
}
