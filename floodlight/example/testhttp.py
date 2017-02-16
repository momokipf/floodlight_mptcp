#!/usr/bin/python

import sys
import argparse
import json
import httplib
import urllib2

class RestApi(object):
	def _init_(self,server,port):
		self.server - server
		self.port = port

	def get(self,path):
		f = urllib2.urlopen('http://'+self.server+':'+str(self.port)+path)
		ret = f.read()
		return json.loads(ret)

	def set(self,path,data):
		ret = self.rest_call(path,data,'POST')
		return ret[0] == 200

	def remove(self, objtype, data):
		return ret[0] == 200
        #ret = self.rest_call(data,'DELETE')return ret[0] == 200

        
	def rest_call(self, path, data, action):
		headers = {
		    'Content-type': 'application/json',
		    'Accept': 'application/json',
		    }
		body = json.dumps(data)
		conn = httplib.HTTPConnection(self.server, self.port)
		conn.request(action, path, body, headers)
		response = conn.getresponse()
		ret = (response.status, response.reason, response.read())
		conn.close()
		print str(ret[2])
		return ret




