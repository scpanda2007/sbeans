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
			throw (NumberFormatException)new NumberFormatException("解析整数属性["+name+"]失败:"+value).initCause(e);
		}
	}
	
	public int getRequiredIntProperty(String name){
		String value = this.property.getProperty(name);
		if(value==null){
			throw new NullPointerException("解析属性["+name+"]为空");
		}
		try{
			return Integer.parseInt(name);
		}catch(NumberFormatException e){
			throw (NumberFormatException)new NumberFormatException("解析整数属性["+name+"]失败:"+value).initCause(e);
		}
	}
	
	public int getIntProperty(String name, int defaultValue, int min, int max){
		if(max < min){
			throw new IllegalArgumentException("最小值:"+min+" 大于最大值:"+max);
		}
		if(defaultValue < min || defaultValue > max){
			throw new IllegalArgumentException("默认值:"+defaultValue+" 不在["+min+" , "+max+" ]范围内");
		}
		int value = getIntProperty(name,defaultValue);
		if(value < min || value > max){
			throw new IllegalArgumentException("解析值:"+value+" 不在["+min+" , "+max+" ]范围内");
		}
		return value;
	}
	
	public long getLongProperty(String name, long defaultValue){
		String value = this.property.getProperty(name);
		try{
			return value == null ? defaultValue : Long.parseLong(name);
		}catch(NumberFormatException e){
			throw (NumberFormatException)new NumberFormatException("解析长整数属性["+name+"]失败:"+value).initCause(e);
		}		
	}
	
	public long getRequiredLongProperty(String name){
		String value = this.property.getProperty(name);
		if(value==null){
			throw new NullPointerException("解析属性["+name+"]为空");
		}
		try{
			return Long.parseLong(name);
		}catch(NumberFormatException e){
			throw (NumberFormatException)new NumberFormatException("解析长整数属性["+name+"]失败:"+value).initCause(e);
		}
	}
	
	public long getLongProperty(String name, long defaultValue, long min, long max){
		if(max < min){
			throw new IllegalArgumentException("最小值:"+min+" 大于最大值:"+max);
		}
		if(defaultValue < min || defaultValue > max){
			throw new IllegalArgumentException("默认值:"+defaultValue+" 不在["+min+" , "+max+" ]范围内");
		}
		long value = getLongProperty(name,defaultValue);
		if(value < min || value > max){
			throw new IllegalArgumentException("解析值:"+value+" 不在["+min+" , "+max+" ]范围内");
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
			throw new NullPointerException(" 解析的类名为空:"+name);
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
				throw new IllegalArgumentException(" 调用"+className+getPropertyText(name)+"构造函数时抛出异常:"+t,t);
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
			throw new IllegalArgumentException(""+className+getPropertyText(name)+"找不到如下参数的构造函数:"+sb.toString(),e);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			throw new IllegalArgumentException(" 找不到类	:"+className+" "+getPropertyText(name),e);
		} catch (ClassCastException e){
			throw new IllegalArgumentException(" 该类:"+className+" "+getPropertyText(name)+" 不能转化为类型:"+type.getName(),e);
		} catch (Exception e){
			throw new IllegalArgumentException(" 该类:"+className+" "+getPropertyText(name)+" 在实例化时发生异常: "+e,e);
		}
		
	}
	
	private String getPropertyText(String name){
		return name==null? "":"(参数属性名:"+name+")";
	}
	
	public <T extends Enum<T>> T getEnumProperty(String name, Class<T> type, T defaultType){
		checkNull("name",name);
		checkNull("type",type);
		checkNull("defaultType",defaultType);
		String value = this.property.getProperty(name);
		try{
			return value == null ? defaultType : Enum.valueOf(type, value);
		}catch(IllegalArgumentException e){
			throw new IllegalArgumentException(" 参数属性:"+name+"的值:"+value+" 需要为下列的值之一");
		}
	}
}
