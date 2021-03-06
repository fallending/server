
// Generic view model interacting with the server side over AJAX
// Tokens 
// 		- app-id - application id, base for other ID's like modal, form, and overlay.
//		- url - server endpoint communication url.
//		- declarations - observables and other declarations
//		- success-handler - invoked on AJAX success
//		- error-handler - invoked on AJAX error
//		- ajax-config - additional configuration for jQuery.ajax, e.g. method. Shall end with a comma, may be blank
$(function() {
	var container = document.getElementById("{{app-id}}-modal");
	if (container) {
		var ViewModel = function() {	
			// Observables with inital values for edits etc.
			{{declarations}}
			
			var overlay = jQuery("#{{app-id}}-overlay");
				
			this.submit = function() {
				overlay.height(overlay.parent().height());
				overlay.width(overlay.parent().width());
				overlay.show();
				
				jQuery.ajax("{{url}}", 
						{
							{{ajax-config}}
							
							success: function(data) {
								{{success-handler}}
								overlay.hide();
							}.bind(this),
							
							error: function(jqXHR, textStatus, errorThrown) {
								{{error-handler}}
								overlay.hide();
							}.bind(this)
						});
			};
			
		};
		
		ko.applyBindings(new ViewModel(), container);
		
		// Modal resizing on shown
	    $('#{{app-id}}-modal').on('shown.bs.modal', function() {
	    	var modalBody = $(this).find('.modal-body');
	    	var form = modalBody.find('form');
	    	form.width((modalBody.width()-30)+"px");
	    	modalBody.height((form.height() + 30) + "px"); // 30 pixels for dynamic wysiwyg toolbar, just in case.
	    	
	    	// Hide overlay if it remained open due to an error.
	    	$('#{{app-id}}-overlay').hide();
	    });	    
	} else {
		console.error("Application container '{{app-id}}'-modal not found");
	}
	
});
	
	