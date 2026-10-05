#!/usr/bin/env bash

grep -q "'" conf/messages.*  && \
  echo "****** ERROR!! The 'messages' file contains one or more single quotes. ******" && \
  echo "****** Please, replace them with opening and closing quotes. ******" && \
  return
sbt precommit
