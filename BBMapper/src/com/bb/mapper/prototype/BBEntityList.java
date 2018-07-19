package com.bb.mapper.prototype;

import java.util.ArrayList;

import com.bb.mapper.util.BBMapperUtil;

public class BBEntityList extends ArrayList<BBEntity> {
	
	public String toString() {
		
		String className = this.getClass().getName();
		
		int count = this.size();
		if (count < 1) {
			return "BBEntityList : Empty";
		}
		
		StringBuffer buff = new StringBuffer();
		for (int i=0; i<count; i++) {
			if (buff.length() > 0) {
				buff.append("\n");
			}
			
			try {
				buff.append("BBEntityList[" + i + "] : " + BBMapperUtil.convertToString((BBEntity) this.get(i)));
			} catch (Exception e) {
				buff.append(i + " : " + e.getMessage());
			}
		}

		return buff.toString();
	}
}
