/*
 * Copyright (C) 2016 Johannes C. Schneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 3 of 
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
'use strict';

//also have a look at init.js, where some init data is made available

var DATE_FORMAT = "YYYY-MM-DD HH:mm:ss";

var convertToDateString = function (timestamp) {
    return moment(timestamp).format(DATE_FORMAT);
};

var convertToTimestamp = function (dateString) {
    return moment(dateString, DATE_FORMAT).valueOf();
};

var SwitchComponent = Vue.extend({
    methods: {
        initData: function (tab) {
            var self = this;

            $.ajax({
                url: 'api/switches?displayTab=' + tab,
                type: 'GET',
                dataType: 'json',
                async: true,
                success: function (data) {
                    self.switches = data;
                }
            });
        },
        turnOn: function (myswitch) {
            $.getJSON('api/switches/' + myswitch.group + '/' + myswitch.code + '/on');
        },
        turnOff: function (myswitch) {
            $.getJSON('api/switches/' + myswitch.group + '/' + myswitch.code + '/off');
        }
    }
});

var switchesA = new SwitchComponent({
    el: '#switchesA',
    data: {
        switches: []
    }
});
switchesA.initData('A');

var switchesB = new SwitchComponent({
    el: '#switchesB',
    data: {
        switches: []
    }
});
switchesB.initData('B');

var combos = new Vue({
    el: '#combos',
    data: {
        combos: []
    },
    methods: {
        initData: function () {
            var self = this;

            $.ajax({
                url: 'api/combos',
                type: 'GET',
                dataType: 'json',
                async: true,
                success: function (data) {
                    self.combos = data;
                }
            });
        },
        fire: function (combo) {
            $.getJSON('api/combos/' + combo);
        }
    }
});
combos.initData();

var eventsTable = new Vue({
    el: '#eventsTable',
    data: {
        events: []
    },
    methods: {
        refreshData: function () {
            var self = this;

            $.ajax({
                url: 'api/events',
                type: 'GET',
                dataType: 'json',
                async: true,
                success: function (data) {
                    self.events = data;
                }
            });
        },
        remove: function (event) {
            var self = this;

            $.ajax({
                url: 'api/events/' + event.id,
                type: 'DELETE',
                dataType: 'json',
                async: true,
                success: function (data) {
                    self.refreshData();
                }
            });
        },
        update: function (event) {
            editEvent.initFrom(event);
        },
        toDateString: function (timestamp) {
            return convertToDateString(timestamp);
        }
    }
});

var addEvent = new Vue({
    el: '#addModal',
    data: {
        executionString: convertToDateString(new Date().getTime()),
        name: "",
        switchName: "",
        switchPosition: true,
        repeating: false,
        switchNames: switchNames
    },
    methods: {
        initData: function () {
            var self = this;
            self.switchName = self.switchNames[0];
        },
        submit: function () {
            var self = this;

            $.ajax({
                url: 'api/events',
                type: 'POST',
                data: JSON.stringify({
                    executionTime: convertToTimestamp(self.executionString),
                    name: self.name,
                    switchName: self.switchName,
                    switchPosition: self.switchPosition,
                    repeating: self.repeating
                }),
                dataType: 'json',
                contentType: "application/json; charset=utf-8",
                processData: false,
                async: true,
                success: function (data) {

                }
            });
        }
    }
});
addEvent.initData();

var editEvent = new Vue({
    el: '#editModal',
    data: {
        id: "",
        executionString: "",
        name: "",
        switchName: "",
        switchPosition: true,
        repeating: false,
        switchNames: switchNames
    },
    methods: {
        initFrom: function (event) {
            var executionString = convertToDateString(event.executionTime);
            this.id = event.id;
            this.executionString = executionString;
            this.name = event.name;
            this.switchName = event.switchName;
            this.switchPosition = event.switchPosition;
            this.repeating = event.repeating;
        },
        submit: function () {
            var self = this;

            $.ajax({
                url: 'api/events',
                type: 'PUT',
                data: JSON.stringify({
                    id: self.id,
                    executionTime: convertToTimestamp(self.executionString),
                    name: self.name,
                    switchName: self.switchName,
                    switchPosition: self.switchPosition,
                    repeating: self.repeating
                }),
                dataType: 'json',
                contentType: "application/json; charset=utf-8",
                processData: false,
                async: true,
                success: function (data) {

                }
            });
        }
    }
});

var refreshTableAfterShortDelay = function () {
    setTimeout(eventsTable.refreshData, 100);
};

$(document).ready(function () {

    //init materialize js components
    $('.modal').modal();
    //does not work: selected value is not updated in vue model
    //$('select').material_select();

    $('#eventTableTab').on('click', function () {
        eventsTable.refreshData();
    });
    $('#buttonCreateEvent').on('click', function () {
        refreshTableAfterShortDelay();
    });
    $('#buttonEditEvent').on('click', function () {
        refreshTableAfterShortDelay();
    });

});
