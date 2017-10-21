package org.slizaa.neo4j.queryresult.ui;

import java.util.Arrays;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.slizaa.neo4j.queryresult.ui.internal.functions.GetColumnNamesFunction;
import org.slizaa.neo4j.queryresult.ui.internal.functions.GetRecordsFunction;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;

public class Main {

  public static void main(String[] args) {
    Display display = new Display();
    Shell shell = new Shell(display);
    shell.setLayout(new FillLayout());
    shell.setText("DEFAULT");
    final Browser browser;
    try {
      browser = new Browser(shell, SWT.NONE);
    } catch (SWTError e) {
      System.out.println("Could not instantiate Browser: " + e.getMessage());
      display.dispose();
      return;
    }

    //
    new GetColumnNamesFunction(browser, () -> Arrays.asList("t.fqn"));

    new GetRecordsFunction(browser, () -> createTestData());

    shell.open();
    browser.setUrl(
        "file://D:\\50-Development\\environments\\slizaa-master\\git\\slizaa-private\\03-org.slizaa.neo4j\\org.slizaa.neo4j.queryresult\\content\\result.html");
    while (!shell.isDisposed()) {
      if (!display.readAndDispatch())
        display.sleep();
    }
    display.dispose();
  }

  private static JsonArray createTestData() {

    List<String> strings = Arrays.asList("{\"t.fqn\":\"com.beust.jcommander.internal.DefaultConverterFactory\"}",
        "{\"t.fqn\":\"com.beust.jcommander.internal.Lists\"}", "{\"t.fqn\":\"com.beust.jcommander.internal.Console\"}",
        "{\"t.fqn\":\"com.beust.jcommander.internal.Sets\"}", "{\"t.fqn\":\"com.beust.jcommander.internal.Nullable\"}",
        "{\"t.fqn\":\"com.beust.jcommander.internal.Maps\"}",
        "{\"t.fqn\":\"com.beust.jcommander.internal.DefaultConsole\"}",
        "{\"t.fqn\":\"com.beust.jcommander.internal.JDK6Console\"}",
        "{\"t.fqn\":\"com.beust.jcommander.converters.CommaParameterSplitter\"}");

    Gson gson = new GsonBuilder().disableHtmlEscaping().create();
    JsonArray array = new JsonArray();

    for (String e : strings) {
      array.add(gson.toJsonTree(e));
    }

    return array;
  }
}
