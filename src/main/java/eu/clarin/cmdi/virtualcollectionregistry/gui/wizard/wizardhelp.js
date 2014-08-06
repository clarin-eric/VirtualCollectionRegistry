/* 
 * Copyright (C) 2014 CLARIN
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

$(document).ready(function() {
    /* activate tooltip on annotated labels */
    $(".tooltipparent").each(function() {
        var tooltip = '#' + $(this).attr('id') + ' .tooltip';
        $(this).qtip({
            content: $(tooltip),
            show: 'mouseover',
            hide: 'mouseout'
        });
        $(".tooltip", this).hide();
    });

    /* toggle extra help information*/
    $(".extrainfotoggle").click(function(event) {
        event.preventDefault();
        $(this).parent(".extrainfo").children(".notes").slideToggle();
    });
    $(".extrainfo .notes").hide();
    $(".extrainfotoggle").qtip({
        content: 'Help',
        show: 'mouseover',
        hide: 'mouseout'
    });
});
