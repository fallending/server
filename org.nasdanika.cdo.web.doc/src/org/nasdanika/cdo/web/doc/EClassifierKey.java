package org.nasdanika.cdo.web.doc;

import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EClassifier;

public class EClassifierKey implements Comparable<EClassifierKey> {
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((nsURI == null) ? 0 : nsURI.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EClassifierKey other = (EClassifierKey) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (nsURI == null) {
			if (other.nsURI != null)
				return false;
		} else if (!nsURI.equals(other.nsURI))
			return false;
		return true;
	}

	public String getNsURI() {
		return nsURI;
	}

	public String getName() {
		return name;
	}
	
	public String getDocumentation() {
		return documentation;
	}

	String nsURI;
	String name;
	String documentation;
	
	public EClassifierKey(EClassifier eClassifier) {
		nsURI = eClassifier.getEPackage().getNsURI();
		name = eClassifier.getName();
		EAnnotation docAnn = eClassifier.getEAnnotation(EModelElementDocumentationGenerator.ECORE_DOC_ANNOTATION_SOURCE);
		if (docAnn==null) {
			documentation = "";
		} else {
			documentation = docAnn.getDetails().get("documentation");
		}
	}

	@Override
	public int compareTo(EClassifierKey o) {
		if (o==this) {
			return 0;
		}
		
		if (o.nsURI.equals(nsURI)) {
			return name.compareTo(o.getName());
		}
		
		return nsURI.compareTo(o.nsURI);
	}
	
}