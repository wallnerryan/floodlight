/*
   Copyright 2012 IBM & Marist College 2012

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

window.Tool = Backbone.Model.extend({

    defaults: {
        toolName: '',
        toolVersion: 1.0,
        toolId: null,
        restURI: null,
        desc: "",
        enabled: false,
    },

});

window.ToolCollection = Backbone.Collection.extend({

    model:Tool,
  
    initialize:function () {
        // console.log('...Initializing Tools');
      	var self = this;
      	self.bind("change");
      	
      	var tools = [];
      	console.log("fetching controller status");
        $.ajax({
            url:hackBase + "/wm/core/tools/enabled/json",
            dataType:"json",
            success:function (data) {
                console.log("fetched tools");
                //console.log(data);
                _.each(data["tools"], function(en,key){
                	 var tool = new Object();
                	 tool.name = key;
            		 tools.push(tool);
                	});
                	//console.log(tools);
      				var ntools = tools.length;
      				var count = 0;
      				_.each(tools, function(tool) {
      				
      					//Loop through each toolname and set parameters for webui
      				    count = count+1;
      				    switch(tool.name.toLowerCase()){
      				    
      				   		case 'firewall':
      				   		
      				   			///////////////////////////////////////
      				   			t = "Firewall";
      				   			uri="/wm/firewall/";
      				   			desc = "Stateless firewall implemented as a \
      				   					Google Summer of Code project. \
      				   					Configuration done through REST API";
      				   			break;
      				   			///////////////////////////////////////
      				   		
      				   		case 'qos':
      				   			
      				   			///////////////////////////////////////
      				   			t = "Quality of Service";
      				   			uri="/wm/qos/";
      				   			desc = "A Quality of Service Module that allows \
      				   					Quality of Service state to be pushed into the \
      				   					network through REST API's. The QoS follows \
      				   					queuing QoS for max-min rate limits and a DiffServ \
      				   					model Type of Service model. Allows for users to \
      				   					define a QoS rule to be a queuing policy or a \
      				   					type of service/ diffserv policy. \
      				   					Configuration done through REST API";
      				   			break;
      				   			///////////////////////////////////////
      				   			
      				   		default:
      				   			 t = "donotadd";
      				   			console.log("Module: "+tool.name+" Not Recognized");
      				   }
      				   if (t != "donotadd"){
      				   
      				    //get the status of the tool
      				    enabled = getStatus(tool.name);
      				    //add to the model
          				self.add({toolName: t, toolId: count, restURI: uri, desc: desc, enabled: enabled, id: count});
          			   }
                     });
                 }
        });
     },
     
     fetch:function () {
        this.initialize()
    },
});

function getStatus(toolName){
		var status;
		if(toolName == "qos"){
     	 $.ajax({
            url:hackBase + "/wm/qos/status/json",
            dataType:"json",
            async: false, //need this for synchronization 
            success:function (data) {
            	status = data;
            	//console.log(status);
            }
          });
        }
        if(toolName == "firewall"){
     	 $.ajax({
            url:hackBase + "/wm/firewall/module/status/json",
            dataType:"json",
            async: false, //need this for synchronization 
            success:function (data) {
            	status = data;
            	console.log(status);
            }
          });
        }
        return status;
     }
