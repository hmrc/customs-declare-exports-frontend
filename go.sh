#!/bin/bash

sm --start ASSETS_FRONTEND -f -r 4.3.1 && sm --start CUSTOMS_DECLARE_EXPORTS_FRONTEND_DEPS -f && sbt run