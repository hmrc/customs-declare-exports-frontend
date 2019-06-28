#!/bin/bash

sm --start ASSETS_FRONTEND -f -r3.3.2 && sm --start CUSTOMS_DECLARE_EXPORTS_FRONTEND_DEPS -f && sbt run