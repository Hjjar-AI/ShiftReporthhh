# Architecture

[Implementation/limits](../status/current-state.md) · [Protocol](telegram-protocol.md) · [Roadmap](../status/roadmap.md)

## Accepted synchronization scope

Manually retrieved/published complete Telegram snapshots and reviewed reports; last successful pinned write wins. No automatic sync/remote merge/atomic multi-writer guarantee/required backend. Clinical patient/shift saves are last-save-wins; retained revision columns support bookkeeping, not stale-edit rejection. Review/authority protect publication and privileged registry mutations, not competing remote writes. Timestamps record known events, not freshness. Patient publication retains clinical/registry/delivery guards. A single explicit review may include pending doctor bytes and patients; admin publishes doctors first. Unreviewed later edits remain pending.

## Ownership and local commits

Publications contain the current ward only; older multi-list CSV containers remain readable, with Fetch selecting the newest list. No dated-list picker/copy or previous-publication local-view workflow. Keep device-local Fetch Undo and reviewed admin rollback of the prior remote publication. Existing stored clinical rows are preserved.

08:30 only labels the work day: no automatic new ward/rollover or date-based edit/publish/send gate. Existing ward identities/rows continue across days; captured report dates remain immutable after review.

Screens own presentation/navigation; ViewModels own state/mutations and delegate. Repositories atomically commit clinical changes; `KnownShiftSnapshot`/`PatientPublicationStatus` derive current-ward pending/count/briefing from the same normalized content/order, excluding retired tracking/task metadata; historical wards remain stored without blocking current fetch, and a missing baseline alone does not make an empty roster-free ward pending; new audit recording and clinical-history UI/loads are disabled, with old records retained; patient/doctor sync share singleton store/mutex. Extraction must not duplicate sessions/reviews/artifacts/drafts/locks; desktop facades preserve imports and defer optional cyclic imports. Blocking work off-main; cancellation separate.

Unknown or newer pinned state fails closed rather than becoming an empty state. Immutable doctor wire decodes are cached, while local alias normalization and live actor/authority checks remain fresh. Reviewed publication resolves pending-fetch choices directly; no redundant self-download or new Undo. Snapshot lineage remains bookkeeping/history, and patient revisions also drive saved-card reveal.

Privacy/PIN removes clinical composition but retains signed-in navigation/ViewModels and `SaveableStateHolder`: stable ActivityResult keys require the holder, not just controller. Sign-out discards both; credentials never saveable.

## Access and project boundaries

Protected-file join; manual configuration at creation. Pre-login reconnect validates before replacing credentials, preserves clinical/key/topics and blocks cross-project changes. Sign-out retains enrollment; local PIN supports offline return, first enrollment/shared privileges retain identity checks. BotFather revokes; token holders can bypass client gates.

Android pin override binds reviewed pin/config/live first-admin authority and rechecks before seeding; import only newly seeded reference. No old-data import/history deletion/automatic device transfer/initialized-local replacement; pin checks non-atomic. Desktop requires private challenge/live group admin/empty pin+workspace, with encrypted UNKNOWN before writes. Raw desktop settings stay private. Optional authenticated encryption protects keys, while PDF/metadata stays readable; missing device keys preserve files. Admin checks below UI. [Setup](../guides/telegram-setup.md).

## Clinical and presentation boundaries

Plain checklist tasks; legacy owner/deadline/priority fields remain compatible wire/storage only. No new completion/editor identity or time stamps; old fields remain readable. Teams independent of patient cover; CSV retains omitted active doctors. Backup/restore never publishes; transactional live responsible-doctor role checks protect Save/recycle/replacement. Restore advances revisions; rejection rolls back clinical/pending state.

Shared fields/tokens standardize presentation; callers own drafts/validation/busy/selection/review. RTL/growing48dp/adaptive layouts, clinical-first edit/demographic-first create, labs and fixed keyboard-safe actions. Appearance exposes theme/text size/spacing/reduced motion with consistent remaining defaults; A4/A5 remain. Appearance/accent/severity/PDF independent; saved text unchanged. Brief individual/immediate bulk or reduced motion. Stable visible IDs define rounds; buttons supplement gestures. [UI guide](../guides/android-ui.md).

## Reviewed PDF and delivery identity

Android review captures CSV/text content and exact opened PDF/options/SHA-256; subsequent local edits remain pending, rather than invalidating approved content. Live actor/authority/assignment/destination checks remain; missing/changed PDF file requires preview. Publish before delivery, including subsets. UNKNOWN before report network writes blocks shift/destination retry until authorized resolution or a one-use permit for the same exact artifact. The uncertain receipt remains evidence after extra-copy permission. A pending copy permit restricts that route to the approved content/format key (including its legacy alias). Consuming it durably restores the original UNKNOWN until the copy is acknowledged; failure/interruption leaves the original actionable. Received/not-received resolution is available in delivery history before using the permit and revokes it. In-flight copy/original records cannot be resolved concurrently. Local receipts cannot prove non-delivery or cross-device exactly-once.

Android delivery identity uses an explicit versioned field list and a legacy-key compatibility check for existing receipts. A reviewed report keeps its captured label date for text/PDF day counts. Definite Telegram rejections before any delivered chunk are NOT_SENT; partial delivery stays uncertain. Receipts are not automatically pruned: removing UNKNOWN loses unresolved evidence, while removing SENT can permit duplicates. Supervisor receipt message IDs remain separate from General-chat sent pointers.

Android/desktop A5 cards share canvas/fonts/sections and full-page links preserving headers. Full clinical pagination remains; admin A4 names-only labels historical current-roster limits. Export/open is separate from sending authority.

## Isolation and platform choices

Demo injects memory-only state into real forms/cards/renderer; explicit every-page-labelled PDF only, reset/exit discards, no production store/session/transport. [Demo](../guides/android-demo.md).

Desktop fingerprint-bound AES-GCM before SQLite, DPAPI/Linux-passphrase keys and platform path/lock/PDF adapters. Logical Arabic input/caret/clipboard/Save separate from shaping; unfinished drafts/cached enrollment/shared authority distinct. [Operations](../../desktop/README.md).

Design basis: Android adaptive/Compose accessibility, I-PASS,NHS warnings,WCAG2.2; rationale, not verification. Voice/templates remain deferred in Roadmap; unselected UI stays in [decisions](../decisions/android-ui-options.md).
