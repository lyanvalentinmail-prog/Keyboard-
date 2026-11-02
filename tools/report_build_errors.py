#!/usr/bin/env python3
"""Expose Gradle failures as GitHub annotations even when raw log downloads fail."""
from pathlib import Path
import re
import sys

lines = Path(sys.argv[1]).read_text(errors="replace").splitlines()
pattern = re.compile(r"(^e:|error:|FAILED|What went wrong|Execution failed|Could not resolve|Caused by:|> Task)")
selected = [line for line in lines if pattern.search(line) and not line.startswith("> Task")]
if not selected:
    selected = [line for line in lines if pattern.search(line)]
# GitHub keeps only ten failure annotations per job: prioritize actionable errors.
for line in selected[-10:]:
    message = line.replace("%", "%25").replace("\r", "%0D").replace("\n", "%0A")
    print("::error title=Android build::" + message)
