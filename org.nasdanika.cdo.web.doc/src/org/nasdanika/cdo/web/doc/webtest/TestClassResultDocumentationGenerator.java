package org.nasdanika.cdo.web.doc.webtest;

import java.net.URI;
import java.text.MessageFormat;
import java.util.Collection;

import org.apache.commons.lang3.StringEscapeUtils;
import org.eclipse.emf.common.util.ECollections;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.EMap;
import org.eclipse.emf.ecore.EObject;
import org.nasdanika.cdo.web.doc.DocRoute;
import org.nasdanika.cdo.web.doc.DocumentationGenerator;
import org.nasdanika.cdo.web.doc.TocNode;
import org.nasdanika.html.Bootstrap;
import org.nasdanika.html.Bootstrap.Glyphicon;
import org.nasdanika.html.Fragment;
import org.nasdanika.html.HTMLFactory;
import org.nasdanika.html.RowContainer;
import org.nasdanika.html.RowContainer.Row;
import org.nasdanika.html.RowContainer.Row.Cell;
import org.nasdanika.html.Table;
import org.nasdanika.html.Tag;
import org.nasdanika.web.HttpServletRequestContext;
import org.nasdanika.webtest.model.Descriptor;
import org.nasdanika.webtest.model.OperationStatus;
import org.nasdanika.webtest.model.ParameterizedTestResult;
import org.nasdanika.webtest.model.TestClassResult;
import org.nasdanika.webtest.model.TestMethodResult;

class TestClassResultDocumentationGenerator extends TestResultDocumentationGenerator<TestClassResult> {

	protected TestClassResultDocumentationGenerator(TestResultsDocumentationGenerator testResultsDocumentationGenerator) {
		super(testResultsDocumentationGenerator);
	}

	@Override
	public void createToc(TestClassResult obj, TocNode parent) {
		if (!obj.getMethodResults().isEmpty()) {
			super.createToc(obj, parent);
		}
	}
	
	@Override
	protected String getIcon(TestClassResult descriptor) {
		if (descriptor.eContainer() instanceof ParameterizedTestResult && !descriptor.getMethodResults().get(0).getParameters().isEmpty()) {		
			return "/bundle/org.nasdanika.icons/fatcow-hosting-icons/FatCow_Icons16x16/wrench.png";
		}
		return super.getIcon(descriptor);
	}
	
	@Override
	protected String getTitle(TestClassResult obj) {
		EList<String> parameters = obj.getMethodResults().get(0).getParameters();
		if (obj.eContainer() instanceof ParameterizedTestResult && !parameters.isEmpty()) {
			String ret = null;
			for (String prm: parameters) {
				if (prm != null) {
					ret = prm;
				}
			}
			return ret;
		}
		return super.getTitle(obj);
	}		
	
	@Override
	protected Fragment getIndex(TestClassResult obj, HttpServletRequestContext context, java.net.URI baseURI, String urlPrefix, String path) throws Exception {
		HTMLFactory htmlFactory = testResultsDocumentationGenerator.getDocRoute().getHtmlFactory();		
		EList<String> parameters = obj.getMethodResults().isEmpty() ? ECollections.emptyEList() : obj.getMethodResults().get(0).getParameters();
		Fragment ret = super.getIndex(obj, context, baseURI, urlPrefix, path);
		if (obj.eContainer() instanceof ParameterizedTestResult && !parameters.isEmpty()) {
			
			ret.content(htmlFactory.tag(Tag.TagName.h4, "Parameters"));
			Table parametersTable = htmlFactory.table().bordered();
			ret.content(parametersTable);
			parametersTable.header().headerRow("Title", "Value", "Description", "Field", "Type").style(Bootstrap.Style.INFO);
			RowContainer<?> parametersTableBody = parametersTable.body();
			EList<Descriptor> parameterDescriptors = ((ParameterizedTestResult) obj.eContainer()).getParameterDescriptors();
			for (int i=0; i<parameterDescriptors.size(); ++i) {
				Descriptor pd = parameterDescriptors.get(i);
				Row pRow = parametersTableBody.row(pd.getTitle());
				pRow.cell(StringEscapeUtils.escapeHtml4(parameters.get(i)));
				description(pd, pRow.cell(), context, baseURI, urlPrefix);
				String pqn = pd.getQualifiedName();
				int cIdx = pqn.indexOf(":");
				int lsIdx = pqn.substring(cIdx+1).lastIndexOf(' ');
				pRow.cell(pqn.substring(0, cIdx));
				String className = pqn.substring(lsIdx+cIdx+2);
				pRow.cell(testResultsDocumentationGenerator.getDocRoute().javaDocLink(className, false, false));
			}
			
			ret.content(htmlFactory.tag(Tag.TagName.h4, "Test methods"));
			methodResults(obj, context, baseURI, urlPrefix, htmlFactory, ret);
						
			return ret;
		}
				
		methodResults(obj, context, baseURI, urlPrefix, htmlFactory, ret);
		return ret;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void methodResults(
			TestClassResult obj, 
			HttpServletRequestContext context, 
			URI baseURI, 
			String urlPrefix,
			HTMLFactory htmlFactory, 
			Fragment ret) throws Exception {
		EMap<OperationStatus, Integer> suiteStats = testResultStats(obj);						
		statsChart(suiteStats, ret);	
	
		Table methodTable = htmlFactory.table().bordered();
		ret.content(methodTable);
		Row headerRow = methodTable.header().row().style(Bootstrap.Style.INFO);
		headerRow.header(htmlFactory.glyphicon(Glyphicon.cog), " Method");
		headerRow.header(htmlFactory.glyphicon(Glyphicon.file), " Description");
		headerRow.header(htmlFactory.glyphicon(Glyphicon.time), " Duration");
		for (TestMethodResult tmr: obj.getMethodResults()) {
			Row row = methodTable.body().row();
			switch (tmr.getStatus()) {
			case ERROR:
				row.style(Bootstrap.Style.WARNING);
				break;
			case FAIL:
				row.style(Bootstrap.Style.DANGER);
				break;
			case PASS:
				row.style(Bootstrap.Style.SUCCESS);
				break;
			case PENDING:
				row.style(Bootstrap.Style.DEFAULT);
				break;
			default:
				break;				
			}
			String objectPath = testResultsDocumentationGenerator.getObjectPath(tmr);
			DocumentationGenerator<Object> docGen = testResultsDocumentationGenerator.getDocumentationGenerator(tmr.eClass());
			String title = docGen instanceof DescriptorDocumentationGenerator ? ((DescriptorDocumentationGenerator) docGen).getTitle(tmr) : tmr.getTitle(); 
			String href = DocRoute.ROUTER_DOC_CONTENT_FRAGMENT_PREFIX+testResultsDocumentationGenerator.getDocRoute().getDocRoutePath()+objectPath+"/index.html";
			row.cell(htmlFactory.link(href, operationStatusGlyph(tmr.getStatus()), "&nbsp;", StringEscapeUtils.escapeHtml4(title)));				
			Cell descriptionCell = row.cell();
			if (docGen instanceof DescriptorDocumentationGenerator) {
				((DescriptorDocumentationGenerator) docGen).description(tmr, descriptionCell, context, baseURI, urlPrefix);
			}
			
			long duration = tmr.getFinish() - tmr.getStart();
			if (duration<1000) {
				row.cell(duration, " ms");
			} else {
				row.cell(MessageFormat.format("{0,number,#.###} sec", new Object[] {duration/1000.0}));
			}				
		}
	}
		
	@Override
	protected Collection<? extends EObject> getTocChildren(TestClassResult testResult) {
		return testResult.getMethodResults();
	}

}
