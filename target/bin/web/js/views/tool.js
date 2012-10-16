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

window.ToolListView = Backbone.View.extend({

	initialize:function () {
			console.log('...Initializing Tools Page');
        	this.template = _.template(tpl.get('tools-list'));
    	},

	render:function (eventName) {
       		$(this.el).html(this.template({ntools:tl.length}));
			_.each(this.model.models, function (t) {
				//console.log(t);
				//$(this.el).find('#tools').append("<h1>TEST</h2>");
				$(this.el).find('#tools > tbody:first-child').append(new ToolsListItemView({model:t}).render().el);
			}, this);
			return this;
	}
});

window.ToolsListItemView = Backbone.View.extend({

	tagName:"tr",

	initialize:function () {
        this.template = _.template(tpl.get('tools-list-item'));
      },
      
    render:function () {
    	$(this.el).html(this.template(this.model.toJSON()));
        return this;
    }

});


window.ToolDetailsView = Backbone.View.extend({

	initialize:function () {
        this.template = _.template(tpl.get('tool'));
    },
    
    events:{
        "click #button":"enableToolFunction"
    },

    render:function (eventName) {
        $(this.el).html(this.template(this.model.toJSON()));
        return this;
    },
    
    enableToolFunction:function (event){
    	if (!event) var event = window.event;
    	if (event.target) targ = event.target;
    	alert("Enabling "+ targ.name);
    	
    	//***************************************
    	//***************************************
    	//TODO if targ.name == Quality of Service 
    	//     if targ.name == Firewall
    	//Send JSON "POST" to enable firewall or qos 
    	//***************************************
    	//***************************************
    }

});
