package viso.sbeans.impl.util;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * ��ӡ��־�ĸ�����
 * */
public class LogWrapper {
	private static final String kClassName = LogWrapper.class.getName();
	private Logger logger;
	
	public LogWrapper(Logger logger){
		if(logger==null){
			throw new NullPointerException(" ����loggerΪ��. ");
		}
		this.logger = logger;
	}
	
	public Logger getLogger(){
		return this.logger;
	}
	
	public boolean isLoggable(Level level){
		return logger.isLoggable(level);
	}
	
	public void log(Level level,String message){
		if(logger.isLoggable(level)){
			log(new LogRecord(level, message));
		}
	}
	
	public void log(Level level,String message, Object param){
		if(logger.isLoggable(level)){
			LogRecord lr = new LogRecord(level,message);
			lr.setParameters(new Object[]{param});
			log(lr);
		}
	}
	
	public void log(Level level,String message, Object ...params){
		if(logger.isLoggable(level)){
			LogRecord lr = new LogRecord(level,message);
			lr.setParameters(new Object[]{params});
			log(lr);
		}
	}
	
	public void logThrow(Level level, Throwable t, String message){
		if(logger.isLoggable(level)){
			LogRecord lr = new LogRecord(level,message);
			lr.setThrown(t);
			log(lr);
		}
	}
	
	public void logThrow(Level level, Throwable t, String message, Object param){
		if(logger.isLoggable(level)){
			LogRecord lr = new LogRecord(level,message);
			lr.setThrown(t);
			lr.setParameters(new Object[]{param});
			log(lr);
		}		
	}
	
	public void logThrow(Level level, Throwable t, String message, Object ...params){
		if(logger.isLoggable(level)){
			LogRecord lr = new LogRecord(level,message);
			lr.setThrown(t);
			lr.setParameters(new Object[]{params});
			log(lr);
		}		
	}
	
	/**
	 * ȥ����ӡջ���� �Ա����� �Ҳ����ĵĲ���
	 * */
	private void log(LogRecord record){
		StackTraceElement stackcallframe = null;
		for(StackTraceElement frame : new Throwable().getStackTrace()){
			if(!frame.getClassName().equals(kClassName)){
				stackcallframe = frame;
				break;
			}
		}
		
		if(stackcallframe!=null){
			record.setSourceClassName(stackcallframe.getClassName());
			record.setSourceMethodName(stackcallframe.getMethodName());
		}
		
		try{
			logger.log(record);
		}catch(Throwable t){
			//����
		}
	}
}
