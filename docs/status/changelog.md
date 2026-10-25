# Completed work

[Current behavior/evidence](current-state.md) · [Remaining implementation](roadmap.md) · [Checks](testing-handoff.md). Source completion ≠ runtime verification; superseded intermediate designs omitted.

## 25 October 2026

- **Recovery:** upload/fetch/CSV/Undo busy cleanup and cancellation/error handling; cleanup before follow-up fetch. Local reload failure retains read-only list/Retry; initial/missing-ward errors remain full-screen. Settings keeps workspace scroll/Advanced state; current-workspace search no longer resets it.
- **UI:** themed responsive splash preserves emblem/credits/timer/hold/tap; grouped dashboard, quieter neutral cards, shared spacing/empty states, direct Recycle Bin/Admin, operation-specific progress and Back to top. Existing saved-patient reveal/highlight reused.
- **Editors:** shared full-screen sections/equal paired fields; clinical-first Edit/demographics-first Add; plain multiline fields and retained old drafts; inline tasks including unfinished text; explicit Save/Save and next, optional profiles/neutral unnamed labels. Retired persistent entry mode/custom doctor-title display; legacy columns retained.
- **Clinical simplification:** last-save-wins, no new clinical revision prompts/audit/editor/task-completion stamps. Plain task text/done, grouped board/2-second completion/Undo; readiness links and priority Undo. Legacy metadata retained without new controls/counters.
- **Continuous ward:**08:30 day label only, no rollover/date gate. Retired dated-list browsing/copy/local previous-publication viewing; current-ward publication, old multi-list decoding/newest fetch, stored rows/Undo/admin rollback preserved.
- **Reports/settings:** combined label «رفع للشبكة وإرسال تقرير»; short final confirmation; retired announcements/history loads. Preview-byte Save/Share under Export, explicit supervisor batches/names-only sheets, Start empty under Advanced. A4/A5 portrait/light/Medium separators; old values ignored. Four sort presets, View Options owns cards; simplified appearance. General-send local-copy coupling remains unchanged/unselected.
- **Network/auth:**25second retry budget, definite429→NOT_SENT versus partial UNKNOWN; explicit back-online fetch. Persisted5/15/60-minute PIN escalation/five-minute default lock and neutral foreground cover.
- **Build fixes:** missing `dp`, obsolete DAO revision argument, invalid LABS enum references and unused optional-fields argument. Owner had successful earlier installs; later Kotlin passed but javac hit orphan announcement Hilt output. Agent did not clean/build; latest source uncompiled.
- **Checks:** applicable production Python3.8 grammar/XML/string/delimiter/diff checks only; no new suite/device/live runs or schema/toolchain changes.

## Earlier retained implementation

- Protected join/encryption/reconnect, PIN/biometrics/live authority, reviewed existing-pin Android creation,200% fixed-action warning; automatic lock-screen biometric prompt and preview-open state survive intended lifecycle boundaries.
- Captured doctor/patient publication, recoverable fetch/Undo, live-admin prior-publication rollback/older-remote override, bounded cache, pending counts and durable exact-artifact UNKNOWN/extra-copy controls.
- Doctor Publish/Fetch/CSV Import and full-screen roster/team/import reviews; atomic current-roster detachment on safe deletion. Stable details dialog/Previous-Edit-Next footer/per-patient scroll; card long-press Edit; saved drafts/picker keys/privacy owners.
- Desktop standalone Windows/Linux encrypted workflow, logical Arabic input, local PIN, reviewed administration/recovery, demo, retained widgets/shortcuts; visible connection/login/destinations and adjacent join/env discovery.
- A5 shared geometry/fonts/sections, RTL column-first index/full-page links, conditional two-column treatment and complete pagination. Current artifact counts/hashes in `samples/artifact-notes.json`; superseded page-count chronology removed.
- Selected O2/O6/O10/O11/O13/O16 and production-preview part of O23 implemented; automatic biometric O20 also implemented. Remaining proposals stay in Decisions.
- Historical full JVM/device/desktop/interoperability/PDF/live receipts and exact snapshot limits live only in [Current state](current-state.md#verification-and-limits). Existing task-related tests were updated in earlier authorized work, not rerun for later simplification.

## Documentation consolidation

Repository-owned Markdown reviewed; repeated behavior/history/checklists consolidated, stale retired-feature descriptions corrected against production source. AGENTS compacted without removing permission/safety/product constraints; command blocks and private originals retained. Plan: [documentation-plan.md](documentation-plan.md). Local audit: `local/verification/documentation-2026-10-25/`. Public documentation reduced about32% by Unicode characters; AGENTS about26% (not measured model tokens). All30 public documents pass local link/anchor checks;45 fenced blocks preserved against HEAD;10 private/archive documents unchanged. Completed-only usability plan removed; O20 moved out of proposals after source confirmation. No builds/tests/source changes; external URLs not checked.
