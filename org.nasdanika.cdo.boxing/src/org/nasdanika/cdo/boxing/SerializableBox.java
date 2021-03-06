/**
 */
package org.nasdanika.cdo.boxing;

import java.io.Serializable;

import org.nasdanika.core.Context;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Serializable Box</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.nasdanika.cdo.boxing.SerializableBox#getValue <em>Value</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.nasdanika.cdo.boxing.BoxingPackage#getSerializableBox()
 * @model superTypes="org.nasdanika.cdo.boxing.Box<org.nasdanika.cdo.boxing.Serializable, org.nasdanika.cdo.boxing.Context>"
 * @generated
 */
public interface SerializableBox extends Box<Serializable, Context> {
	/**
	 * Returns the value of the '<em><b>Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Value</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Value</em>' attribute.
	 * @see #setValue(byte[])
	 * @see org.nasdanika.cdo.boxing.BoxingPackage#getSerializableBox_Value()
	 * @model
	 * @generated
	 */
	byte[] getValue();

	/**
	 * Sets the value of the '{@link org.nasdanika.cdo.boxing.SerializableBox#getValue <em>Value</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Value</em>' attribute.
	 * @see #getValue()
	 * @generated
	 */
	void setValue(byte[] value);

} // SerializableBox
