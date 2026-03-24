#!/usr/bin/env bash
set -euo pipefail

echo "⚠️  This will discard ALL uncommitted changes and reset to the challenge branch."
read -r -p "Are you sure? (yes/N) " confirm
if [[ "$confirm" != "yes" ]]; then
  echo "Aborted."
  exit 0
fi

git reset --hard
git clean -fd
git checkout challenge/patient-overview-pr
echo "✅ Reset complete. On branch: $(git branch --show-current)"
