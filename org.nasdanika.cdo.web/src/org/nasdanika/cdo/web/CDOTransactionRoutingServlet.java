package org.nasdanika.cdo.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.emf.cdo.CDOObject;
import org.eclipse.emf.cdo.common.id.CDOID;
import org.eclipse.emf.cdo.transaction.CDOTransaction;
import org.nasdanika.cdo.CDOTransactionContext;
import org.nasdanika.cdo.CDOTransactionContextFilter;
import org.nasdanika.cdo.CDOTransactionContextProvider;
import org.nasdanika.cdo.CDOViewContextProvider;
import org.nasdanika.cdo.CDOViewContextSubject;
import org.nasdanika.core.Context;
import org.nasdanika.web.HttpServletRequestContext;

@SuppressWarnings("serial")
public class CDOTransactionRoutingServlet<CR> extends CDOViewRoutingServletBase<CDOTransaction, CR, CDOTransactionContext<CR>> {
	
	@SuppressWarnings("rawtypes")
	@Override
	protected Class<? extends CDOViewContextProvider> getProviderType() {
		return CDOTransactionContextProvider.class;
	}

	@Override
	protected HttpServletRequestContext createCompositeContext(
			String[] path,
			HttpServletRequest req, 
			HttpServletResponse resp, 
			String reqUrl,
			CDOTransactionContext<CR> transactionContext,
			Context[] chain) throws Exception {
		
		return new CDOTransactionHttpServletRequestContextImpl<CR>(
				path, 
				null, 
				extensionManager, 
				classLoadingContext,
				null,
				req, 
				resp,
				reqUrl, 
				null,
				chain,
				transactionContext);	
	}

	@Override
	protected CDOTransactionWebSocketContext<CR> createWebSocketContext(CDOViewContextSubject<CDOTransaction, CR> subject, final CDOID targetID) {
		CDOViewContextProvider<CDOTransaction, CR, CDOTransactionContext<CR>> provider = cdoViewContextProviderServiceTracker.getService();
		if (provider==null) {
			throw new IllegalStateException("View provider not found");
		}
		
		class CDOTransactionWebSocketContextImpl extends CDOTransactionContextFilter<CR> implements CDOTransactionContext<CR>, CDOTransactionWebSocketContext<CR> {

			public CDOTransactionWebSocketContextImpl(CDOTransactionContext<CR> target) {
				super(target);
			}

			@Override
			public CDOObject getTargetObject() {
				if (targetID == null) {
					return null;
				}
				return getView().getObject(targetID);
			}
			
		}
	
		return new CDOTransactionWebSocketContextImpl(provider.createContext(subject));
	}
			
}
