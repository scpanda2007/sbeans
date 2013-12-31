package viso.sbeans.impl.util;

import java.util.Properties;

public class PropertyWrapper {
	private final Properties property;
	
	public PropertyWrapper(Properties property){
		this.property = property;
	}
	
	public Properties getProperty(){
		return property;
	}
	
	public String getProperty(String name){
		return property.getProperty(name);
	}
	
	public String getProperty(String name,String defaultValue){
		return property.getProperty(name, defaultValue);
	}
	
	public boolean getBooleanProperty(String name, boolean defaultValue){
		String value = property.getProperty(name);
		return value==null? defaultValue : Boolean.parseBoolean(value);
	}
	
	public int getIntProperty(String name, int defaultValue) {
		String value = property.getProperty(name);
		try {
			return value == null ? defaultValue : Integer.parseInt(name);
		} catch (NumberFormatException e) {
			throw (NumberFormatException) new NumberFormatException("解析值 ["
					+ name + "] 必须是一个有效的整数:" + value).initCause(e);
		}
	}
	
	public int getRequiredIntProperty(String name){
		String value = property.getProperty(name);
		if(value==null){
			throw new NullPointerException("解析值["+name+"]不存在");
		}
		try {
			return Integer.parseInt(name);
		} catch (NumberFormatException e) {
			throw (NumberFormatException) new NumberFormatException("解析值 ["
					+ name + "] 必须是一个有效的整数:" + value).initCause(e);
		}
	}
	
	public int getIntProperty(String name, int defaultValue, int min, int max){
		if(min > max){
			throw new IllegalArgumentException(" 参数 最小值["+min+"] 和 最大值["+max+"]大小关系错误");
		}else if(defaultValue < min || defaultValue > max){
			throw new IllegalArgumentException(" 默认值["+defaultValue+"] 需要在["+min+" , "+max+" ]的范围之内");
		}
		int value = getIntProperty(name,defaultValue);
		if(value < min || value > max){
			throw new IllegalArgumentException(" 解析值["+name+"] 需要在["+min+" , "+max+" ]的范围之内");
		}
		return value;
	}
}
