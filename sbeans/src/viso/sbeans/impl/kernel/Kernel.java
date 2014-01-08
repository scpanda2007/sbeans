package viso.sbeans.impl.kernel;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import viso.sbeans.app.AppListener;
import viso.sbeans.impl.service.data.DataService;
import viso.sbeans.impl.service.server.SessionService;
import viso.sbeans.impl.util.LogWrapper;
import viso.sbeans.impl.util.PropertyWrapper;
import viso.sbeans.kernel.CompomentRegister;

public class Kernel {
	
	private static final String PCK_NAME = "viso.sbeans.kernel.Kernel";
	public static final String kAppListener = "viso.sbeans.app.AppListener";
	
	DataService dataService;
	SessionService sessionService;
	
	private static final LogWrapper logger = new LogWrapper(Logger.getLogger(PCK_NAME));
	
	class CompomentRegisterImpl implements CompomentRegister{
		
		Map<Class<?>,Object> compoments;
		
		public CompomentRegisterImpl(){
			compoments = new HashMap<Class<?>,Object>();
		}
		
		public synchronized void  register(Class<?> klazz,Object object){
			
			if(!klazz.isInstance(object)){
				throw new IllegalArgumentException("对象不是"+klazz.getName()+"的实例");
			}
			
			for(Class<?> iter : compoments.keySet()){
				if(iter.isAssignableFrom(klazz)){
					throw new IllegalStateException("已经存在注册的类"+klazz.getName()+"的实例了");
				}
			}
			
			compoments.put(klazz, object);
		}
		
		public synchronized <T> T getCompoment(Class<T> klazz){
			for(Class<?> iter : compoments.keySet()){
				if(iter.isAssignableFrom(klazz)){
					return klazz.cast(compoments.get(iter));
				}
			}
			return null;
		}
	}
	
	public Kernel(Properties property) throws Exception {
		try {
			CompomentRegisterImpl register = new CompomentRegisterImpl();
			PropertyWrapper properties = new PropertyWrapper(property);
			AppListener appListener = properties.getClassInstanceProperty(kAppListener, AppListener.class, new Class<?>[]{}, new Object[]{});
			if(appListener==null){
				throw new IllegalStateException("需要指定一个AppListener。");
			}
			appListener.initialize(property);
			register.register(AppListener.class, appListener);
			sessionService = new SessionService(property, register);
			sessionService.start();
		} catch (Exception e) {
			logger.logThrow(Level.FINEST, e, "启动失败");
			shutdown();
		}
	}
	
	public void shutdown(){
		if(dataService!=null){
			try{
				dataService.shutdown();
			}catch(Throwable t){
				logger.logThrow(Level.FINEST, t, "关闭数据服务失败");
			}
		}
		if(sessionService!=null){
			try{
				sessionService.shutdown();
			}catch(Throwable t){
				logger.logThrow(Level.FINEST, t, "关闭网络服务失败");
			}
		}
	}
	
	public static void main(String args[]) throws Exception{
		Properties property = new Properties();
		new Kernel(property);
	}
}
