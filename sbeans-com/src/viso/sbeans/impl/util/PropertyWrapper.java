package viso.sbeans.impl.util;

import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

import static viso.sbeans.impl.util.Objects.checkNull;

public class PropertyWrapper {
	private final Properties property;
	
	public PropertyWrapper(Properties property){
		this.property = property;
	}
	
	public String getProperty(String name){
		return this.property.getProperty(name);
	}
	
	public String getProerty(String name, String defaultValue){
		String value = this.property.getProperty(name);
		return value == null ? defaultValue : value;
	}
	
	public boolean getBooleanProperty(String name, boolean defaultValue){
		String value = this.property.getProperty(name);
		return value == null ? defaultValue : Boolean.parseBoolean(value);
	}
	
	public int getIntProperty(String name, int defaultValue){
		String value = this.property.getProperty(name);
		try{
			return value == null ? defaultValue : Integer.parseInt(name);
		}catch(NumberFormatException e){
			throw (NumberFormatException)new NumberFormatException("������������["+name+"]ʧ��:"+value).initCause(e);
		}
	}
	
	public int getRequiredIntProperty(String name){
		String value = this.property.getProperty(name);
		if(value==null){
			throw new NullPointerException("��������["+name+"]Ϊ��");
		}
		try{
			return Integer.parseInt(name);
		}catch(NumberFormatException e){
			throw (NumberFormatException)new NumberFormatException("������������["+name+"]ʧ��:"+value).initCause(e);
		}
	}
	
	public int getIntProperty(String name, int defaultValue, int min, int max){
		if(max < min){
			throw new IllegalArgumentException("��Сֵ:"+min+" �������ֵ:"+max);
		}
		if(defaultValue < min || defaultValue > max){
			throw new IllegalArgumentException("Ĭ��ֵ:"+defaultValue+" ����["+min+" , "+max+" ]��Χ��");
		}
		int value = getIntProperty(name,defaultValue);
		if(value < min || value > max){
			throw new IllegalArgumentException("����ֵ:"+value+" ����["+min+" , "+max+" ]��Χ��");
		}
		return value;
	}
	
	public long getLongProperty(String name, long defaultValue){
		String value = this.property.getProperty(name);
		try{
			return value == null ? defaultValue : Long.parseLong(name);
		}catch(NumberFormatException e){
			throw (NumberFormatException)new NumberFormatException("��������������["+name+"]ʧ��:"+value).initCause(e);
		}		
	}
	
	public long getRequiredLongProperty(String name){
		String value = this.property.getProperty(name);
		if(value==null){
			throw new NullPointerException("��������["+name+"]Ϊ��");
		}
		try{
			return Long.parseLong(name);
		}catch(NumberFormatException e){
			throw (NumberFormatException)new NumberFormatException("��������������["+name+"]ʧ��:"+value).initCause(e);
		}
	}
	
	public long getLongProperty(String name, long defaultValue, long min, long max){
		if(max < min){
			throw new IllegalArgumentException("��Сֵ:"+min+" �������ֵ:"+max);
		}
		if(defaultValue < min || defaultValue > max){
			throw new IllegalArgumentException("Ĭ��ֵ:"+defaultValue+" ����["+min+" , "+max+" ]��Χ��");
		}
		long value = getLongProperty(name,defaultValue);
		if(value < min || value > max){
			throw new IllegalArgumentException("����ֵ:"+value+" ����["+min+" , "+max+" ]��Χ��");
		}
		return value;		
	}
	
	public <T> T getClassInstanceProperty(String name, Class<T> type, Class<?>[] paramTypes, Object ...params){
		String className = this.property.getProperty(name);
		return className==null? null : getClassInstance(name, className, type, paramTypes, params);
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getClassInstanceProperty(String name, String defaultClass, Class<T> type, Class<?>[] paramTypes, Object ...params){
		Object instance = getClassInstanceProperty(name, type, paramTypes, params);
		if(instance!=null){
			return (T) instance;
		}
		if(defaultClass==null){
			return null;
		}
		return getClassInstanceProperty(defaultClass, type, paramTypes, params);
	}
	
	private <T> T getClassInstance(String name, String className, Class<T> type, Class<?>[] paramTypes, Object ...params){
		if(className==null){
			throw new NullPointerException(" ����������Ϊ��:"+name);
		}
		try {
			return Class.forName(className).asSubclass(type).getConstructor(paramTypes).newInstance(params);
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			Throwable t = e.getCause();
			if(t instanceof RuntimeException){
				throw (RuntimeException)t;
			}else if(t instanceof Error){
				throw (Error)t;
			}else{
				throw new IllegalArgumentException(" ����"+className+getPropertyText(name)+"���캯��ʱ�׳��쳣:"+t,t);
			}
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			StringBuilder sb = new StringBuilder();
			boolean first = true;
			for(Class<?> paramType:paramTypes){
				if(first){
					first = false;
				}
				else{
					sb.append(" ,");
				}
				sb.append(paramType.getName());
			}
			throw new IllegalArgumentException(""+className+getPropertyText(name)+"�Ҳ������²����Ĺ��캯��:"+sb.toString(),e);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			throw new IllegalArgumentException(" �Ҳ�����	:"+className+" "+getPropertyText(name),e);
		} catch (ClassCastException e){
			throw new IllegalArgumentException(" ����:"+className+" "+getPropertyText(name)+" ����ת��Ϊ����:"+type.getName(),e);
		} catch (Exception e){
			throw new IllegalArgumentException(" ����:"+className+" "+getPropertyText(name)+" ��ʵ����ʱ�����쳣: "+e,e);
		}
		
	}
	
	private String getPropertyText(String name){
		return name==null? "":"(����������:"+name+")";
	}
	
	public <T extends Enum<T>> T getEnumProperty(String name, Class<T> type, T defaultType){
		checkNull("name",name);
		checkNull("type",type);
		checkNull("defaultType",defaultType);
		String value = this.property.getProperty(name);
		try{
			return value == null ? defaultType : Enum.valueOf(type, value);
		}catch(IllegalArgumentException e){
			throw new IllegalArgumentException(" ��������:"+name+"��ֵ:"+value+" ��ҪΪ���е�ֵ֮һ");
		}
	}
}
