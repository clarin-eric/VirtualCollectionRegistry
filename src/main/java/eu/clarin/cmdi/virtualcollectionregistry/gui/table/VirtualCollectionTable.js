/* 
 * Copyright (C) 2016 CLARIN
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
(function() {
	/*
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
                    content.className = "details hide col-xs-12";
                } else {
                    el.className = "name collection detailsShown";
                    content.className = "details show col-xs-12";
                }
            };
        });
    }
    
    document.addEventListener('DOMContentLoaded', function() {
        ajaxToggle();
    }, false);
    
    */
    $( document ).ready(function() {
        console.log("Registering jquery event handlers on collections table");
        $( "#collectionsTable" ).on( "click", "div.collection", function() {
            if( $(this).length <= 0) {
                return;
            }
            var elTitle = $( this )[0];
            var parent = $(elTitle).parent();
            var details = $(parent).find('div.details')[0];
            if (!details || details.length <= 0) {
                return;
            }
            
            //console.log(details);
            
            //var content = details[0];
            if(elTitle.className.indexOf('detailsShown') > 0) {
                elTitle.className = "name collection hover";
                details.className = "details hide col-xs-12";
            } else {
                elTitle.className = "name collection detailsShown hover";
                details.className = "details show col-xs-12";
            }
        });
        console.log( "ready!" );
    });

})();


