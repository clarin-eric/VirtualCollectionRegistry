(function($) {
	$.extend($.fn, {
		detailsToggle : function() {
			var o = {
				speed: 250
			};
			var name = $(this).children('div.name');
			var details = $(this).children('div.details');
			if (details.length > 0) {
				name.bind('click.detailsToggle', function() {
					details.slideToggle(o.speed, function() {
						name.toggleClass('detailsShown');
					});
				});
			} else {
				name.addClass('noDetails');
			}
			return $(this);
		}
	});
})(jQuery);
