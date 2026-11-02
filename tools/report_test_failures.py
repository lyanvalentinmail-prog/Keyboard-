#!/usr/bin/env python3
"""Print the first causes of failing tests to the CI log and job summary."""
from pathlib import Path
import os
import xml.etree.ElementTree as ET

summary = ["## Resultados de pruebas\n"]
for path in sorted(Path("app/build/test-results/testDebugUnitTest").glob("TEST-*.xml")):
    root = ET.parse(path).getroot()
    summary.append(f"- {root.attrib['name']}: {root.attrib['tests']} pruebas, "
                   f"{root.attrib.get('failures', 0)} fallos, {root.attrib.get('errors', 0)} errores.")
    for case in root.findall("testcase"):
        failure = case.find("failure")
        if failure is None:
            failure = case.find("error")
        if failure is not None:
            text = failure.attrib.get("message", "")
            trace = (failure.text or "").splitlines()[:7]
            detail = "\n".join([text] + trace)
            summary.append("\n### " + case.attrib["name"] + "\n```\n" + detail + "\n```\n")
report = "\n".join(summary)
with open(os.environ["GITHUB_STEP_SUMMARY"], "a") as output:
    output.write(report)
