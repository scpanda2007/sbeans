package viso.sbeans.impl.util;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * »’÷æ∏®÷˙¿‡
 * */
public class LogWrapper {
	private static final String kClassName = LogWrapper.class.getName();
	private final Logger logger;
	public LogWrapper(Logger logger){
		this.logger = logger;
	}
	
	public void log(Level level,String message){
		if(logger.isLoggable(level)){
			log(new LogRecord(level,message));
		}
	}
	
	public void log(Level level,String message,Object param){
		if(logger.isLoggable(level)){
			LogRecord lr = new LogRecord(level,message);
			lr.setParameters(new Object[]{param});
			log(lr);
		}
	}
	
	public void log(Level level,String message,Object ...params){
		if(logger.isLoggable(level)){
			LogRecord lr = new LogRecord(level,message);
			lr.setParameters(params);
			log(lr);
		}
	}
	
	public void logThrow(Level level,Throwable t,String message){
		if(logger.isLoggable(level)){
			LogRecord lr = new LogRecord(level,message);
			lr.setThrown(t);
			log(lr);
		}
	}
	
	public void logThrow(Level level,Throwable t,String message,Object param){
		if(logger.isLoggable(level)){
			LogRecord lr = new LogRecord(level,message);
			lr.setThrown(t);
			lr.setParameters(new Object[]{param});
			log(lr);
		}
	}
	
	public void logThrow(Level level,Throwable t,String message,Object ...params){
		if(logger.isLoggable(level)){
			LogRecord lr = new LogRecord(level,message);
			lr.setThrown(t);
			lr.setParameters(params);
			log(lr);
		}
	}
	private void log(LogRecord lr){
		StackTraceElement trackframe = null;
		for(StackTraceElement stacktrace : new Throwable().getStackTrace()){
			if(!stacktrace.getClassName().equals(kClassName)){
				trackframe = stacktrace;
				break;
			}
		}
		
		if(trackframe!=null){
			lr.setSourceClassName(trackframe.getClassName());
			lr.setSourceMethodName(trackframe.getMethodName());
		}
		
		try{
			logger.log(lr);
		}catch(Throwable t){
			//∫ˆ¬‘
		}
	}
}
