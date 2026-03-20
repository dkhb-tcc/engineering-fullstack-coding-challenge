#!/usr/bin/env bash
set -euo pipefail

REPO_ROOT="/workspaces/$(basename "$PWD")"

if [ -d "$REPO_ROOT/frontend" ]; then
  cd "$REPO_ROOT/frontend"
  npm install
fi

if [ -d "$REPO_ROOT/patient-service" ]; then
  cd "$REPO_ROOT/patient-service"
  PYTHON_BIN=python3
  if ! command -v python3 >/dev/null 2>&1; then
    PYTHON_BIN=python
  fi
  $PYTHON_BIN -m venv .venv
  . .venv/bin/activate
  python -m pip install --upgrade pip
  if [ -f requirements.txt ]; then
    pip install -r requirements.txt
  fi
fi
