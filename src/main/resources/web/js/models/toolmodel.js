/*
   Copyright 2012 IBM

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
        enabled: false,
        description: '',
    },

});

window.ToolCollection = Backbone.Collection.extend({

    model:Tool,
  
    initialize:function () {
        // console.log('...Initializing Tools');
      	var self = this;
      	
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
      				   count = count+1;
      				    switch(tool.name){
      				    
      				   		case 'firewall':
      				   		
      				   			/////////////
      				   		
      				   			t = "Firewall";
      				   			uri="/wm/firewall/";
      				   			desc="A firewall based application";
      				   			break;
      				   			
      				   			/////////////	
      				   		
      				   		case 'qos':
      				   			
      				   			/////////////
      				   		
      				   			t = "Quality of Service";
      				   			uri="/wm/qos/";
      				   			desc="A Quality of Service based application, providing DiffServ/ToS and Queue bases QoS";
      				   			is_enabled = tools.isenabled;    				   			
      				   			break;
      				   			
      				   			/////////////
      				   			
      				   		default:
      				   			t = "none";
      				   		console.log("Module Not Recognized");
      				   }
      				   if (t != "none"){
      				    //get the status of the tool
      				    //***************************
      				    enabled = getStatus(tool.name);
      				    //***************************
          				self.add({toolName: t, toolId: count, restURI: uri, description: desc, enabled: enabled, id: count});
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
            	console.log(status);
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
