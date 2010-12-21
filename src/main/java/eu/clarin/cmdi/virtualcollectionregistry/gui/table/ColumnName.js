(function($) {
	$.extend($.fn, {
		detailsToggle : function() {
			var o = {
				speed: 250
			};
			var name = $(this).children('div.name');
			var details = $(this).children('div.details');
			name.click(function(e) {
				e.stopPropagation();
				details.slideToggle(o.speed, function() {
					name.toggleClass('detailsShown');
				});
				return false;
			});
			return $(this);
		}
	});
})(jQuery);
