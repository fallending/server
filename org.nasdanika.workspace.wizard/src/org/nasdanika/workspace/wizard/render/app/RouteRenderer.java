package org.nasdanika.workspace.wizard.render.app;

public class RouteRenderer {


  protected static String nl;
  public static synchronized RouteRenderer create(String lineSeparator)
  {
    nl = lineSeparator;
    RouteRenderer result = new RouteRenderer();
    nl = null;
    return result;
  }

  public final String NL = nl == null ? (System.getProperties().getProperty("line.separator")) : nl;
  protected final String TEXT_1 = "package ";
  protected final String TEXT_2 = ";" + NL + "" + NL + "import org.nasdanika.html.Accordion;" + NL + "import org.nasdanika.html.ApplicationPanel;" + NL + "import org.nasdanika.html.Breadcrumbs;" + NL + "import org.nasdanika.html.Fragment;" + NL + "import org.nasdanika.html.HTMLFactory;" + NL + "import org.nasdanika.html.HTMLFactory.Glyphicon;" + NL + "import org.nasdanika.html.Navbar;" + NL + "import org.nasdanika.html.Theme;" + NL + "import org.nasdanika.html.UIElement.DeviceSize;" + NL + "import org.nasdanika.html.UIElement.Event;" + NL + "import org.nasdanika.html.UIElement.Size;" + NL + "import org.nasdanika.html.UIElement.Style;" + NL + "import org.nasdanika.web.Action;" + NL + "import org.nasdanika.web.HttpContext;" + NL + "import org.nasdanika.web.Route;" + NL + "import org.nasdanika.web.WebContext;" + NL + "import java.util.Date;" + NL + "" + NL + "/**" + NL + " * Route to demonstrate/test HTMLFactory capabilities" + NL + " *" + NL + " */" + NL + "public class ";
  protected final String TEXT_3 = "Route implements Route {" + NL + "" + NL + "\t@Override" + NL + "\tpublic Action execute(WebContext context, Object... args) throws Exception {" + NL + "\t\tfinal HTMLFactory htmlFactory = context.adapt(HTMLFactory.class);" + NL + "\t\t" + NL + "\t\tApplicationPanel appPanel = htmlFactory.applicationPanel()" + NL + "\t\t\t\t.header(\"";
  protected final String TEXT_4 = "\")" + NL + "\t\t\t\t.headerLink(\"#\")" + NL + "\t\t\t\t.footer(" + NL + "\t\t\t\t\t\thtmlFactory.link(\"#\", \"Contact us\"), " + NL + "\t\t\t\t\t\t\"&nbsp;&middot;&nbsp;\", " + NL + "\t\t\t\t\t\thtmlFactory.link(\"#\", \"Privacy Policy\"))" + NL + "\t\t\t\t.width(800);" + NL + "\t\t" + NL + "\t\tNavbar navBar = htmlFactory.navbar(\"Welcome back, Joe Doe\", \"#\")" + NL + "\t\t\t\t.item(htmlFactory.link(\"#\", \"Accounts\"), true, false)" + NL + "\t\t\t\t.item(htmlFactory.link(\"#\", \"Customer service\"), false, true);" + NL + "\t\t\t\t\t\t" + NL + "\t\tnavBar.dropdown(\"Transfer\", false)" + NL + "\t\t\t\t.item(htmlFactory.link(\"#\", \"Internal\"))" + NL + "\t\t\t\t.divider()" + NL + "\t\t\t\t.header(\"External transfers\")" + NL + "\t\t\t\t.item(htmlFactory.link(\"#\", \"Wire\"))" + NL + "\t\t\t\t.item(htmlFactory.link(\"#\", \"Payment Gateway\"));" + NL + "\t\t" + NL + "\t\tBreadcrumbs breadcrumbs = htmlFactory.breadcrumbs()" + NL + "\t\t\t\t.item(\"#\", \"Home\")" + NL + "\t\t\t\t.item(\"#\", \"My page\")" + NL + "\t\t\t\t.item(null, \"Details\");" + NL + "\t\t" + NL + "\t\tappPanel.navigation(navBar, breadcrumbs);" + NL + "\t\t" + NL + "\t\tappPanel.contentPanel(" + NL + "\t\t\t\thtmlFactory.linkGroup()" + NL + "\t\t\t\t\t.item(\"Item 1\", \"#\", Style.DEFAULT, true)" + NL + "\t\t\t\t\t.item(\"Item 2\", \"#\", Style.DEFAULT, false)" + NL + "\t\t\t\t\t.item(\"Item 3\", \"#\", Style.SUCCESS, false))" + NL + "\t\t\t\t.width(DeviceSize.LARGE, 2).id(\"side-panel\");" + NL + "\t\t\t\t" + NL + "\t\tAccordion accordion = htmlFactory.accordion()" + NL + "\t\t\t\t.item(\"Item 1\", \"Item 1 body\")" + NL + "\t\t\t\t.item(\"Item 2\", Style.PRIMARY, \"Item 2 body\")" + NL + "\t\t\t\t.item(\"Item 3\", Style.WARNING, \"Item 3 body\")" + NL + "\t\t\t\t.style(Style.SUCCESS);" + NL + "\t\t" + NL + "\t\tFragment body = htmlFactory.fragment(accordion);" + NL + "\t\t" + NL + "\t\t// Button groups " + NL + "\t\tFragment buttonGroups = htmlFactory.fragment();" + NL + "\t\tbuttonGroups.content(" + NL + "\t\t\t\thtmlFactory.buttonGroup(" + NL + "\t\t\t\t\t\thtmlFactory.button(\"A\").on(Event.click, \"alert('Here we go!!!');\")," + NL + "\t\t\t\t\t\thtmlFactory.button(\"B\").style(Style.PRIMARY)," + NL + "\t\t\t\t\t\thtmlFactory.button(\"C\"))," + NL + "\t\t\t\t\"&nbsp;\"," + NL + "\t\t\t\thtmlFactory.buttonGroup(" + NL + "\t\t\t\t\t\thtmlFactory.button(\"A\").style(Style.WARNING)," + NL + "\t\t\t\t\t\thtmlFactory.button(\"B\").style(Style.INFO)," + NL + "\t\t\t\t\t\thtmlFactory.button(\"C\")).size(Size.LARGE)," + NL + "\t\t\t\t\"&nbsp;\"," + NL + "\t\t\t\thtmlFactory.buttonGroup(" + NL + "\t\t\t\t\t\thtmlFactory.button(\"A\")," + NL + "\t\t\t\t\t\thtmlFactory.button(\"B\").style(Style.PRIMARY)," + NL + "\t\t\t\t\t\thtmlFactory.button(\"C\")).vertical().size(Size.EXTRA_SMALL)," + NL + "\t\t\t\t\"&nbsp;\"," + NL + "\t\t\t\thtmlFactory.buttonGroup(" + NL + "\t\t\t\t\t\thtmlFactory.button(\"A\")," + NL + "\t\t\t\t\t\thtmlFactory.button(\"B\").style(Style.PRIMARY)," + NL + "\t\t\t\t\t\thtmlFactory.button(\"C\")" + NL + "\t\t\t\t\t\t\t.item(htmlFactory.link(\"#\", \"C1\"))" + NL + "\t\t\t\t\t\t\t.divider()" + NL + "\t\t\t\t\t\t\t.header(\"C2\")" + NL + "\t\t\t\t\t\t\t.item(htmlFactory.link(\"#\", \"C2.1\"))" + NL + "\t\t\t\t\t\t\t.item(htmlFactory.link(\"#\", \"C2.2\")))," + NL + "\t\t\t\t\"&nbsp;\"," + NL + "\t\t\t\thtmlFactory.buttonGroup(" + NL + "\t\t\t\t\t\thtmlFactory.button(\"A\")," + NL + "\t\t\t\t\t\thtmlFactory.button(\"B\").style(Style.PRIMARY)," + NL + "\t\t\t\t\t\thtmlFactory.button(\"C\")" + NL + "\t\t\t\t\t\t\t.item(htmlFactory.link(\"#\", \"C1\"))" + NL + "\t\t\t\t\t\t\t.divider()" + NL + "\t\t\t\t\t\t\t.header(\"C2\")" + NL + "\t\t\t\t\t\t\t.item(htmlFactory.link(\"#\", \"C2.1\"))" + NL + "\t\t\t\t\t\t\t.item(htmlFactory.link(\"#\", \"C2.2\"))).vertical()," + NL + "\t\t\t\t\"<HR/>\", " + NL + "\t\t\t\thtmlFactory.label(Style.SUCCESS, \"This is a button toolbar \", htmlFactory.glyphicon(Glyphicon.arrow_down))," + NL + "\t\t\t\t\"<P/>\"," + NL + "\t\t\t\thtmlFactory.buttonToolbar(" + NL + "\t\t\t\t\t\thtmlFactory.buttonGroup(" + NL + "\t\t\t\t\t\t\t\thtmlFactory.button(\"A\")," + NL + "\t\t\t\t\t\t\t\thtmlFactory.button(\"B\").style(Style.PRIMARY)," + NL + "\t\t\t\t\t\t\t\thtmlFactory.button(\"C\"))," + NL + "\t\t\t\t\t\thtmlFactory.buttonGroup(" + NL + "\t\t\t\t\t\t\t\thtmlFactory.button(\"X\")," + NL + "\t\t\t\t\t\t\t\thtmlFactory.button(\"Y\").style(Style.PRIMARY)," + NL + "\t\t\t\t\t\t\t\thtmlFactory.button(\"Z\")" + NL + "\t\t\t\t\t\t\t\t.item(htmlFactory.link(\"#\", \"Z1\"))" + NL + "\t\t\t\t\t\t\t\t.divider()" + NL + "\t\t\t\t\t\t\t\t.header(\"Z2\")" + NL + "\t\t\t\t\t\t\t\t.item(htmlFactory.link(\"#\", \"Z2.1\"))" + NL + "\t\t\t\t\t\t\t\t.item(htmlFactory.link(\"#\", \"Z2.2\")))" + NL + "\t\t\t\t\t\t)," + NL + "\t\t\t\t\"<HR/>\", " + NL + "\t\t\t\thtmlFactory.buttonGroup(" + NL + "\t\t\t\t\t\thtmlFactory.button(\"A\")," + NL + "\t\t\t\t\t\thtmlFactory.button(\"B\").style(Style.PRIMARY)," + NL + "\t\t\t\t\t\thtmlFactory.button(\"C\")" + NL + "\t\t\t\t\t\t\t.item(htmlFactory.link(\"#\", \"C1\"))" + NL + "\t\t\t\t\t\t\t.divider()" + NL + "\t\t\t\t\t\t\t.header(\"C2\")" + NL + "\t\t\t\t\t\t\t.item(htmlFactory.link(\"#\", \"C2.1\"))" + NL + "\t\t\t\t\t\t\t.item(htmlFactory.link(\"#\", \"C2.2\"))).justified()\t\t\t\t" + NL + "\t\t\t\t);" + NL + "\t\t" + NL + "\t\t" + NL + "\t\tbody.content(htmlFactory.panel(Style.INFO, \"Button Groups &amp; Toolbars\", buttonGroups, null).id(\"button-groups\"));" + NL + "\t\t" + NL + "\t\tappPanel.contentPanel(body).width(DeviceSize.LARGE, 10);" + NL + "\t\t" + NL + "\t\tbody.content(" + NL + "\t\t\t\thtmlFactory.panel(" + NL + "\t\t\t\t\t\tStyle.INFO, " + NL + "\t\t\t\t\t\tnull," + NL + "\t\t\t\t\t\tnew AutoCloseable() {" + NL + "\t\t\t\t\t\t\t" + NL + "\t\t\t\t\t\t\t@Override" + NL + "\t\t\t\t\t\t\tpublic String toString() {" + NL + "\t\t\t\t\t\t\t\t// Produce dynamic HTML" + NL + "\t\t\t\t\t\t\t\treturn htmlFactory" + NL + "\t\t\t\t\t\t\t\t\t\t.label(Style.SUCCESS, new Date())" + NL + "\t\t\t\t\t\t\t\t\t\t.toString();" + NL + "\t\t\t\t\t\t\t}" + NL + "\t\t\t\t\t\t\t" + NL + "\t\t\t\t\t\t\t@Override" + NL + "\t\t\t\t\t\t\tpublic void close() throws Exception {" + NL + "\t\t\t\t\t\t\t\t// Close resources;\t\t\t\t\t\t\t\t" + NL + "\t\t\t\t\t\t\t}" + NL + "\t\t\t\t\t\t}, \t\t\t\t\t\t " + NL + "\t\t\t\t\t\tnull));" + NL + "\t\t" + NL + "\t\tString themeName = ((HttpContext) context).getRequest().getParameter(\"theme\");" + NL + "\t\t" + NL + "\t\tfinal AutoCloseable app = " + NL + "\t\t\thtmlFactory.bootstrapRouterApplication(" + NL + "\t\t\t\tthemeName == null ? null : Theme.valueOf(themeName), " + NL + "\t\t\t\t\"My Application\", " + NL + "\t\t\t\tnull, " + NL + "\t\t\t\tnull, " + NL + "\t\t\t\tappPanel);" + NL + "\t\t" + NL + "\t\t// TODO Auto-generated method stub" + NL + "\t\treturn new Action() {" + NL + "\t\t\t" + NL + "\t\t\t@Override" + NL + "\t\t\tpublic void close() throws Exception {" + NL + "\t\t\t\tapp.close();\t\t\t\t" + NL + "\t\t\t}" + NL + "\t\t\t" + NL + "\t\t\t@Override" + NL + "\t\t\tpublic Object execute() throws Exception {" + NL + "\t\t\t\treturn app.toString();" + NL + "\t\t\t}" + NL + "\t\t};" + NL + "\t}" + NL + "" + NL + "\t@Override" + NL + "\tpublic boolean canExecute() {" + NL + "\t\treturn true;" + NL + "\t}" + NL + "" + NL + "\t@Override" + NL + "\tpublic void close() throws Exception {" + NL + "\t\t// NOP" + NL + "\t}" + NL + "" + NL + "}";
  protected final String TEXT_5 = NL;

public String generate(org.nasdanika.workspace.wizard.WorkspaceWizard wizard) throws Exception
  {
    final StringBuffer stringBuffer = new StringBuffer();
    stringBuffer.append(TEXT_1);
    stringBuffer.append(wizard.getApplicationArtifactId());
    stringBuffer.append(TEXT_2);
    stringBuffer.append(wizard.getJavaName());
    stringBuffer.append(TEXT_3);
    stringBuffer.append(wizard.getName());
    stringBuffer.append(TEXT_4);
    stringBuffer.append(TEXT_5);
    return stringBuffer.toString();
  }
}