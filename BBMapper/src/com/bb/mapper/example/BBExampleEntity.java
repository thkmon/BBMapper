package com.bb.mapper.example;

import com.bb.mapper.annotation.BBColumn;
import com.bb.mapper.annotation.BBPrimaryKey;
import com.bb.mapper.annotation.BBTable;
import com.bb.mapper.prototype.BBEntity;

@BBTable(name = "EXAMPLE_TABLE")
public class BBExampleEntity implements BBEntity {

	@BBPrimaryKey(name = "COLUMN1")
	@BBColumn(name = "COLUMN1")
	private String column1 = null;
	
	@BBColumn(name = "COLUMN2")
	private String column2 = null;
}
