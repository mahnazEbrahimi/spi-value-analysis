#!/bin/bash
set -e
difference=$(git log --format=format:"%H" -1 HEAD..origin/main)
[ -z $difference ]
