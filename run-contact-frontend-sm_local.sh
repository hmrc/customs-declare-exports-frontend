#!/bin/bash

sm --stop CONTACT_FRONTEND
sm --start CONTACT --appendArgs '{"CONTACT_FRONTEND":["-DbackUrlDestinationWhitelist=http://localhost:6791,http://localhost:6793,http://localhost:6796"]}'
