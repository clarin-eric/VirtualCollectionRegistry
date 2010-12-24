(function($) {
	$.extend($.fn, {
		ajaxPopupMenu: function() {
			var o = {
				inSpeed: 250,
				outSpeed: 75
			};
			var trigger = $(this).children('div.popupTrigger');
			var menu = $(this).children('div.popupMenu');
			trigger.bind('click.ajaxPopupMenu', function(e) {
				e.stopPropagation();

				// hide any open context menus that may be showing
				$('div.popupMenu').fadeOut(o.outSpeed);

				// detect mouse position
				var d = {}, x, y;
				if (self.innerHeight) {
					d.pageYOffset = self.pageYOffset;
					d.pageXOffset = self.pageXOffset;
					d.innerHeight = self.innerHeight;
					d.innerWidth = self.innerWidth;
				} else if (document.documentElement
						&& document.documentElement.clientHeight) {
					d.pageYOffset = document.documentElement.scrollTop;
					d.pageXOffset = document.documentElement.scrollLeft;
					d.innerHeight = document.documentElement.clientHeight;
					d.innerWidth = document.documentElement.clientWidth;
				} else if (document.body) {
					d.pageYOffset = document.body.scrollTop;
					d.pageXOffset = document.body.scrollLeft;
					d.innerHeight = document.body.clientHeight;
					d.innerWidth = document.body.clientWidth;
				}
				(e.pageX) ? x = e.pageX : x = e.clientX + d.scrollLeft;
				(e.pageY) ? y = e.pageY : y = e.clientY + d.scrollTop;

				menu.css({
					top : y,
					left : x
				}).fadeIn(o.inSpeed);
				$(document).bind('keypress.ajaxPopupMenu', function(e) {
					if (e.keyCode == 27) {
						$(document).trigger('click');
					}
				});
				setTimeout(function() {
					$(document).click(function() {
						$(document).unbind('click.ajaxPopupMenu')
							.unbind('keypress.ajaxPopupMenu');
						menu.fadeOut(o.outSpeed);
					});
				}, 0);
				return false;
			});
			return $(this);
		}
	});
})(jQuery);
