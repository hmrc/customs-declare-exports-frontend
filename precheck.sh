#!/usr/bin/env bash

sbt clean coverage test it:test scalafmt::test test:scalafmt::test coverageReport
