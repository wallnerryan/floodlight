#! /usr/bin/python
# coding: utf-8
'''
QoSpusher.py -----------------------------------------------------------------------------
Developed By: Ryan Wallner (ryan.wallner1@marist.edu)
Used add Types of Service i.e.(Best Effort) and Quality of Service policy’s to the network
Utilizes the QoS modules in BigSwitch Network's Floodlight Controller
 
 NOTES:
 	This script is developed as an additional program that utilizes the QoS module
 	in floodlights opensource software defined network controller.
 	
 USAGE: 
	qospusher.py <add|delete> <service|policy> <service-object | policy object> <controller-ip>
 	*note: if you are deleting you can just reference the name of the service/policy
 	
 	[author] - rjwallner
------------------------------------------------------------------------------------------
'''
import sys
import os
import httplib      # basic HTTP library for HTTPS connections
import urllib       # used for url-encoding during login request
import simplejson # converts between JSON and python objects

def main():
	#checks
	if (len(sys.argv) == 2):
	 if sys.argv[1] == "--help":
	  usage_help()
	  exit()
	if(len(sys.argv) == 3):
	 controller = sys.argv[2]
	 if sys.argv[1] == "enable":
	 	enable(controller)
	 	exit()
	 elif  sys.argv[1] == "disable":
	 	disable(controller)
	 	exit()
	if (len(sys.argv)) != 5:
	 usage()
	 exit()
		
	#Define the od variables for the request
	cmd = sys.argv[1]
	s_p = sys.argv[2]
	obj = sys.argv[3]
	server = sys.argv[4]
	try:
	 helper = httpHelper(__name="QoSHTTPHelper")
	 print helper.get_attributes('__name')
	 helper.connect(server,8080)
	except httplib.HTTPException:
	 print "Error connecting to controller, please make sure you controller is running"
	 exit(1)
	except Exception as e:
	 print e
	 print "Error connecting to controller"
	 exit(1)
	
	if cmd == "add":
	 if s_p == "service":
	  print "Trying to add service %s" % obj
	  url = "http://%s:%d/wm/qos/service/json" % (server,8080)
	  _json = obj
	  try:
	   req = helper.request("POST",url,_json)
	   print "[CONTROLLER]: %s" % req
	   #write("service",_json) #NEEDS WORK, SEE BELOW!
	  except Exception as e:
	   print e
	   print "Could Not Complete Request"
	   exit(1)
	  helper.close_connection()
			
	 elif s_p == "policy":
	  print "Trying to add policy %s" % obj
	  url = "http://%s:%d/wm/qos/policy/json" % (server,8080)
	  _json = obj
	  try:
	   req = helper.request("POST",url,_json)
	   print "[CONTROLLER]: %s" % req
	   write("policy",_json)
	  except Exception as e:
	   print e
	   print "Could Not Complete Request"
	   exit(1)
	  helper.close_connection()
	  
	elif cmd == "delete":
	 if s_p == "service":
	  print "Trying to delete service %s" % obj
	  helper.close_connection()
	  #TODO
			
	 elif s_p == "policy":
	  print "Trying to delete policy %s" % obj
	  helper.close_connection()
	  #TODO
	else:
	 print "Error parsing command %s" % s_p
	 usage()
	 exit()

#write to json file
def write(op,json_o=None):
    conf = "qos-state.json"
    pwd = os.getcwd()
    try:
    	if os.path.exists("%s/%s" % (pwd,conf)):
    	 qos_data = open(conf)
    	else:
    	 print "Does not exists, creating %s in %s " % (conf,pwd)
         qos_data = open(conf, 'w+')
         qos_data.write('{"services":[],"policies":[]}');
         qos_data.close()
         qos_data = open(conf)
    except ValueError as e:
     print "Problem with qos-state file"
     print e
     exit(1)

    #load and encode
    data = simplejson.load(qos_data)
    sjson = simplejson.JSONEncoder(sort_keys=False,indent=3).encode(data)
    jsond = simplejson.JSONDecoder().decode(sjson)
     
    if op == "service":
     print "Writing service to qos-state.json"
     jsond['services'].append(json_o)
    elif op == "policy":
     print "Writing policy to qos.state.json"
     jsond['policies'].append(json_o)
    
    #deserialize and write back
    sjson =  simplejson.JSONEncoder(sort_keys=False,indent=3).encode(jsond)
    qos_data.close()
    newd = open(conf, 'w+')
    newd.write(sjson)
    print sjson
    newd.close()
        
#delete from json file
def remove(op,name):
    conf = "qos-state.json"
    pwd = os.getcwd()
    try:
      if os.path.exists("%s/%s" % (pwd,conf)):
       print "Opening qos-state.json in %s" % pwd
       qos_data = open(conf)
      else:
       print "%s/%s does not exist" %(pwd,conf)
    except ValueError as e:
     print "Problem with qos-state file"
     print e
     exit(1)

    #load and encode    
    data = simplejson.load(qos_data)
    sjson = simplejson.JSONEncoder(sort_keys=False,indent=3).encode(data)
    jsond = simplejson.JSONDecoder().decode(sjson)
        
    #TODO
    #ON ADD / REMOVE 
    #JSON HAS TWO MAIN ARRAYS {servcies:[]} and {policies:[]}
    #LOG WITH DATES*
    
    #deserialize and write back
    sjson =  simplejson.JSONEncoder(sort_keys=False,indent=3).encode(jsond)
    qos_data.close()
    newd = open(conf, 'w+')
    newd.write(sjson)
    print sjson
    newd.close()
    
def enable(ip):
	print "Enabling QoS at %s:%d" % (ip,8080)
	#TODO
def disable(ip):
	print "Disabling QoS at %s:%d" % (ip,8080)
	#TODO
	
class httpHelper:
	
	__name = "None"
	httpcon = None
	
	#initialize
	def __init__(self, **kvargs):
	 self._attributes = kvargs
	def set_attributes(self, key, value):
	 self.attributes[key] = value
	 return
	def get_attributes(self, key):
	 return self._attributes.get(key,None)
		
	def connect(self,ip,port):		
	 print "Trying server..."
	 try:
	   self.httpcon = httplib.HTTPConnection(ip,port)
	 except httplib.HTTPException:
	   print "Could not connect to server: %s:%s" % (ip, port)
	 except Exception as e:
	 	print "Could not connect to server: %s:%s" % (ip, port)
	 	print e
	 print "Connected to: %s:%s" % (ip,port)
	 return self.httpcon
	
	def close_connection(self):
	 try:
	  self.httpcon.close()
	 except httplib.HTTPException:
	  print "Could not close connection"
	 except Exception as e:
	  print "Could not close connection"
	  print e
	 print "Closed connection successfully"
		
	def request(self, method, url, body, content_type="application/json"):
	 headers = { "Content-Type" : content_type }
	 self.httpcon.request(method, url,body, headers)
	 response = self.httpcon.getresponse()
	 s = response.status
	 ok = httplib.OK
	 acc = httplib.ACCEPTED
	 crtd = httplib.CREATED
	 ncontnt = httplib.NO_CONTENT
	 if s != ok and s != acc and s != crtd and s != ncontnt:
		print "%s to %s got an unexpected response code: %d %s (content = '%s')" \
			% (method, url, response.status, response.reason, response.read())
	 return response.read()
	
def usage():
	print "type 'qos_pusher.py --help' for more details"
	print "qospusher.py <add|delete> <service|policy> <service-object | policy object> <controller-ip>";\
	print "qospusher.py <enable|disable> <controller-ip>"
def usage_help():
	print '''
	        ###################################
		QoSPusher.py
		Author: Ryan Wallner (Ryan.Wallner1@marist.edu)
		QoSPusher is a simple service and policy tool for 
		Floodlight's Quality of Service Module
			
		To Add a service:
		qospusher.py add service "{'name': 'Express Fowarding', 'tos': '101000'}" 127.0.0.1
							
		To Delete a service:
		qospusher.py delete service "Express Fowarding" 127.0.0.1
							
		To Add a policy:
		qospusher.py add policy "{}" 127.0.0.1
							
		To Delete a policy:
		qospusher.py delete policy "{}" 127.0.0.1
		###################################
		'''
#Call main
if  __name__ == "__main__" :
	main()
