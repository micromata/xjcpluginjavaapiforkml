// ///////////////////////////////////////////////////////////////////////////
//
// $RCSfile: $
//
// Project JaxbPluginJavaForKmlApi
//
// Author Flori (f.bachmann@micromata.de)
// Created 26.03.2009
// Copyright Micromata 26.03.2009
//
// $Id: $
// $Revision: $
// $Date: $
//
// ///////////////////////////////////////////////////////////////////////////
package org.jvnet.jaxb2_commons.javaforkmlapi;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

import org.apache.log4j.Logger;

import com.sun.codemodel.CodeWriter;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.writer.PrologCodeWriter;

// ^(//)(.*?)$
// sb.append("$2");
public class JavaAPIPrologCodeWriter extends PrologCodeWriter {
	private static final Logger LOG = Logger.getLogger(JavaAPIPrologCodeWriter.class.getName());
	public static String OWNER = "Micromata GmbH";

	public static String ORGANIZATION = "Micromata GmbH";

	public static String YEAR = "2009";

	public static String getLicense() {
		final StringBuffer sb = new StringBuffer();
		sb.append(" Copyright (c) " + YEAR + ", " + OWNER + "");
		sb.append(" All rights reserved.");
		sb.append("");
		sb.append(" Redistribution and use in source and binary forms, with or without ");
		sb.append(" modification, are permitted provided that the following conditions are met:");
		sb.append("");
		sb.append("  1. Redistributions of source code must retain the above copyright notice, ");
		sb.append("     this list of conditions and the following disclaimer.");
		sb.append("  2. Redistributions in binary form must reproduce the above copyright notice,");
		sb.append("     this list of conditions and the following disclaimer in the documentation");
		sb.append("     and/or other materials provided with the distribution.");
		sb.append("  3. Neither the name of" + ORGANIZATION + " nor the names of its contributors may be");
		sb.append("     used to endorse or promote products derived from this software without");
		sb.append("     specific prior written permission.");
		sb.append("");
		sb.append(" THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED");
		sb.append(" WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF ");
		sb.append(" MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO");
		sb.append(" EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, ");
		sb.append(" SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,");
		sb.append(" PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;");
		sb.append(" OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,");
		sb.append(" WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR ");
		sb.append(" OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ");
		sb.append(" ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.");
		sb.append("");
		sb.append(" This file contains the implementation of the KML reference class methods.");
		return sb.toString();

	}

	private final String floriProlog;

	
  @Override
  public Writer openSource(final JPackage pkg, final String fileName) throws IOException {
    final Writer w = super.openSource(pkg,fileName);
    
    final PrintWriter out = new PrintWriter(w);
    
    // write prolog if this is a java source file
    if( floriProlog != null ) {
        out.println( "//" );
        
        String s = floriProlog;
        int idx;
        while( (idx=s.indexOf('\n'))!=-1 ) {
            out.println("// "+ s.substring(0,idx) );
            s = s.substring(idx+1);
        }
        out.println("//");
        out.println();
    }
    out.flush();    // we can't close the stream for that would close the undelying stream.
    LOG.info("JavaAPIPrologCodeWriter openSource " + floriProlog);
    return w;
}
	
	public JavaAPIPrologCodeWriter(final CodeWriter core) {
		super(core, getLicense());
		floriProlog = getLicense();
		LOG.info("JavaAPIPrologCodeWriter " + floriProlog);
	}

//	public JavaAPIPrologCodeWriter() {
////		super(core, getLicense());
//  }

}
