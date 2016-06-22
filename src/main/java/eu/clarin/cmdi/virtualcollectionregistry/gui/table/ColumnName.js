(function() {
	
    function elements(selector) {
        return Array.prototype.slice.call(document.querySelectorAll(selector));
    }
    
    function ajaxToggle() {
        var headers =elements('div.name.collection');
        headers.forEach(function(header) {
            header.onclick = function(e) {
                
                var el = e.srcElement;
                var content = e.srcElement.parentNode.querySelector('div.details');
                if(el.className.indexOf('detailsShown') > 0) {
                    el.className = "name collection";
                    content.className = "details show";
                } else {
                    el.className = "name collection detailsShown";
                    content.className = "details hide";
                }
            };
        });
    }
    
    document.addEventListener('DOMContentLoaded', function() {
        ajaxToggle();
    }, false);
	
})();
