#!/usr/bin/env python3
"""Generate the editable SVG icons and their native Android VectorDrawable twins.

No third-party packages or runtime dependencies. Run from any directory:
    python3 tools/sync_icons.py
    python3 tools/sync_icons.py --check
All shapes are original, on a 24-unit grid (108 for the launcher).
"""
from pathlib import Path
import argparse
import math
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
ANDROID = "http://schemas.android.com/apk/res/android"
SVG = "http://www.w3.org/2000/svg"
INK = "#F3F6FC"
ACCENT = "#8AB4FF"
SURFACE = "#2A303B"
BACKGROUND = "#101318"
ON_ACCENT = "#0B1B33"


def dot(x, y, radius):
    return (f"M{x-radius},{y}a{radius},{radius} 0 1,0 {2*radius},0 "
            f"a{radius},{radius} 0 1,0 {-2*radius},0Z", INK, None)


def gear():
    points = []
    for tooth in range(8):
        for offset, radius in [(0, 8), (.18, 10), (.62, 10), (.80, 8)]:
            angle = math.tau * (tooth + offset) / 8 - math.pi / 2
            points.append((12 + radius * math.cos(angle), 12 + radius * math.sin(angle)))
    return "M" + "L".join(f"{x:.3f},{y:.3f}" for x, y in points) + "Z"


# Each path is (geometry, fill, stroke). Keeping both formats in one place prevents
# the Android keyboard and the SVG design sources from drifting apart.
ICONS = {
    "font": [
        ("M2.5,18.5L8,5.5L13.5,18.5M4.5,14H11.5", None, INK),
        ("M21,12.5C19.5,11 16,11.2 16,13.5C16,15.5 21,14.3 21,16.5C21,19 17.5,19.5 16,17.5M21,11.5V18.5", None, INK),
    ],
    "emoji": [
        ("M21,12a9,9 0 1,0 -18,0a9,9 0 1,0 18,0Z", None, INK),
        ("M7.5,14C9.5,17.5 14.5,17.5 16.5,14", None, INK),
        dot(8.5, 9, 1), dot(15.5, 9, 1),
    ],
    "clipboard": [
        ("M8,5H6a2,2 0 0,0 -2,2V20a2,2 0 0,0 2,2H18a2,2 0 0,0 2,-2V7a2,2 0 0,0 -2,-2H16", None, INK),
        ("M9,2H15a1,1 0 0,1 1,1V5a1,1 0 0,1 -1,1H9a1,1 0 0,1 -1,-1V3a1,1 0 0,1 1,-1Z", None, INK),
        ("M8,11H16M8,16H14", None, INK),
    ],
    "settings": [(gear(), None, INK),
                 ("M15.2,12a3.2,3.2 0 1,0 -6.4,0a3.2,3.2 0 1,0 6.4,0Z", None, INK)],
    "more": [dot(5, 12, 1.6), dot(12, 12, 1.6), dot(19, 12, 1.6)],
    "shift": [("M12,3L21,12H16V20H8V12H3Z", None, INK)],
    "shift_lock": [("M12,2L20,10H15.5V17H8.5V10H4Z", None, INK),
                   ("M8.5,21H15.5", None, INK)],
    "backspace": [
        ("M9,5H20a1,1 0 0,1 1,1V18a1,1 0 0,1 -1,1H9L2,12Z", None, INK),
        ("M10.5,9L16.5,15M16.5,9L10.5,15", None, INK),
    ],
    "enter": [("M20,4V12a3,3 0 0,1 -3,3H4M8,11L4,15L8,19", None, INK)],
    "search": [("M17,10.5a6.5,6.5 0 1,0 -13,0a6.5,6.5 0 1,0 13,0Z", None, INK),
               ("M15.3,15.3L21,21", None, INK)],
    "send": [("M22,2L9,15M22,2L14,22L9,15L2,10Z", None, INK)],
    "done": [("M4,12L9,17L20,6", None, INK)],
    "go": [("M3,12H21M14,5L21,12L14,19", None, INK)],
    "next": [("M12,3V17M6,11L12,17L18,11M5,21H19", None, INK)],
    "previous": [("M12,21V7M6,13L12,7L18,13M5,3H19", None, INK)],
    "close": [("M6,6L18,18M18,6L6,18", None, INK)],
    "check": [("M5,12L10,17L20,7", None, INK)],
    "keyboard": [
        ("M4,5H20a2,2 0 0,1 2,2V17a2,2 0 0,1 -2,2H4a2,2 0 0,1 -2,-2V7a2,2 0 0,1 2,-2Z", None, INK),
        ("M6,9H6.1M10,9H10.1M14,9H14.1M18,9H18.1M6,12H6.1M10,12H10.1M14,12H14.1M18,12H18.1M7,15H17", None, INK),
    ],
    "keyboard_hide": [
        ("M4,3H20a2,2 0 0,1 2,2V13a2,2 0 0,1 -2,2H4a2,2 0 0,1 -2,-2V5a2,2 0 0,1 2,-2Z", None, INK),
        ("M6,7H6.1M10,7H10.1M14,7H14.1M18,7H18.1M7,11H17M8,19L12,23L16,19", None, INK),
    ],
    "copy": [
        ("M9,8H19a2,2 0 0,1 2,2V20a2,2 0 0,1 -2,2H9a2,2 0 0,1 -2,-2V10a2,2 0 0,1 2,-2Z", None, INK),
        ("M16,8V4a2,2 0 0,0 -2,-2H4a2,2 0 0,0 -2,2V14a2,2 0 0,0 2,2H7", None, INK),
    ],
    "paste": [
        ("M8,5H6a2,2 0 0,0 -2,2V20a2,2 0 0,0 2,2H18a2,2 0 0,0 2,-2V7a2,2 0 0,0 -2,-2H16", None, INK),
        ("M9,2H15a1,1 0 0,1 1,1V5a1,1 0 0,1 -1,1H9a1,1 0 0,1 -1,-1V3a1,1 0 0,1 1,-1Z", None, INK),
        ("M12,10V18M8.5,14.5L12,18L15.5,14.5", None, INK),
    ],
    "refresh": [("M20,8A8.5,8.5 0 0,0 5,6L2,9M2,3V9H8M4,16A8.5,8.5 0 0,0 19,18L22,15M22,21V15H16", None, INK)],
    "edit": [("M16,3L21,8L8,21H3V16ZM13,6L18,11M3,16L8,21", None, INK)],
    "symbols": [("M3,7H11M7,3V11M14,16L21,23M21,16L14,23M3,20H10M15,4H21M15,8H21", None, INK)],
}

LAUNCHER = [
    ("M20,0H88a20,20 0 0,1 20,20V88a20,20 0 0,1 -20,20H20a20,20 0 0,1 -20,-20V20a20,20 0 0,1 20,-20Z", BACKGROUND, None),
    ("M22,27H86a9,9 0 0,1 9,9V76a9,9 0 0,1 -9,9H22a9,9 0 0,1 -9,-9V36a9,9 0 0,1 9,-9Z", SURFACE, None),
    ("M23,37H31V45H23ZM39,37H47V45H39ZM55,37H63V45H55ZM71,37H85V45H71ZM23,53H35V61H23ZM73,53H85V61H73ZM35,70H73V76H35Z", "#657187", None),
    ("M43,63L54,39L65,63M47,55H61", None, ACCENT),
]
FOREGROUND = [
    ("M26,34H82a7,7 0 0,1 7,7V71a7,7 0 0,1 -7,7H26a7,7 0 0,1 -7,-7V41a7,7 0 0,1 7,-7Z", SURFACE, None),
    ("M28,42H35V48H28ZM41,42H48V48H41ZM60,42H67V48H60ZM73,42H80V48H73ZM28,54H38V60H28ZM70,54H80V60H70ZM39,65H69V70H39Z", "#657187", None),
    ("M44,62L54,40L64,62M48,55H60", None, ACCENT),
]


def color(value):
    if value is None:
        return None
    return {INK: "@color/text_primary", ACCENT: "@color/accent"}.get(value, value)


def files():
    result = {}
    all_icons = [(name, paths, 24) for name, paths in ICONS.items()]
    all_icons += [("launcher", LAUNCHER, 108), ("launcher_foreground", FOREGROUND, 108)]
    for name, paths, size in all_icons:
        svg = ET.Element("svg", {
            "xmlns": SVG, "width": str(size), "height": str(size),
            "viewBox": f"0 0 {size} {size}", "fill": "none",
        })
        ET.SubElement(svg, "title").text = name.replace("_", " ")
        vector = ET.Element("vector", {
            "xmlns:android": ANDROID,
            f"{{{ANDROID}}}width": f"{size}dp", f"{{{ANDROID}}}height": f"{size}dp",
            f"{{{ANDROID}}}viewportWidth": str(size), f"{{{ANDROID}}}viewportHeight": str(size),
        })
        for data, fill, stroke in paths:
            node = ET.SubElement(svg, "path", {"d": data})
            android_path = ET.SubElement(vector, "path", {f"{{{ANDROID}}}pathData": data})
            if fill:
                node.set("fill", fill)
                android_path.set(f"{{{ANDROID}}}fillColor", color(fill))
            if stroke:
                node.set("stroke", stroke)
                node.set("stroke-width", "4" if size == 108 else "1.8")
                node.set("stroke-linecap", "round")
                node.set("stroke-linejoin", "round")
                android_path.set(f"{{{ANDROID}}}strokeColor", color(stroke))
                android_path.set(f"{{{ANDROID}}}strokeWidth", "4" if size == 108 else "1.8")
                android_path.set(f"{{{ANDROID}}}strokeLineCap", "round")
                android_path.set(f"{{{ANDROID}}}strokeLineJoin", "round")
        for element in (svg, vector):
            ET.indent(element, space="    ")
        stem = "ic_" + name if size == 24 else "ic_" + name
        result[f"design/icons/{stem}.svg"] = ET.tostring(svg, encoding="unicode") + "\n"
        result[f"app/src/main/res/drawable/{stem}.xml"] = ET.tostring(vector, encoding="unicode") + "\n"
    return result


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--check", action="store_true", help="fail if generated files are out of date")
    args = parser.parse_args()
    changed = []
    for name, content in files().items():
        path = ROOT / name
        if args.check:
            if not path.exists() or path.read_text() != content:
                changed.append(name)
        else:
            path.parent.mkdir(parents=True, exist_ok=True)
            if not path.exists() or path.read_text() != content:
                path.write_text(content)
                changed.append(name)
    if args.check and changed:
        raise SystemExit("Out-of-date icons: " + ", ".join(changed))
    print(f"{len(changed)} icon files {'need updating' if args.check else 'updated'}; SVG and Android vectors are in sync.")


if __name__ == "__main__":
    main()
