var spinner = spinner || {};

(function() {
	$.extend(spinner, {
		showSpinnerForOverlay : function() {
			if ($('#overlay').length === 0) {
				$('body').prepend('<div id="overlay"><div></div></div>');
			}

			var spinnerElement = $('#overlay');
			spinnerElement.stop().show();
		},

		show : function() {
			return spinner.showSpinnerForOverlay();
		},

		hide : function() {
			var spinnerElement = $('#overlay');
			spinnerElement.fadeOut(300);
		}

	})

})(jQuery, spinner);
