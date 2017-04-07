#!/bin/bash

curl -X POST -d '{"cmd":"DELETE","dst-port":"0","dst-switch":"00:00:00:00:00:00:00:01","src-switch":"00:00:00:00:00:00:00:01","src-port":"0"}' http://localhost:8080/wm/fdm/config/json

curl -X POST -d '{"cmd":"DELETE","dst-port":"0","dst-switch":"00:00:00:00:00:00:00:02","src-switch":"00:00:00:00:00:00:00:02","src-port":"0"}' http://localhost:8080/wm/fdm/config/json
curl -X POST -d '{"cmd":"DELETE","dst-port":"0","dst-switch":"00:00:00:00:00:00:00:03","src-switch":"00:00:00:00:00:00:00:03","src-port":"0"}' http://localhost:8080/wm/fdm/config/json

