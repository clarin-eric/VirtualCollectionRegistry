(function($) {
	$.extend($.fn, {
		ajaxToggleBorder : function() {
			var o = {
				speed: 250
			};
			var header = $(this).children('div.toggleBorderHeader');
			var content = $(this).children('div.toggleBorderContent');
			header.bind('click.ajaxToggleBorder', function() {
				content.slideToggle(o.speed, function() {
					header.toggleClass('collapsed');
				});
			});
			return $(this);
		}
	});
})(jQuery);
