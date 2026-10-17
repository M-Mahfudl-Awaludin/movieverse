#!/usr/bin/env python3
"""Ringkas hasil Android Lint (XML) per kategori, khususnya kategori Performance.

Pemakaian: python3 scripts/lint_summary.py [folder-root]
Exit code selalu 0 (informasional); isi ringkasan dilihat di log CI.
"""
import collections
import pathlib
import sys
import xml.etree.ElementTree as ET

root = pathlib.Path(sys.argv[1] if len(sys.argv) > 1 else ".")
reports = sorted(p for p in root.glob("**/build/reports/lint-results-*.xml"))
if not reports:
    print("Tidak ada laporan lint ditemukan.")
    sys.exit(0)

performance = collections.Counter()
total = collections.Counter()
for report in reports:
    for issue in ET.parse(report).getroot().iter("issue"):
        category = issue.get("category", "?")
        total[category] += 1
        if category.startswith("Performance"):
            location = issue.find("location")
            where = location.get("file", "?") if location is not None else "?"
            performance[(issue.get("id"), where.replace(str(root), "").lstrip("/"))] += 1

print("== Ringkasan lint per kategori ==")
for category, count in sorted(total.items()):
    print(f"  {category}: {count}")
print("== Issue Performance ==")
if not performance:
    print("  (tidak ada)")
for (issue_id, where), count in sorted(performance.items()):
    print(f"  {issue_id} x{count}  {where}")
