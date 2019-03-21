#!/usr/bin/env bash

sbt clean scalafmt test:scalafmt coverage test it:test coverageReport
