"""Prevent publishing a monitor APK that omits the modern application classes."""

from pathlib import Path
import sys
from zipfile import ZipFile

source_root = Path(__file__).resolve().parents[1] / "app/src/main/java"
required = {
    "L" + path.relative_to(source_root).with_suffix("").as_posix() + ";"
    for path in (source_root / "com/vone").rglob("*.java")
}
assert "Lcom/vone/vmq/MonitorSign;" in required, "Modern monitor signing source is missing"
assert "Lcom/vone/vmq/MonitorResponse;" in required, "Modern response handling source is missing"

with ZipFile(sys.argv[1]) as apk:
    bad_entry = apk.testzip()
    assert bad_entry is None, f"Corrupt APK entry: {bad_entry}"
    dex = b"\0".join(apk.read(name) for name in apk.namelist() if name.endswith(".dex"))

missing = sorted(name for name in required if name.encode("utf-8") not in dex)
assert not missing, f"APK omits application classes: {missing}"
print(f"Verified all {len(required)} top-level application classes in APK")
