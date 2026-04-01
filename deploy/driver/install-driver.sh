#!/usr/bin/env sh
set -eu

SCRIPT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
REPO_ROOT=$(CDPATH= cd -- "$SCRIPT_DIR/../.." && pwd)

apt-get update
apt-get install -y build-essential linux-headers-$(uname -r)

make -C "$REPO_ROOT/driver"
insmod "$REPO_ROOT/driver/quotes_driver.ko"

echo "quotes driver loaded"
ls -l /dev/quotes
cat /proc/quotes_stats
