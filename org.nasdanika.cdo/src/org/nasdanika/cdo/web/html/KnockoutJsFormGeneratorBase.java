package org.nasdanika.cdo.web.html;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EModelElement;
import org.eclipse.emf.ecore.ETypedElement;
import org.nasdanika.cdo.web.routes.CDOWebUtil;
import org.nasdanika.html.Form;
import org.nasdanika.html.FormGroup;
import org.nasdanika.html.FormInputGroup;
import org.nasdanika.html.HTMLFactory;
import org.nasdanika.html.InputBase;
import org.nasdanika.html.UIElement;

/**
 * Adds KnockoutJs bindings, uses help text to display validation errors, adds form validation error text on the top of the form. 
 * @author Pavel
 *
 * @param <T>
 */
public abstract class KnockoutJsFormGeneratorBase<S extends EModelElement, T extends ETypedElement> extends FormGeneratorBase<T> {

	private String model;
	private String handler;
	private S source;
	
	/**
	 * Source of generation metadata
	 * @return
	 */
	public S getSource() {
		return source;
	}

	/**
	 * 
	 * @param model Model expression. The model object shall contain data object to which form controls are bound, validationResults object
	 * which holds validation messages for controls, and validationResult string with form validation result.
	 * @param handler Form submit handler expression
	 */
	protected KnockoutJsFormGeneratorBase(S source, String model, String handler) {
		this.source = source;
		this.model = model;
		this.handler = handler;
	}
	
	@Override
	public Form generateForm(HTMLFactory htmlFactory) throws Exception {		
		Form form = super.generateForm(htmlFactory);
		form.koDataBind("submit", handler);
		return form;
	}
	
	/**
	 * Adds DIV for form validation message.
	 */
	@Override
	protected void populateForm(HTMLFactory htmlFactory, Form form)	throws Exception {
		form.content(htmlFactory.div("").style("color", "red").koDataBind("text", model+".validationResults['"+CDOWebUtil.getThisKey(getSource())+"']"));		
	}
	
	@Override
	protected Object generateHelpText(HTMLFactory htmlFactory, T element) {
		return htmlFactory.span().style("color", "red").koDataBind("text", model+".validationResults."+element.getName());
	}

	@Override
	protected void configureGroup(HTMLFactory htmlFactory, T element, Object group) {
		super.configureGroup(htmlFactory, element, group);
		if (group instanceof FormGroup) { 
			FormGroup<?> formGroup = (FormGroup<?>) group;
			if (!(group instanceof FormInputGroup)) {
				formGroup.feedback();
			}
			formGroup.koDataBind("css", "{ 'has-error' : "+model+".validationResults."+element.getName()+" }");
		}
	}
	
	@Override
	protected void configureControl(HTMLFactory htmlFactory, T element,	Object control) {		
		super.configureControl(htmlFactory, element, control);
		if (control instanceof InputBase) {
			((InputBase<?>) control).koDataBind(isCheckbox(element, control) ? "checked" : "value", model+".data."+element.getName());
		} else if (control instanceof UIElement) {
			((UIElement<?>) control).koDataBind("text", model+".data."+element.getName());
		}
	}
	
	private static KnockoutJsModelGenerator MODEL_GENERATOR = new KnockoutJsModelGenerator();
	
	/**
	 * Generates model object with asynchronous validation function.
	 * @return
	 * @throws Exception 
	 */
	public String generateModel() throws Exception {
		String customDeclarations = "";
		EAnnotation formAnnotation = source.getEAnnotation(FORM_ANNOTATION_SOURCE);
		if (formAnnotation!=null && formAnnotation.getDetails().containsKey(MODEL_KEY)) {
			customDeclarations = formAnnotation.getDetails().get(MODEL_KEY);
		}

		return MODEL_GENERATOR.generate(generateModelEntries(), model, generateLoadModel(), generateApply(), customDeclarations);
	}
	
	protected abstract String generateApply() throws Exception;
	
	protected String generateLoadModel() throws Exception {
		return "";
	}
	
	/**
	 * 
	 * @return [ entry name, default value, validator ]. First entry is for the form (this).
	 */
	protected List<String[]> generateModelEntries() {
		List<String[]> ret = new ArrayList<>();
		String validator = null;
		EAnnotation formAnnotation = getSource().getEAnnotation(FORM_ANNOTATION_SOURCE);
		if (formAnnotation!=null && formAnnotation.getDetails().containsKey(VALIDATOR_KEY)) {
			validator =  formAnnotation.getDetails().get(VALIDATOR_KEY);
		}

		ret.add(new String[] { CDOWebUtil.getThisKey(source), null, validator});
		return ret;
	}
}
