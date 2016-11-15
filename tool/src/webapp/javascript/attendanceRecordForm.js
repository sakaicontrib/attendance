/*
 * *
 *  * Copyright (c) 2016, University of Dayton
 *  *
 *  * Licensed under the Educational Community License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *             http://opensource.org/licenses/ecl2
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

(function (attendance, $, undefined) {
    // Set the status class (which applies a background) if the input is selected
    function processInput (inputs) {
        for(var i = 0; i < inputs.length; i++) {
            if(inputs[i].checked) {
                var statusName = $(inputs[i]).siblings("label[hidden]").text();
                $(inputs[i]).parent().addClass(statusName);
            } else {
                $(inputs[i]).parent().removeClass().addClass("div-table-col");
            }
        }
    }

    // Re-setup a row after form input
    attendance.recordFormRowSetup = function(row) {
        var inputs = [];
        for(var i = 0; i < row.length; i++) {
            if(row[i].name === 'attendance-record-status-group') {
                inputs.push(row[i]);
            }
        }
        processInput(inputs);
    };

    // Set up the whole page record forms
    attendance.recordFormSetup = function() {
        var inputs = [];
        $("input[name='attendance-record-status-group']").each(function() {
            inputs.push(this);
        });

        processInput(inputs);
    };
}(window.attendance = window.attendance || {}, jQuery ));
