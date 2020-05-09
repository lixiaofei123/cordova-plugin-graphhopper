
var exec = require('cordova/exec');

var PLUGIN_NAME = 'Graphhopper';

var Graphhopper = {
    getBestPath: function(lonFrom,latFrom, lonTo,latTo,ghLocation, success, failure){
        exec(success,failure,PLUGIN_NAME,'getBestPath',[lonFrom,latFrom, lonTo,latTo,ghLocation])
    }
}

module.exports = Graphhopper