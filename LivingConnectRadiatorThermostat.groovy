/**
 *  Copyright 2015 SmartThings
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
 */
metadata {
	definition (name: "Living Connect Radiator Thermostat", namespace: "Danfoss", author: "AdamV") {
		capability "Actuator"
		capability "Temperature Measurement"
		capability "Relative Humidity Measurement"
		capability "Thermostat"
		capability "Configuration"
		capability "Polling"
		capability "Sensor"
        capability "Battery"
		
		attribute "thermostatFanState", "string"

		command "switchMode"
		command "switchFanMode"
        command "quickSetCool"
        command "quickSetHeat"

		fingerprint deviceId: "0x0804"
		fingerprint inClusters: "0x43,0x40,0x44,0x31,0x80,0x46,0x81,0x72,0x8F,0x75,0x86,0x84,0xEF,0x46,0x81,0x8F"
	}

	// simulator metadata
	simulator {
		status "off"			: "command: 4003, payload: 00"
		status "heat"			: "command: 4003, payload: 01"
		status "cool"			: "command: 4003, payload: 02"
		status "auto"			: "command: 4003, payload: 03"


		// reply messages
		reply "2502": "command: 2503, payload: FF"
	}

	tiles (scale: 2){
    
    		multiAttributeTile(name:"heatingSetpoint", type: "thermostat", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.heatingSetpoint", key: "PRIMARY_CONTROL") {
   		         attributeState("heat", unit:"C", label:'${currentValue}°', 
                backgroundColors:[
                    [value: 0, color: "#153591"],
                    [value: 7, color: "#1e9cbb"],
                    [value: 15, color: "#90d2a7"],
                    [value: 23, color: "#44b621"],
                    [value: 29, color: "#f1d801"],
                    [value: 35, color: "#d04e00"],
                    [value: 36, color: "#bc2323"]
               		 ]
           		 )
            }	
			tileAttribute("device.heatingSetpoint", key: "VALUE_CONTROL") {
				attributeState "heat", action:"quickSetHeat"
			}
        }

/*        
		valueTile("temperature", "device.temperature", width: 2, height: 2) {
			state("temperature", label:'${currentValue}°',
				backgroundColors:[
					[value: 31, color: "#153591"],
					[value: 44, color: "#1e9cbb"],
					[value: 59, color: "#90d2a7"],
					[value: 74, color: "#44b621"],
					[value: 84, color: "#f1d801"],
					[value: 95, color: "#d04e00"],
					[value: 96, color: "#bc2323"]
				]
			)
		}
*/
/*		standardTile("fanMode", "device.thermostatFanMode", inactiveLabel: false, decoration: "flat") {
			state "fanAuto", label:'${name}', action:"switchFanMode"
			state "fanOn", label:'${name}', action:"switchFanMode"
			state "fanCirculate", label:'${name}', action:"switchFanMode"
		}
        
*/
        
        valueTile("battery", "device.battery", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
         	tileAttribute ("device.battery", key: "PRIMARY_CONTROL"){
                        state "battery", label:'${currentValue}% battery', unit:""
        	}
        }
	//	controlTile("heatSliderControl", "device.heatingSetpoint", "slider", height: 2, width: 6, inactiveLabel: false) {
	//		state "setHeatingSetpoint", action:"quickSetHeat", backgroundColor:"#d04e00"
	//	}
//		valueTile("heatingSetpoint", "device.heatingSetpoint", inactiveLabel: false, decoration: "flat") {
//			state "heat", label:'${currentValue}° heat', backgroundColor:"#ffffff"
//		}
//		controlTile("coolSliderControl", "device.coolingSetpoint", "slider", height: 1, width: 2, inactiveLabel: false) {
//			state "setCoolingSetpoint", action:"quickSetCool", backgroundColor: "#1e9cbb"
//		}
//		valueTile("coolingSetpoint", "device.coolingSetpoint", inactiveLabel: false, decoration: "flat") {
//			state "cool", label:'${currentValue}° cool', backgroundColor:"#ffffff"
//		}
		standardTile("refresh", "device.thermostatMode", inactiveLabel: false, decoration: "flat") {
			state "default", action:"polling.poll", icon:"st.secondary.refresh"
		}
		standardTile("configure", "device.configure", inactiveLabel: false, decoration: "flat") {
			state "configure", label:'', action:"configuration.configure", icon:"st.secondary.configure"
		}
		main "heatingSetpoint"
		details(["heatingSetpoint", "heatSliderControl", "battery", , "refresh", "configure", "temperature", "mode", "fanMode", "coolSliderControl", "coolingSetpoint"])
	}
}

/*
def parse(String description)
{
	log.debug("Raw description: $description")
    def map = createEvent(zwaveEvent(zwave.parse(description, [0x43:2, 0x31: 3])))
	if (!map) {
		return null
	}

	def result = [map]
	if (map.isStateChange && map.name in ["heatingSetpoint","coolingSetpoint","thermostatMode"]) {
		def map2 = [
			name: "thermostatSetpoint",
			unit: getTemperatureScale()
		]
		if (map.name == "thermostatMode") {
			state.lastTriedMode = map.value
			if (map.value == "cool") {
				map2.value = device.latestValue("coolingSetpoint")
				log.info "THERMOSTAT, latest cooling setpoint = ${map2.value}"
			}
			else {
				map2.value = device.latestValue("heatingSetpoint")
				log.info "THERMOSTAT, latest heating setpoint = ${map2.value}"
			}
		}
		else {
			def mode = device.latestValue("thermostatMode")
			log.info "THERMOSTAT, latest mode = ${mode}"
			if ((map.name == "heatingSetpoint" && mode == "heat") || (map.name == "coolingSetpoint" && mode == "cool")) {
				map2.value = map.value
				map2.unit = map.unit
			}
		}
		if (map2.value != null) {
			log.debug "THERMOSTAT, adding setpoint event: $map"
			result << createEvent(map2)
		}
	} else if (map.name == "thermostatFanMode" && map.isStateChange) {
		state.lastTriedFanMode = map.value
	}
	log.debug "Parse returned $result"
	result
}
*/
def parse(String description) {
	state.count = 0
    def results = []
    // log.debug("RAW command: $description")
	if (description.startsWith("Err")) {
		log.debug("An error has occurred")
		} 
    else {
       
       	def cmd = zwave.parse(description, [0x43:2, 0x31: 3])
        // log.debug "Parsed Command: $cmd"
        if (cmd) {
       	results = zwaveEvent(cmd)
		}
    }
}


// Event Generation
def zwaveEvent(physicalgraph.zwave.commands.thermostatsetpointv2.ThermostatSetpointReport cmd)
{
	def cmdScale = cmd.scale == 1 ? "F" : "C"
	def map = [:]
	map.value = convertTemperatureIfNeeded(cmd.scaledValue, cmdScale, cmd.precision)
	map.unit = getTemperatureScale()
	map.displayed = false
	switch (cmd.setpointType) {
		case 1:
			map.name = "heatingSetpoint"
			break;
		case 2:
			map.name = "coolingSetpoint"
			break;
		default:
			return [:]
	}
	// So we can respond with same format
	state.size = cmd.size
	state.scale = cmd.scale
	state.precision = cmd.precision
	map
    sendEvent(map)
}

def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv3.SensorMultilevelReport cmd)
{
	def map = [:]
	if (cmd.sensorType == 1) {
		map.value = convertTemperatureIfNeeded(cmd.scaledSensorValue, cmd.scale == 1 ? "F" : "C", cmd.precision)
		map.unit = getTemperatureScale()
		map.name = "temperature"
	} else if (cmd.sensorType == 5) {
		map.value = cmd.scaledSensorValue
		map.unit = "%"
		map.name = "humidity"
	}
	map
}

def zwaveEvent(physicalgraph.zwave.commands.versionv1.VersionReport cmd) {
	def fw = "${cmd.applicationVersion}.${cmd.applicationSubVersion}"
	updateDataValue("fw", fw)
	def text = "$device.displayName: firmware version: $fw, Z-Wave version: ${cmd.zWaveProtocolVersion}.${cmd.zWaveProtocolSubVersion}"
	createEvent(descriptionText: text, isStateChange: false)
}

def zwaveEvent(physicalgraph.zwave.commands.thermostatoperatingstatev1.ThermostatOperatingStateReport cmd)
{
	def map = [:]
	switch (cmd.operatingState) {
		case physicalgraph.zwave.commands.thermostatoperatingstatev1.ThermostatOperatingStateReport.OPERATING_STATE_IDLE:
			map.value = "idle"
			break
		case physicalgraph.zwave.commands.thermostatoperatingstatev1.ThermostatOperatingStateReport.OPERATING_STATE_HEATING:
			map.value = "heating"
			break
		case physicalgraph.zwave.commands.thermostatoperatingstatev1.ThermostatOperatingStateReport.OPERATING_STATE_COOLING:
			map.value = "cooling"
			break
		case physicalgraph.zwave.commands.thermostatoperatingstatev1.ThermostatOperatingStateReport.OPERATING_STATE_FAN_ONLY:
			map.value = "fan only"
			break
		case physicalgraph.zwave.commands.thermostatoperatingstatev1.ThermostatOperatingStateReport.OPERATING_STATE_PENDING_HEAT:
			map.value = "pending heat"
			break
		case physicalgraph.zwave.commands.thermostatoperatingstatev1.ThermostatOperatingStateReport.OPERATING_STATE_PENDING_COOL:
			map.value = "pending cool"
			break
		case physicalgraph.zwave.commands.thermostatoperatingstatev1.ThermostatOperatingStateReport.OPERATING_STATE_VENT_ECONOMIZER:
			map.value = "vent economizer"
			break
	}
	map.name = "thermostatOperatingState"
	map
}

def zwaveEvent(physicalgraph.zwave.commands.thermostatfanstatev1.ThermostatFanStateReport cmd) {
	def map = [name: "thermostatFanState", unit: ""]
	switch (cmd.fanOperatingState) {
		case 0:
			map.value = "idle"
			break
		case 1:
			map.value = "running"
			break
		case 2:
			map.value = "running high"
			break
	}
	map
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
        def map = [ name: "battery", unit: "%" ]
        if (cmd.batteryLevel == 0xFF) {  // Special value for low battery alert
                map.value = 1
                map.descriptionText = "${device.displayName} has a low battery"
                map.isStateChange = true
        } else {
                map.value = cmd.batteryLevel
                log.debug ("Battery: $cmd.batteryLevel")
        }
        // Store time of last battery update so we don't ask every wakeup, see WakeUpNotification handler
        state.lastbatt = new Date().time
        createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpNotification cmd)
{
       // state.count = state.count + 1
        log.debug("Device Wakeup: $cmd")
        def newSetPoint = state.setPoint
        def result = []
        
        result << createEvent(descriptionText: "${device.displayName} woke up", isStateChange: false)
        result << zwave.wakeUpV2.wakeUpNoMoreInformation().format()
        result << setHeatingSetpoint(newSetPoint, 1000)
        result << log.debug("sent response")
        
        delayBetween([result], 2000)
        
        
     //   def result = [createEvent(descriptionText: "${device.displayName} woke up", isStateChange: false)]
	
   // if (state.count == 2){
    
  //  def result = []
        // Only ask for battery if we haven't had a BatteryReport in a while
    //    if (!state.lastbatt || (new Date().time) - state.lastbatt > 24*60*60*1000) {
    //            result << response(zwave.batteryV1.batteryGet())
    //            result << response("delay 1200")  // leave time for device to respond to batteryGet
    //    }
    //    def deviceScale = state.scale ?: 1
     //   def p = (state.precision == null) ? 1 : state.precision
     //   result << response(zwave.thermostatSetpointV1.thermostatSetpointSet(setpointType: 1, scale: deviceScale, precision: p, scaledValue: state.setPoint))
     //   result << response(zwave.thermostatSetpointV1.thermostatSetpointGet(setpointType: 1))
     //   result << response(setHeatingSetpoint(state.setPoint, 1000))
     //   result << response("delay 1000")
      //  result << setHeatingSetpoint(state.setPoint, 1000)
       // result << response("delay 1000")
      //  setHeatingSetpoint(state.setPoint, 1000)
       // result << response(zwave.wakeUpV2.wakeUpNoMoreInformation())
        // setHeatingSetpoint(state.setPoint, 1000)
       //		def deviceScale = state.scale ?: 1
		//	def deviceScaleString = deviceScale == 2 ? "C" : "F"
    	//	def locationScale = getTemperatureScale()
		//	def p = (state.precision == null) ? 1 : state.precision


     /*   if (state.count == 3){
        result
        log.debug("response fired count: $state.count")
        state.count = 0
        }
        */
    //    poll()
    //log.debug("response fired count: $state.count")
    //state.count = 0
   //}
   
 //  result
}


def zwaveEvent(physicalgraph.zwave.commands.thermostatmodev2.ThermostatModeReport cmd) {
	def map = [:]
	switch (cmd.mode) {
		case physicalgraph.zwave.commands.thermostatmodev2.ThermostatModeReport.MODE_OFF:
			map.value = "off"
			break
		case physicalgraph.zwave.commands.thermostatmodev2.ThermostatModeReport.MODE_HEAT:
			map.value = "heat"
			break
		case physicalgraph.zwave.commands.thermostatmodev2.ThermostatModeReport.MODE_AUXILIARY_HEAT:
			map.value = "emergency heat"
			break
		case physicalgraph.zwave.commands.thermostatmodev2.ThermostatModeReport.MODE_COOL:
			map.value = "cool"
			break
		case physicalgraph.zwave.commands.thermostatmodev2.ThermostatModeReport.MODE_AUTO:
			map.value = "auto"
			break
	}
	map.name = "thermostatMode"
	map
}

def zwaveEvent(physicalgraph.zwave.commands.thermostatfanmodev3.ThermostatFanModeReport cmd) {
	def map = [:]
	switch (cmd.fanMode) {
		case physicalgraph.zwave.commands.thermostatfanmodev3.ThermostatFanModeReport.FAN_MODE_AUTO_LOW:
			map.value = "fanAuto"
			break
		case physicalgraph.zwave.commands.thermostatfanmodev3.ThermostatFanModeReport.FAN_MODE_LOW:
			map.value = "fanOn"
			break
		case physicalgraph.zwave.commands.thermostatfanmodev3.ThermostatFanModeReport.FAN_MODE_CIRCULATION:
			map.value = "fanCirculate"
			break
	}
	map.name = "thermostatFanMode"
	map.displayed = false
	map
}

def zwaveEvent(physicalgraph.zwave.commands.thermostatmodev2.ThermostatModeSupportedReport cmd) {
	def supportedModes = ""
	if(cmd.off) { supportedModes += "off " }
	if(cmd.heat) { supportedModes += "heat " }
	if(cmd.auxiliaryemergencyHeat) { supportedModes += "emergency heat " }
	if(cmd.cool) { supportedModes += "cool " }
	if(cmd.auto) { supportedModes += "auto " }

	state.supportedModes = supportedModes
}

def zwaveEvent(physicalgraph.zwave.commands.thermostatfanmodev3.ThermostatFanModeSupportedReport cmd) {
	def supportedFanModes = ""
	if(cmd.auto) { supportedFanModes += "fanAuto " }
	if(cmd.low) { supportedFanModes += "fanOn " }
	if(cmd.circulation) { supportedFanModes += "fanCirculate " }

	state.supportedFanModes = supportedFanModes
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
	log.debug "Zwave event received: $cmd"
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	log.warn "Unexpected zwave command $cmd"
}

// Command Implementations
def poll() {
	delayBetween([
		// zwave.sensorMultilevelV3.sensorMultilevelGet().format(), // current temperature
		zwave.thermostatSetpointV2.thermostatSetpointGet(setpointType: 1).format(),
		zwave.thermostatSetpointV2.thermostatSetpointGet(setpointType: 2).format(),
		zwave.thermostatOperatingStateV1.thermostatOperatingStateGet().format()
	], 2300)
}

def quickSetHeat(upDown) {
	def change = upDown
    def latest = device.latestValue("heatingSetpoint")
    if (change == 1) {
    	def newSetPoint = latest + 1
        log.debug("New Set Point: $newSetPoint")
		state.setPoint = newSetPoint
        log.debug("New State Set Point: $state.setPoint")
    	setHeatingSetpoint(newSetPoint, 1000)
    }
    else if (change == 0) {
    	def newSetPoint = latest - 1
        log.debug("New Set Point: $newSetPoint")
        state.setPoint = newSetPoint
        log.debug("New State Set Point: $state.setPoint")
    	setHeatingSetpoint(newSetPoint, 1000)
    }
	//setHeatingSetpoint(degrees, 1000)
    // log.debug("Degrees at quicksetheat: $degrees")
}

//def setHeatingSetpoint(degrees, delay = 30000) {
//	setHeatingSetpoint(degrees.toDouble(), delay)
  //      log.debug("Degrees at setheatpoint: $degrees")
//}

def setHeatingSetpoint(Double degrees, Integer delay) {
	log.trace "setHeatingSetpoint($degrees, $delay)"
	def deviceScale = state.scale ?: 1
	def deviceScaleString = deviceScale == 2 ? "C" : "F"
    def locationScale = getTemperatureScale()
	def p = (state.precision == null) ? 1 : state.precision

    def convertedDegrees
    if (locationScale == "C" && deviceScaleString == "F") {
    	convertedDegrees = celsiusToFahrenheit(degrees)
    } else if (locationScale == "F" && deviceScaleString == "C") {
    	convertedDegrees = fahrenheitToCelsius(degrees)
    } else {
    	convertedDegrees = degrees
    }

	delayBetween([
		zwave.thermostatSetpointV1.thermostatSetpointSet(setpointType: 1, scale: deviceScale, precision: p, scaledValue: convertedDegrees).format(),
		zwave.thermostatSetpointV1.thermostatSetpointGet(setpointType: 1).format()
	], delay)
}

def quickSetCool(degrees) {
	setCoolingSetpoint(degrees, 1000)
}

def setCoolingSetpoint(degrees, delay = 30000) {
	setCoolingSetpoint(degrees.toDouble(), delay)
}

def setCoolingSetpoint(Double degrees, Integer delay = 30000) {
    log.trace "setCoolingSetpoint($degrees, $delay)"
	def deviceScale = state.scale ?: 1
	def deviceScaleString = deviceScale == 2 ? "C" : "F"
    def locationScale = getTemperatureScale()
	def p = (state.precision == null) ? 1 : state.precision

    def convertedDegrees
    if (locationScale == "C" && deviceScaleString == "F") {
    	convertedDegrees = celsiusToFahrenheit(degrees)
    } else if (locationScale == "F" && deviceScaleString == "C") {
    	convertedDegrees = fahrenheitToCelsius(degrees)
    } else {
    	convertedDegrees = degrees
    }

	delayBetween([
		zwave.thermostatSetpointV1.thermostatSetpointSet(setpointType: 2, scale: deviceScale, precision: p,  scaledValue: convertedDegrees).format(),
		zwave.thermostatSetpointV1.thermostatSetpointGet(setpointType: 2).format()
	], delay)
}

def configure() {
	delayBetween([
    	zwave.wakeUpV2.wakeUpIntervalSet(seconds:30, nodeid:zwaveHubNodeId).format(),
		//zwave.thermostatModeV2.thermostatModeSupportedGet().format(),
		zwave.associationV1.associationSet(groupingIdentifier:1, nodeId:[zwaveHubNodeId]).format()
	], 2300)
}

def modes() {
	["off", "heat", "cool", "auto", "emergency heat"]
}

def switchMode() {
	def currentMode = device.currentState("thermostatMode")?.value
	def lastTriedMode = state.lastTriedMode ?: currentMode ?: "off"
	def supportedModes = getDataByName("supportedModes")
	def modeOrder = modes()
	def next = { modeOrder[modeOrder.indexOf(it) + 1] ?: modeOrder[0] }
	def nextMode = next(lastTriedMode)
	if (supportedModes?.contains(currentMode)) {
		while (!supportedModes.contains(nextMode) && nextMode != "off") {
			nextMode = next(nextMode)
		}
	}
	state.lastTriedMode = nextMode
	delayBetween([
		zwave.thermostatModeV2.thermostatModeSet(mode: modeMap[nextMode]).format(),
		zwave.thermostatModeV2.thermostatModeGet().format()
	], 1000)
}

def switchToMode(nextMode) {
	def supportedModes = getDataByName("supportedModes")
	if(supportedModes && !supportedModes.contains(nextMode)) log.warn "thermostat mode '$nextMode' is not supported"
	if (nextMode in modes()) {
		state.lastTriedMode = nextMode
		"$nextMode"()
	} else {
		log.debug("no mode method '$nextMode'")
	}
}

def switchFanMode() {
	def currentMode = device.currentState("thermostatFanMode")?.value
	def lastTriedMode = state.lastTriedFanMode ?: currentMode ?: "off"
	def supportedModes = getDataByName("supportedFanModes") ?: "fanAuto fanOn"
	def modeOrder = ["fanAuto", "fanCirculate", "fanOn"]
	def next = { modeOrder[modeOrder.indexOf(it) + 1] ?: modeOrder[0] }
	def nextMode = next(lastTriedMode)
	while (!supportedModes?.contains(nextMode) && nextMode != "fanAuto") {
		nextMode = next(nextMode)
	}
	switchToFanMode(nextMode)
}

def switchToFanMode(nextMode) {
	def supportedFanModes = getDataByName("supportedFanModes")
	if(supportedFanModes && !supportedFanModes.contains(nextMode)) log.warn "thermostat mode '$nextMode' is not supported"

	def returnCommand
	if (nextMode == "fanAuto") {
		returnCommand = fanAuto()
	} else if (nextMode == "fanOn") {
		returnCommand = fanOn()
	} else if (nextMode == "fanCirculate") {
		returnCommand = fanCirculate()
	} else {
		log.debug("no fan mode '$nextMode'")
	}
	if(returnCommand) state.lastTriedFanMode = nextMode
	returnCommand
}

def getDataByName(String name) {
	state[name] ?: device.getDataValue(name)
}

def getModeMap() { [
	"off": 0,
	"heat": 1,
	"cool": 2,
	"auto": 3,
	"emergency heat": 4
]}

def setThermostatMode(String value) {
	delayBetween([
		zwave.thermostatModeV2.thermostatModeSet(mode: modeMap[value]).format(),
		zwave.thermostatModeV2.thermostatModeGet().format()
	], standardDelay)
}

def getFanModeMap() { [
	"auto": 0,
	"on": 1,
	"circulate": 6
]}

def setThermostatFanMode(String value) {
	delayBetween([
		zwave.thermostatFanModeV3.thermostatFanModeSet(fanMode: fanModeMap[value]).format(),
		zwave.thermostatFanModeV3.thermostatFanModeGet().format()
	], standardDelay)
}

def off() {
	delayBetween([
		zwave.thermostatModeV2.thermostatModeSet(mode: 0).format(),
		zwave.thermostatModeV2.thermostatModeGet().format()
	], standardDelay)
}

def heat() {
	delayBetween([
		zwave.thermostatModeV2.thermostatModeSet(mode: 1).format(),
		zwave.thermostatModeV2.thermostatModeGet().format()
	], standardDelay)
}

def emergencyHeat() {
	delayBetween([
		zwave.thermostatModeV2.thermostatModeSet(mode: 4).format(),
		zwave.thermostatModeV2.thermostatModeGet().format()
	], standardDelay)
}

def cool() {
	delayBetween([
		zwave.thermostatModeV2.thermostatModeSet(mode: 2).format(),
		zwave.thermostatModeV2.thermostatModeGet().format()
	], standardDelay)
}

def auto() {
	delayBetween([
		zwave.thermostatModeV2.thermostatModeSet(mode: 3).format(),
		zwave.thermostatModeV2.thermostatModeGet().format()
	], standardDelay)
}

def fanOn() {
	delayBetween([
		zwave.thermostatFanModeV3.thermostatFanModeSet(fanMode: 1).format(),
		zwave.thermostatFanModeV3.thermostatFanModeGet().format()
	], standardDelay)
}

def fanAuto() {
	delayBetween([
		zwave.thermostatFanModeV3.thermostatFanModeSet(fanMode: 0).format(),
		zwave.thermostatFanModeV3.thermostatFanModeGet().format()
	], standardDelay)
}

def fanCirculate() {
	delayBetween([
		zwave.thermostatFanModeV3.thermostatFanModeSet(fanMode: 6).format(),
		zwave.thermostatFanModeV3.thermostatFanModeGet().format()
	], standardDelay)
}

private getStandardDelay() {
	1000
}

