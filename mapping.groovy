/**
 *  Traffic Report
 *
 *  Copyright 2014 Brian Critchlow
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *
 *  This SmartApp has the intention of notifying you of traffic conditions on your Hue bulbs and alerting you of departure time
 *  based on that traffic. The app will request two locations, the expected time of arrival, and when to start polling for traffic.
 *  It will also allow you to set the thresholds for traffic and what colors to change the Hue to.
 *
 *
 *
 *
 *  if realTime > time
 *  if (arrivalTime - realTime) >= now
 */

definition(
    name: "Traffic Report",
    namespace: "docwisdom",
    author: "Brian Critchlow",
    description: "notifies of traffic conditions by Hue color and flashes when you should leave based on set arrival time.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/docwisdom-smartthings/Transport-traffic-jam-icon.png",
    iconX2Url: "https://s3.amazonaws.com/docwisdom-smartthings/Transport-traffic-jam-icon.png")


preferences {
	//what is the departure location?
	section("Departing From:"){
		input "from", "text", title: "Address?"
	}
    //what is the destination location?
	section("Arriving At:"){
		input "to", "text", title: "Address?"
	}
    //what time do you need to arrive?
	section("Expected Arrival Time:"){
		input "arrivalTime", "time", title: "When?"
	}
    // //what time should I begin checking traffic?
	section("Begin Checking At:"){
		input "checkTime", "time", title: "When?"
	}
    //which hue bulbs to control?
    section("Control these bulbs...") {
		input "hues", "capability.colorControl", title: "Which Hue Bulbs?", required:true, multiple:true
	}
    //color for no traffic
	section("Color For No Traffic:"){
		input "color1", "enum", title: "Hue Color?", required: false, multiple:false, options: [
					["Soft White":"Soft White - Default"],
					["White":"White - Concentrate"],
					["Daylight":"Daylight - Energize"],
					["Warm White":"Warm White - Relax"],
					"Red","Green","Blue","Yellow","Orange","Purple","Pink"]
		//input "lightLevel1", "enum", title: "Light Level?", required: false, options: [[10:"10%"],[20:"20%"],[30:"30%"],[40:"40%"],[50:"50%"],[60:"60%"],[70:"70%"],[80:"80%"],[90:"90%"],[100:"100%"]]
	}
    //some traffic threshold in minutes
	section("Some Traffic Adds This Many Minutes To Commute:") {
		input "threshold1", "number", title: "Minutes?"
	}
    //color for some traffic
    section("Color For Some Traffic:"){
		input "color2", "enum", title: "Hue Color?", required: false, multiple:false, options: [
					["Soft White":"Soft White - Default"],
					["White":"White - Concentrate"],
					["Daylight":"Daylight - Energize"],
					["Warm White":"Warm White - Relax"],
					"Red","Green","Blue","Yellow","Orange","Purple","Pink"]
		//input "lightLevel2", "enum", title: "Light Level?", required: false, options: [[10:"10%"],[20:"20%"],[30:"30%"],[40:"40%"],[50:"50%"],[60:"60%"],[70:"70%"],[80:"80%"],[90:"90%"],[100:"100%"]]
	}
    //bad traffic threshold in minutes
	section("Bad Traffic Adds This Many Minutes To Commute:") {
		input "threshold1", "number", title: "Minutes?"
	}
    //color for bad traffic
    section("Color For Bad Traffic:"){
		input "color1", "enum", title: "Hue Color?", required: false, multiple:false, options: [
					["Soft White":"Soft White - Default"],
					["White":"White - Concentrate"],
					["Daylight":"Daylight - Energize"],
					["Warm White":"Warm White - Relax"],
					"Red","Green","Blue","Yellow","Orange","Purple","Pink"]
		//input "lightLevel3", "enum", title: "Light Level?", required: false, options: [[10:"10%"],[20:"20%"],[30:"30%"],[40:"40%"],[50:"50%"],[60:"60%"],[70:"70%"],[80:"80%"],[90:"90%"],[100:"100%"]]
	}
}


def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	initialize()
}

def initialize() {
	// TODO: subscribe to attributes, devices, locations, etc.
    //checkTrafficHandler()
}

def checkTrafficHandler(evt) {
	log.debug "Event = $evt"

    def today = new Date()
    def todayFormatted = Date.parse( "M-d-yyyy", today)
    // Connect to mapquest API
	def params = [
        uri: "http://www.mapquestapi.com",
        path: "/directions/v2/route?",
        headers: ['Cache-Control': 'no-cache', 'Content-Type': 'application/x-www-form-urlencoded'],
        body: [
        	'key': 'Fmjtd%7Cluur20u82u%2Can%3Do5-9ay506',
            'from': '${from}',
            'to': '${to}',
			'narrativeType': 'none',
            'ambiguities': 'ignore',
            'routeType': 'fastest',
            'unit': 'm',
            'outFormat': 'json',
            'useTraffic': 'true',
            'timeType': '3',
            'dateType': '0',
            'date': '${todayFormatted}',
            'localTime': '${arrivalTime}',
            ]
	]

    httpPost(params) {response ->

    	if(method != null) {
        	api(method, args, success)
      	}
        return result
    }
}

def leaveTime = seconds_to_mmss(realTime) -

def seconds_to_hhmmss(sec) {
    ((int)sec / 3600) + ':' + ((int)sec / 60) + ':' + sec % 60
}
def hhmmss_to_seconds(s) {
    ints = s.tokenize(':').collect { Integer.parseInt(it) }
    (ints[0] * 60 + ints[1]) * 60 + ints[2]
}
