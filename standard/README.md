# Meridian Lending Standard

Project `meridian-std` in workspace `<workspace-id>`. Recipe: the standard recipe, unchanged (stage 07 detects D01 and D04 only).

The same code as the other variant (`wide/`), so answers compare row for row.
Score against `../VALIDATION.md` (kept out of the zips).

| Zip | Source id |
|---|---|
| `meridian-apps.zip` | `meridian-std-apps` |
| `meridian-mainframe.zip` | `meridian-std-mainframe` |

## 1. Push both sources (from `msock-testbed/`)

The first push makes the SOCK, as the builder in `tokens.txt`; the second adds to it.
Drop `--apply` for a dry run first.

```
python3 testbed/push_source.py ../synthetic-estate/standard/meridian-apps.zip \
    --workspace <workspace-id> --project meridian-std --source-id meridian-std-apps \
    --name "Meridian Lending Standard" --apply
python3 testbed/push_source.py ../synthetic-estate/standard/meridian-mainframe.zip \
    --workspace <workspace-id> --project meridian-std --source-id meridian-std-mainframe \
    --name "Meridian Lending Standard" --apply
```

Then add the team note (`../team-note.txt`) on the SOCK in the console, as a tacit note from mainframe-ops.

## 2. Build

Start the build (collection step **D**), sign off G1 as the Approver (step **E**), then watch the stages.
With the standard recipe, D02 to D19 at stage 07 are expected to show `held_by_policy`: score those rows *held*, not *Fail*.
