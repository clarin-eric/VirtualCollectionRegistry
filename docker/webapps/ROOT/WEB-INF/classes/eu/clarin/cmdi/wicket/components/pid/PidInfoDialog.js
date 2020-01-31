/* 
 * Copyright (C) 2018 CLARIN
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

/**
 * show a 'copied' tooltip on the button for a second.
 * 
 * @param {type} button
 * @returns {undefined}
 */ 
var showCopiedTooltip = function (button) {    
    var title = button.attr('title');

    button.attr('title', 'Copied!');
    button.tooltip({
        "placement": "bottom",
        "trigger": "manual"
    });
    button.on('shown.bs.tooltip', function (e) {
        //remove tooltip
        setTimeout(function () {
            button.attr('title', title);
            button.tooltip('destroy');
        }, 1000);
    });
    button.tooltip('show');

    //flash button
    button.toggleClass('btn-info');
    setTimeout(function () {
        button.toggleClass('btn-info');
    }, 500);
};

/**
 * Initialize the copy to clipboard feature after the DOM has been fully
 * loaded.
 * 
 * @type type
 */
$( document ).ready(function() {
    var clipboard = new ClipboardJS('.btn.clipboard');
    clipboard.on('success', function(e) {
        var cbBtn = $(e.trigger);
        var dropDownParent = cbBtn.closest('.dropdown');
        if (dropDownParent && dropDownParent.length > 0) {
            //feedback on the dropdown trigger
            showCopiedTooltip(dropDownParent.find('.btn[data-toggle=dropdown]'));
        } else {
            //feedback on the button
            showCopiedTooltip($(e.trigger));
        }
        e.clearSelection();
    });
});