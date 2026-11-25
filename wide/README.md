# Meridian Lending Wide

Project `meridian-wide` in workspace `<workspace-id>`. Recipe: the standard recipe with stage 07 widened to every dimension that has a detector.

The same code as the other variant (`standard/`), so answers compare row for row.
Score against `../VALIDATION.md` (kept out of the zips).

| Zip | Source id |
|---|---|
| `meridian-apps.zip` | `meridian-wide-apps` |
| `meridian-mainframe.zip` | `meridian-wide-mainframe` |

## 1. Push both sources (from `msock-testbed/`)

The first push makes the SOCK, as the builder in `tokens.txt`; the second adds to it.
Drop `--apply` for a dry run first.

```
python3 testbed/push_source.py ../synthetic-estate/wide/meridian-apps.zip \
    --workspace <workspace-id> --project meridian-wide --source-id meridian-wide-apps \
    --name "Meridian Lending Wide" --apply
python3 testbed/push_source.py ../synthetic-estate/wide/meridian-mainframe.zip \
    --workspace <workspace-id> --project meridian-wide --source-id meridian-wide-mainframe \
    --name "Meridian Lending Wide" --apply
```

Then add the team note (`../team-note.txt`) on the SOCK in the console, as a tacit note from mainframe-ops.

## 2. Widen the recipe (before starting the build)

`recipe-wide.json` is the product recipe body. As the Builder, from collection step **C**:

1. `POST /v1/projects/meridian-wide/recipe/resolve` with `{"product": <recipe-wide.json>}`: checks it, changes nothing.
2. `POST /v1/projects/meridian-wide/recipe` with the same body and an `Idempotency-Key`: saves it as version 2.
3. As the Approver: `POST /v1/projects/meridian-wide/recipe/2/activate`.

The only change: stage 07 `synthesis_scope.kinds` from `[D01, D04]` to all 18 dimensions that have a detector.
D14, D20 and D21 have no stage-07 detector (the recipe refuses them); they come from stages 04, 05, 09 and 10.
Expect a higher scope estimate at G1 than the standard SOCK's: every kind adds a detector and its deep write-ups.

## 3. Build

Start the build (collection step **D**), sign off G1 as the Approver (step **E**), then watch the stages.
Every dimension with a detector should now have stage-07 surfaces; a gap is a real finding.
