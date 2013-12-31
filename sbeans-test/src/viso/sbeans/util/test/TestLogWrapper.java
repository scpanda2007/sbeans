package viso.sbeans.util.test;

import java.util.logging.Level;
import java.util.logging.Logger;

import viso.sbeans.impl.util.LogWrapper;

import org.junit.Test;

public class TestLogWrapper {
	@Test
	public void testLogNormal(){
		LogWrapper logger = new LogWrapper(Logger.getLogger("Test"));
		logger.log(Level.FINE, "test log fine");
		logger.log(Level.FINER, "test log finer");
		logger.log(Level.FINEST, "test log finest");
		logger.log(Level.INFO, "test log info");
		logger.log(Level.CONFIG, "test log config");
		logger.log(Level.SEVERE, "test log severe");
		logger.log(Level.ALL, "test log all");
	}
}
