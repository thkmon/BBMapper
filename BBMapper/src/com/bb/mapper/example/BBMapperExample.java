package com.bb.mapper.example;

import com.bb.mapper.BBMapper;
import com.bb.mapper.prototype.BBEntityList;

public class BBMapperExample {
	
	public static void main(String[] args) {
		BBMapperExample mapperExample = new BBMapperExample();
		mapperExample.executeExample();
	}
	
	
	public void executeExample() {
		
		try {
			BBExampleEntity entity = new BBExampleEntity();
			
			BBMapper bbMapper = new BBMapper();
			BBEntityList resultList = bbMapper.select(entity, " SELECT 'test1' column1, 'test2' column2 ");
			System.out.println(resultList);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}



