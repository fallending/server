/**
 */
package org.nasdanika.cdo.security.impl;

import java.lang.reflect.InvocationTargetException;

import java.util.Map;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;

import org.eclipse.emf.internal.cdo.CDOObjectImpl;

import org.nasdanika.cdo.security.Group;
import org.nasdanika.cdo.security.Guest;
import org.nasdanika.cdo.security.Permission;
import org.nasdanika.cdo.security.PrincipalVisitor;
import org.nasdanika.cdo.security.Realm;
import org.nasdanika.cdo.security.SecurityPackage;

import org.nasdanika.core.AuthorizationProvider.AccessDecision;

import org.nasdanika.core.Context;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Guest</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.nasdanika.cdo.security.impl.GuestImpl#getMemberOf <em>Member Of</em>}</li>
 *   <li>{@link org.nasdanika.cdo.security.impl.GuestImpl#getPermissions <em>Permissions</em>}</li>
 *   <li>{@link org.nasdanika.cdo.security.impl.GuestImpl#isDisabled <em>Disabled</em>}</li>
 * </ul>
 *
 * @generated
 */
public class GuestImpl extends CDOObjectImpl implements Guest {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected GuestImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return SecurityPackage.Literals.GUEST;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected int eStaticFeatureCount() {
		return 0;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	public EList<Group> getMemberOf() {
		return (EList<Group>)eGet(SecurityPackage.Literals.PRINCIPAL__MEMBER_OF, true);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	public EList<Permission> getPermissions() {
		return (EList<Permission>)eGet(SecurityPackage.Literals.PRINCIPAL__PERMISSIONS, true);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isDisabled() {
		return (Boolean)eGet(SecurityPackage.Literals.PRINCIPAL__DISABLED, true);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setDisabled(boolean newDisabled) {
		eSet(SecurityPackage.Literals.PRINCIPAL__DISABLED, newDisabled);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public AccessDecision authorize(Context context, EObject target, String action, String qualifier, Map<String, Object> environment) {
		// TODO: implement this method
		// Ensure that you remove @generated or mark it @generated NOT
		throw new UnsupportedOperationException();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void accept(PrincipalVisitor visitor) {
		// TODO: implement this method
		// Ensure that you remove @generated or mark it @generated NOT
		throw new UnsupportedOperationException();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Realm<?> getRealm() {
		// TODO: implement this method
		// Ensure that you remove @generated or mark it @generated NOT
		throw new UnsupportedOperationException();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	@SuppressWarnings("unchecked")
	public Object eInvoke(int operationID, EList<?> arguments) throws InvocationTargetException {
		switch (operationID) {
			case SecurityPackage.GUEST___AUTHORIZE__CONTEXT_EOBJECT_STRING_STRING_MAP:
				return authorize((Context)arguments.get(0), (EObject)arguments.get(1), (String)arguments.get(2), (String)arguments.get(3), (Map<String, Object>)arguments.get(4));
			case SecurityPackage.GUEST___ACCEPT__PRINCIPALVISITOR:
				accept((PrincipalVisitor)arguments.get(0));
				return null;
			case SecurityPackage.GUEST___GET_REALM:
				return getRealm();
		}
		return super.eInvoke(operationID, arguments);
	}

} //GuestImpl
