package org.jvnet.jaxb2_commons.javaforkmlapi.command;

import java.util.HashMap;

import com.sun.codemodel.JDefinedClass;
import com.sun.tools.xjc.outline.Outline;

import org.jvnet.jaxb2_commons.javaforkmlapi.ClazzPool;
import org.jvnet.jaxb2_commons.javaforkmlapi.XJCJavaForKmlApiPlugin;
import org.xml.sax.ErrorHandler;
import com.sun.tools.xjc.Options;

public abstract class Command {

	protected Outline outline;

	protected Options opts;

	protected ErrorHandler errorHandler;

	protected ClazzPool pool;

	@SuppressWarnings("unused")
	private Command() {
	}

	
	public ClazzPool getPool() {
  	return pool;
  }


	public Command(Outline outline, Options opts, ErrorHandler errorHandler, ClazzPool pool) {
		this.outline = outline;
		this.opts = opts;
		this.errorHandler = errorHandler;
		this.pool = pool;
	}

	public abstract void execute();

	public Outline getOutline() {
		return outline;
	}

	public Options getOpts() {
		return opts;
	}

	public ErrorHandler getErrorHandler() {
		return errorHandler;
	}
}
