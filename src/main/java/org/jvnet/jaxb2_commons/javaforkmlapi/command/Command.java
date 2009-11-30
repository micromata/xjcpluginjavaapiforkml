package org.jvnet.jaxb2_commons.javaforkmlapi.command;

import org.jvnet.jaxb2_commons.javaforkmlapi.ClazzPool;
import org.xml.sax.ErrorHandler;

import com.sun.codemodel.JCodeModel;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.outline.Outline;

public abstract class Command {

	protected Outline outline;

	protected Options opts;

	protected ErrorHandler errorHandler;

	protected ClazzPool pool;

	protected JCodeModel cm;

	@SuppressWarnings("unused")
	private Command() {
	}

	
	public ClazzPool getPool() {
  	return pool;
  }


	public Command(final Outline outline, final Options opts, final ErrorHandler errorHandler, final ClazzPool pool) {
		this.outline = outline;
		this.opts = opts;
		this.errorHandler = errorHandler;
		this.pool = pool;
		this.cm = outline.getCodeModel();
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


	public JCodeModel getCm() {
	  return cm;
  }
}
