package de.micromata.javaapiforkml;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;

import javax.xml.bind.SchemaOutputResolver;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;

public class SystemOutSchemaOutput extends SchemaOutputResolver
{
  private StreamResult res;

  @Override
  public Result createOutput(String namespaceUri, String suggestedFilename) throws IOException
  {
    StringWriter out = new StringWriter();
    this.res = new StreamResult(out);
    this.res.setSystemId(suggestedFilename);
    return this.res;
  }

  @Override
  public String toString()
  {
    return this.res.getWriter().toString();
  }

  public void saveToFile(String filename) throws IOException
  {
    FileOutputStream sysfile = new FileOutputStream("" + filename);
    sysfile.write(this.res.getWriter().toString().getBytes());
  }
}
