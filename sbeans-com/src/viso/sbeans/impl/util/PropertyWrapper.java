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
			throw (NumberFormatException) new NumberFormatException("����ֵ ["
					+ name + "] ������һ����Ч������:" + value).initCause(e);
		}
	}
	
	public int getRequiredIntProperty(String name){
		String value = property.getProperty(name);
		if(value==null){
			throw new NullPointerException("����ֵ["+name+"]������");
		}
		try {
			return Integer.parseInt(name);
		} catch (NumberFormatException e) {
			throw (NumberFormatException) new NumberFormatException("����ֵ ["
					+ name + "] ������һ����Ч������:" + value).initCause(e);
		}
	}
	
	public int getIntProperty(String name, int defaultValue, int min, int max){
		if(min > max){
			throw new IllegalArgumentException(" ���� ��Сֵ["+min+"] �� ���ֵ["+max+"]��С��ϵ����");
		}else if(defaultValue < min || defaultValue > max){
			throw new IllegalArgumentException(" Ĭ��ֵ["+defaultValue+"] ��Ҫ��["+min+" , "+max+" ]�ķ�Χ֮��");
		}
		int value = getIntProperty(name,defaultValue);
		if(value < min || value > max){
			throw new IllegalArgumentException(" ����ֵ["+name+"] ��Ҫ��["+min+" , "+max+" ]�ķ�Χ֮��");
		}
		return value;
	}
}
