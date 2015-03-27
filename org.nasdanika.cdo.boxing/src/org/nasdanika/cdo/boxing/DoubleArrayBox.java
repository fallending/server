/**
 */
package org.nasdanika.cdo.boxing;

import org.eclipse.emf.common.util.EList;
import org.nasdanika.core.Context;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Double Array Box</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.nasdanika.cdo.boxing.DoubleArrayBox#getValue <em>Value</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.nasdanika.cdo.boxing.BoxingPackage#getDoubleArrayBox()
 * @model superTypes="org.nasdanika.cdo.boxing.Box<org.eclipse.emf.ecore.EJavaObject, org.nasdanika.cdo.boxing.Context>"
 * @generated
 */
public interface DoubleArrayBox extends Box<Object, Context> {
	/**
	 * Returns the value of the '<em><b>Value</b></em>' attribute list.
	 * The list contents are of type {@link java.lang.Double}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Value</em>' attribute list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Value</em>' attribute list.
	 * @see org.nasdanika.cdo.boxing.BoxingPackage#getDoubleArrayBox_Value()
	 * @model
	 * @generated
	 */
	EList<Double> getValue();

} // DoubleArrayBox
