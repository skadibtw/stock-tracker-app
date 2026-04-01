#!/usr/bin/env sh
set -eu

if lsmod | grep -q '^quotes_driver'; then
  rmmod quotes_driver
fi

echo "quotes driver unloaded"
