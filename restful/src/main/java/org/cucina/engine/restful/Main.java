package org.cucina.engine.restful;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;

import org.cucina.engine.ProcessGuardian;

/**
 * JAVADOC for Class Level
 *
 * @author $Author: $
 * @version $Revision: $
 */
@EnableAutoConfiguration
@ComponentScan
public class Main {
	private static final Logger LOG = LoggerFactory.getLogger(Main.class);

	@Autowired
	private Environment environment;

	/**
	 * JAVADOC Method Level Comments
	 *
	 * @param args JAVADOC.
	 *
	 * @throws Exception JAVADOC.
	 */
	public static void main(String[] args) throws Exception {
		ApplicationContext ac = SpringApplication.run(Main.class, args);

		if (LOG.isTraceEnabled()) {
			String[] names = ac.getBeanDefinitionNames();

			for (int i = 0; i < names.length; i++) {
				LOG.trace(names[i]);
			}
		}
	}

	@Bean
	public ActorSystem actorSystem() {
		ActorSystem sys = ActorSystem.create("workflowSystem");

		return sys;
	}

}
