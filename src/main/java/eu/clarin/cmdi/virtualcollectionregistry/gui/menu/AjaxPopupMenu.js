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
	
    function elements(selector) {
        return Array.prototype.slice.call(document.querySelectorAll(selector));
    }
    
    function hideAllPopups() {
        elements('div.popupMenu').forEach(function(el) {
            el.style.display = 'none';
        });
    }

    function showPopup(e) {
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

        var el = e.srcElement.parentNode.querySelector('div.popupMenu');
        el.style.top = y;
        el.style.left = x;
        el.style.display = 'block';   
    }
        
    function ajaxPopupMenu() {
        var o = {
                inSpeed: 250,
                outSpeed: 75
        };
        var triggers = elements('div.popupTrigger');
        triggers.forEach(function(trigger) {
            trigger.onclick = function(e) {
                e.stopPropagation();
                hideAllPopups();
                showPopup(e);
                return false;
            };
        });
    }
    
    document.addEventListener('click', function() {
        hideAllPopups();
    }, false);
    
    document.addEventListener('DOMContentLoaded', function() {
        ajaxPopupMenu();
    }, false);
    
})();


