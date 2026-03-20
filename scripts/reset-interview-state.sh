#!/usr/bin/env bash
set -euo pipefail
git reset --hard
git clean -fd
git checkout challenge/patient-overview-pr
