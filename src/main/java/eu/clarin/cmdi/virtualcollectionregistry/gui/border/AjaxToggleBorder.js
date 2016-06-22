(function() {
	
    function elements(selector) {
        return Array.prototype.slice.call(document.querySelectorAll(selector));
    }
    
    function ajaxToggleBorder() {
        var headers =elements('div.toggleBorderHeader');
        headers.forEach(function(header) {
            header.onclick = function(e) {
                var content = e.srcElement.parentNode.parentNode.querySelector('div.toggleBorderContent');
                if(header.className === "toggleBorderHeader") {
                    header.className = "toggleBorderHeader collapsed";
                    content.style.display = 'none';
                } else {
                    header.className = "toggleBorderHeader";
                    content.style.display = 'block';
                }
            };
        });
    }
    
    document.addEventListener('DOMContentLoaded', function() {
        ajaxToggleBorder();
    }, false);
	
})();
