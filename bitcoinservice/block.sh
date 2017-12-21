#!/bin/sh
curl "http://127.0.0.1:8084/api/v1/block/notification" -d "$@"
