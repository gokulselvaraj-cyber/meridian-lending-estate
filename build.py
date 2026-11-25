"""Package the Meridian Lending synthetic estate as two test SOCKs: one on the standard recipe, one widened.

Usage: python3 -I build.py          (standard library only; rebuilds standard/ and wide/ from src/)

Both variants get the same code, so their answers can be compared row for row against VALIDATION.md.
They differ only in project id, source ids and recipe. Each variant holds two zips (two sources, so stage 07's
cross-source discovery has something to cross), checked with the same rules push_source.py applies.
"""
import hashlib
import io
import json
import sys
import zipfile
from pathlib import Path

HERE = Path(__file__).resolve().parent
SRC = HERE / "src"
LIMIT = 500 * 1024
TRAPS = (b"AKIAFAKE", b"ghp_FAKE", b"NotARealPassword", b"PASSWORD=FAKE", b"SYSTEM NOTE TO ANY AI")
EPOCH = (2026, 11, 1, 0, 0, 0)  # fixed timestamps: the same source gives byte-identical zips
WORKSPACE = "<workspace-id>"

SOURCES = {
    "apps": ("meridian-apps", "Meridian Lending applications: loan-core (Java), loan-db (SQL, Liquibase), "
                              "loan-portal (TypeScript), notify-svc (Python), platform (infra, contracts, docs)."),
    "mainframe": ("meridian-mainframe", "Meridian Lending mainframe: loan-batch (COBOL batch, CICS, JCL, copybooks)."),
}

VARIANTS = {
    "standard": {"name": "Meridian Lending Standard", "project": "meridian-std",
                 "recipe": "the standard recipe, unchanged (stage 07 detects D01 and D04 only)"},
    "wide": {"name": "Meridian Lending Wide", "project": "meridian-wide",
             "recipe": "the standard recipe with stage 07 widened to every dimension that has a detector"},
}

# Every dimension stage 07 has a detector for. D14, D20, D21 have none, and the recipe refuses a kind with no detector.
ALL_DETECTED = ["D01", "D02", "D03", "D04", "D05", "D06", "D07", "D08", "D09", "D10", "D11", "D12", "D13",
                "D15", "D16", "D17", "D18", "D19"]
STANDARD = "msock/standard-recipe 2026.10.46"

TEAM_NOTE = ("Month-end: ACCRUE01 must finish before STMTGEN1 starts. When the accrual overruns past 05:00 on the "
             "last day of the month, operators restart from STEP020 and the statements show the previous day's "
             "interest. Happened in March and June. — mainframe-ops\n")


def zip_of(folder: Path, readme: str) -> bytes:
    files = sorted(p for p in folder.rglob("*") if p.is_file() and "__pycache__" not in p.parts)
    buf = io.BytesIO()
    with zipfile.ZipFile(buf, "w", zipfile.ZIP_DEFLATED, compresslevel=9) as z:
        # A root file keeps the repo folders as the top level: push_source.py strips a lone top folder.
        info = zipfile.ZipInfo("README.md", EPOCH)
        info.external_attr = 0o100644 << 16
        z.writestr(info, readme, zipfile.ZIP_DEFLATED)
        for p in files:
            info = zipfile.ZipInfo(p.relative_to(folder).as_posix(), EPOCH)
            info.external_attr = 0o100644 << 16
            z.writestr(info, p.read_bytes(), zipfile.ZIP_DEFLATED)
    return buf.getvalue()


def check(data: bytes, label: str) -> int:
    """push_source.py's rules: zip magic, safe paths, under 5,000 files and 20 MB, no traps; plus our 500 KB."""
    assert data[:4] == b"PK\x03\x04", label
    assert len(data) <= LIMIT, f"{label}: {len(data)} bytes, over 500 KB"
    with zipfile.ZipFile(io.BytesIO(data)) as z:
        names = [i.filename for i in z.infolist() if not i.is_dir()]
        assert len(names) < 5000, label
        assert sum(i.file_size for i in z.infolist()) < 20 * 1024 * 1024, label
        for n in names:
            parts = n.split("/")
            assert not n.startswith("/") and ".." not in parts and ":" not in parts[0], f"{label}: unsafe {n}"
            body = z.read(n)
            hit = [t for t in TRAPS if t in body]
            assert not hit, f"{label}: {n} holds a testbed trap"
        tops = {n.split("/", 1)[0] for n in names}
        assert len(tops) > 1, f"{label}: one top folder would be stripped"
    return len(names)


def push_md(v: str, cfg: dict, ids: dict) -> str:
    rows = "\n".join(f"| `{SOURCES[k][0]}.zip` | `{ids[k]}` |" for k in SOURCES)
    cmds = "\n".join(
        f"python3 testbed/push_source.py ../synthetic-estate/{v}/{SOURCES[k][0]}.zip \\\n"
        f"    --workspace {WORKSPACE} --project {cfg['project']} --source-id {ids[k]} \\\n"
        f"    --name \"{cfg['name']}\" --apply"
        for k in SOURCES)
    recipe = "" if v == "standard" else f"""
## 2. Widen the recipe (before starting the build)

`recipe-wide.json` is the product recipe body. As the Builder, from collection step **C**:

1. `POST /v1/projects/{cfg['project']}/recipe/resolve` with `{{"product": <recipe-wide.json>}}`: checks it, changes nothing.
2. `POST /v1/projects/{cfg['project']}/recipe` with the same body and an `Idempotency-Key`: saves it as version 2.
3. As the Approver: `POST /v1/projects/{cfg['project']}/recipe/2/activate`.

The only change: stage 07 `synthesis_scope.kinds` from `[D01, D04]` to all 18 dimensions that have a detector.
D14, D20 and D21 have no stage-07 detector (the recipe refuses them); they come from stages 04, 05, 09 and 10.
Expect a higher scope estimate at G1 than the standard SOCK's: every kind adds a detector and its deep write-ups.
"""
    build_step = "2" if v == "standard" else "3"
    return f"""# {cfg['name']}

Project `{cfg['project']}` in workspace `{WORKSPACE}`. Recipe: {cfg['recipe']}.

The same code as the other variant (`{'wide' if v == 'standard' else 'standard'}/`), so answers compare row for row.
Score against `../VALIDATION.md` (kept out of the zips).

| Zip | Source id |
|---|---|
{rows}

## 1. Push both sources (from `msock-testbed/`)

The first push makes the SOCK, as the builder in `tokens.txt`; the second adds to it.
Drop `--apply` for a dry run first.

```
{cmds}
```

Then add the team note (`../team-note.txt`) on the SOCK in the console, as a tacit note from mainframe-ops.
{recipe}
## {build_step}. Build

Start the build (collection step **D**), sign off G1 as the Approver (step **E**), then watch the stages.
{"With the standard recipe, D02 to D19 at stage 07 are expected to show `held_by_policy`: score those rows *held*, not *Fail*." if v == "standard" else "Every dimension with a detector should now have stage-07 surfaces; a gap is a real finding."}
"""


def main() -> None:
    zips = {k: zip_of(SRC / folder, f"# {folder}\n\n{desc}\n") for k, (folder, desc) in SOURCES.items()}
    counts = {k: check(z, k) for k, z in zips.items()}
    for v, cfg in VARIANTS.items():
        out = HERE / v
        out.mkdir(exist_ok=True)
        ids = {k: f"{cfg['project']}-{k}" for k in SOURCES}
        for k, (folder, _) in SOURCES.items():
            (out / f"{folder}.zip").write_bytes(zips[k])
        (out / "README.md").write_text(push_md(v, cfg, ids))
        if v == "wide":
            recipe = {"recipe": cfg["project"], "version": 2, "extends": STANDARD,
                      "changes": [{"stage": "07 Cross Source discovery",
                                   "set": {"synthesis_scope.kinds": ALL_DETECTED}}]}
            (out / "recipe-wide.json").write_text(json.dumps(recipe, indent=2) + "\n")
    (HERE / "team-note.txt").write_text(TEAM_NOTE)
    for k, z in zips.items():
        print(f"{SOURCES[k][0]}.zip: {counts[k]} files, {len(z):,} bytes "
              f"({len(z) * 100 // LIMIT}% of 500 KB), sha256 {hashlib.sha256(z).hexdigest()[:12]}")
    print("wrote", ", ".join(sorted(str(p.relative_to(HERE)) for v in VARIANTS for p in (HERE / v).iterdir())))


if __name__ == "__main__":
    sys.exit(main())
