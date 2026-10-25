# Current implementation

[Index](../README.md) · [Architecture](../reference/architecture.md) · [Protocol](../reference/telegram-protocol.md) · [Roadmap](roadmap.md) · [Checks](testing-handoff.md)

## Product and source layout

Arabic offline-first Android/desktop handover; manually reviewed Telegram snapshots/reports. Android: Kotlin1.9/Java17 target, Compose BOM2023.10.01, SDK34/min23, Compose→Hilt ViewModels→domain/repositories/Room-SQLCipher. Desktop: Python/CustomTkinter. Transport and shared wire rules: [protocol](../reference/telegram-protocol.md).

## Security and project access

- Password-protected file-only join; manual configuration at creation. Optional authenticated Telegram encryption; PDF/captions readable. Pre-login reconnect validates same project/bot/topics/key before replacing token, retaining clinical/pending state. BotFather alone revokes tokens.
- Android existing-pin creation binds review/config/live first-admin authority and rechecks before seeding; preserves history, imports no old data, cannot replace initialized enrollment. Desktop creation requires empty pin/workspace; both remain non-atomic.
- First enrollment requires Telegram; local clinician PIN supports offline return. Automatic biometric prompt after foreground lock decision, manual retry/PIN fallback/opt-out; cancellation cannot unlock. Re-lock uses persisted5/15/60-minute escalation; successful PIN resets it. Unset auto-lock5minutes, neutral emblem cover during evaluation.
- Admin/identity checks below UI; secrets excluded from saved UI/logs/exports. Missing DB key preserves DB/WAL/SHM/journals; IO check precedes repositories. `FLAG_SECURE` excludes external PDFs; private previews clean best-effort. Clipboard matching-nonce expiry respects replacement clips, subject to background limits.
- Selected shared debug signer supported; second-account update unverified. Production key/certificate still required; no debug fallback. [Signing](../guides/android-build.md).

## Synchronization and publication

Complete-snapshot retrieve→edit→review→publish; last successful pinned write wins. No automatic sync/merge/atomic collaboration/backend; timestamps describe known events, not current remote freshness. `SyncService`/`DoctorSyncCoordinator` share `RemoteSyncStateStore`/mutex.

Continuous ward across days:08:30 labels the displayed/report day only. No automatic replacement/rollover/edit lock/deadline. Review captures its report date independently of stored snapshot identity. New publication contains one current ward; old multi-list CSV reads newest. No dated-list browsing/copy/local previous-publication viewing; stored rows remain.

Fetch with pending changes offers reviewed doctor→patient publication then fetch, explicit replacement with local Undo, or Cancel. Patient-only fetch retains pending doctors; invalid references fail. Older-than-known CSV needs live-admin confirmation. Admin rollback republishes the exact previous remote CSV without changing local rows, which remain pending. Prior publication retained; contact/snapshot cache bounded to12 plus transferred ward, without deleting clinical rows/Undo/audit/receipts.

Reviewed publication captures patient/roster/order/portable doctor bytes; pending registry publishes first under live admin. Later edits stay pending. Unrelated registry changes need not block valid patient publication. Live roles/assignments/protected ranks/pointers and exact artifacts/destinations remain guarded.

Ward shows pending count/publication status; dashboard distinguishes confirmed/uncertain/unavailable/unrecorded device-local delivery. UNKNOWN offers received/not received/one exact extra copy; no automatic resend. Telegram inline waits capped at25seconds;429 before any sent chunk→NOT_SENT, partial delivery remains uncertain. INTERNET capability suffices; offline failure offers explicit back-online fetch.

Upload/fetch/CSV/Undo clean busy state after cancellation/errors; upload cleanup precedes follow-up fetch. Local reload/errors retain the loaded list read-only with inline Retry; initial/missing-ward errors remain full-screen. Mutation entry guard blocks until local recovery. Network fetch errors retain separate typed recovery.

Sync Center groups patient/doctor status, Files/Backup/Reset under Data; Appearance includes workflow, Project includes invitations/destinations. Admin publication history replaces clinical Activity/audit UI. Doctor Publish/Fetch/CSV Import are direct; reviewed full-screen registry/team/roster flows retain fixed actions/errors. Explicit doctor deletion detaches current roster atomically and marks pending, preserving clinical/team/admin guards and older rosters. Permanent-admin Start empty retains accounts/config/key/receipts/old audit/Telegram history.

## Patients and tasks

Optional patient/doctor text/date profiles; selected values/IDs/ages/assignments remain validated, new account PIN required. Blank names stay blank in storage with neutral identity-based labels; patient labels use admission/ordinal, doctors retain distinct IDs. Older mandatory-field clients may reject blanks; update clients together.

Clinical last Save wins; no clinical revision prompt/new audit/editor/completion stamps. Legacy fields remain compatible and excluded from pending-equality comparisons. Existence/deletion/shift/assignment checks remain. Teams and patient cover are independent; CSV retains omitted active doctors.

`PatientTasks` owns explicit text/done records, inline in the patient draft, including unfinished new-task text. No owner/deadline/task-priority controls or overdue/unassigned counters; legacy metadata is storage-only. Task board groups by patient, shows completed rows struck through2seconds, then removes them; Undo updates only that task. `ReportReadiness` links patients missing responsible doctors/diagnosis/treatment/follow-up; no edit-time requirement.

Add: demographics/admission/team→clinical/labs→collapsed tasks/options. Edit: clinical/labs→open tasks/options→team→demographics. Shared section cards; paired controls at280dp×font scale with equal widths/heights, otherwise stacked. Plain multiline text preserves old separators/date lines. Fixed keyboard-safe Save/Cancel and explicit Save and next only when successor exists; advances after success. No persistent mode/custom doctor-title display. Encrypted patient-ID drafts survive recreation; clear only on Save/discard. Copy process-death recovery not promised. Field errors clear individually; validity links jump to fields. Move-to-top stores order within priority section; manual/name/admission-duration/workbook presets retain old custom specs until changed.

## Startup and navigation

Themed splash: responsive white emblem, original title/credits/version, grouped credits and reading footer; monotonic5seconds includes loading/hold, quick tap skips, expired hold releases, recreation preserves deadline. Clinical root starts only after dismissal. Window owners manage bars; API23–25 navigation dark.

Patients/Dashboard dock/rail; retired Activity resolves to Patients. Compact drawer has direct Recycle Bin/Admin; combined action «رفع للشبكة وإرسال تقرير» retains review. Privacy removes clinical composition while preserving signed-in NavHost/ViewModels/saveable picker keys; sign-out discards them, never saves secrets.

Patient details use stable Previous/Edit/Next footer/dialog and per-patient saved section/scroll; filtered/grouped order drives rounds. Card tap→details, guarded long press→Edit. Existing saved-patient reveal/highlight/list position retained; Back to top appears at grid index8. Settings search respects roles; workspace-keyed scroll/Advanced state persists, current-workspace selection no longer resets to top.

## Shared Android presentation

Shared tokens/Tajawal400/500/700, RTL/123 digits, wrapping ≥48dp targets, fold-safe panes; scale0.92–2.0. Appearance: theme/text size/normal-or-comfortable spacing/reduced motion; retired overrides normalized, navigation visible/Add compact/header expansion explicit. Card controls only View Options. Quieter neutral borders retain clinical/selection colors; diagnosis neutral. Dashboard attention/totals/sync grouped; shared empty-state panels and operation-specific loading labels. Individual expansion brief, bulk/reduced motion immediate. [Token/component owner](../reference/design-catalog.md).

## Reports and exports

Doctors+Destinations→Preview+Send→final confirmation. Unlimited active roster; General initially selected, Select All/manual supervisors, each route requires matching preview. PDF default unless saved text choice. Final summary: destinations/format/unique selected patients/incomplete patients and complete-snapshot publication note. Reviewed publish precedes route-specific delivery; later local changes stay pending. Exact file/options/SHA-256/opening/live actor/destinations checked; PDF-open evidence lives with ViewModel through privacy/recreation, cleared by invalidation/regeneration. UNKNOWN and partial outcomes remain durable, independent of publication success.

Announcements retired; legacy fields inert, remote history untouched. Export groups ordinary preview-byte Save/Share (no rerender), explicit supervisor files/unassigned group and admin names-only sheets. General PDF send still makes one local copy; decoupling unselected. PDF A4/A5, portrait/light, Medium separators/contrast; old A3/Letter→A4, old dark/landscape/separator overrides ignored. Color/shading remain, uncommon controls Advanced. Clinical/tasks/order retained; routine/unassigned/last-edit labels omitted.

A5 source geometry:420×595pt; side/top/bottom4/12/8mm; body/section/title-nav spacing5/4/2pt; body/heading/name11.5/12/15pt. RTL3-column index fills down then left,28pt cells/14pt short links; whole-page destinations preserve headers. Demographics/admission/team introduction, badges→diagnosis→treatment→follow-up→grouped tasks→labs. Treatment4+ source lines uses2 RTL columns only if height improves,8pt gutter, source boundaries preferred, right then left per page. Full continuations; roster/context and outside page/creation/DEMO footer. Admin names-only A4 uses3×56.7mm team tables, continuous numbering and current-roster historical limits. Matching-client rendering is not pixel identity.

## Isolated demo

Memory repository→real WardWorkspace/forms/cards/renderer; beverage patients/tree doctors, temporary actor/roster/preferences, recycle/reset/exit. No production DB/settings/session/transport. Only explicit PDF export, DEMO every page; process death/reset/exit discards. [Guide](../guides/android-demo.md).

## Standalone desktop

Visible connection/login, Fetch/Review-Publish/Send and destination selector; protected join/valid named env discovery beside source/EXE, configured `.env`/explicit `--env` precedence, ambiguity chooser and env-relative paths. Discovery does not validate credentials. Encrypted SQLite/PIN/idle lock/drafts/recycle/receipts, reviewed creation/registry administration; logical Arabic editing separate from shaping. Linux XDG/passphrase/opener; Windows DPAPI. Retained/debounced widgets, native menus/shortcuts; demo memory-only. [Usage](../../desktop/README.md).

## Clinical write safety

`PatientDoctorAssignments` validates live roles within `WardMutationRepository` transactions for saves/recycle. Backup replacement validates roster/assignments, advances stored revisions and atomically rolls back rows/pending on rejection; restore never publishes. Password arrays wiped on join/restore exits; cancellation propagates. Legacy records remain readable; no migration introduced by these changes.

## Verification and limits

**Latest source is not build/device verified.** Owner will run builds. Latest reported failure: Kotlin passed after LABS fix; javac failed on orphan generated announcement Hilt factory. Clean regeneration was advised, not executed by agent; subsequent UI/recovery edits have source checks only. Earlier missing `dp`, retired DAO argument and four Kotlin errors were corrected; earlier owner installs passed, not evidence for current source.

| Earlier snapshot | Recorded evidence, not current coverage |
|---|---|
| Pre-splash production | Offline `:app:compileDebugKotlin` passed: one worker/in-process Kotlin/2GB, Kotlin+KSP incremental disabled, autoVersion=false; recovery/deletion/forms/reports/auth. Normal incremental unrerun. Logs `local/verification/recovery-doctor-delete-2026-10-24/`. |
| Pre-current card geometry |4 Android PDF +60 desktop cases passed; light/dark fixtures17/8pages,68/15Fit links;40tasks/long text retained,30-patient/3-index-page returns. Sampled baselines matched with glyph-shaping differences. |
| Pre-card full JVM |682passed,0failures/errors,1live-seeder opt-in skip. Inventory then683methods/85classes. |
| Pre-card phone | PPA-LX2/API29:285passed,0failures/errors,3skips (private CSV absent, provider opt-in, fingerprint owner skip); assisted provider pass→286distinct. Inventory then288methods/55classes. |
| Desktop/interoperability |60passed/0skips,4reverse file checks/7selected mutants; Linux demo/Settings/reset and selected every-page-DEMO PDFs passed. Not full GUI/Windows/live certification. |
| Current sample artifact | `samples/artifact-notes.json`:50patients/112tasks,56PDF pages/50first-index links/268valid Fit links. Separate medication-list preview3pages/8links. Older64page/298link treatment receipt is a different snapshot. No current Android matching-render claim. |

Latest source checks: Python3.8 grammar where applicable, values XML/string references/Kotlin delimiter review and Git whitespace; no compilation inference. Evidence: `local/verification/cards-parity-2026-10-24/`, `full-current-2026-10-24/`, `a5-columns-2026-10-24/`, `a5-treatment-columns-2026-10-24/`. Full Android suites not rerun after card changes; unique historical live transport/seeder/fingerprint receipts retained. Private33-doctor CSV passed source validation only; original phone failure remains unreproduced.

Recorded tools: Gradle8.7, Java21(target17),SDK34/offline/one worker/in-process Kotlin/autoVersion=false; desktop existing Python3.14 venv with ReportLab5.0.1/reshaper3.0.1/bidi0.6.11 and successful pip check. No new dependency installation for those runs. Counts/reruns are not coverage percentages; simulated faults/clients/folds/debug200-card samples do not establish two physical phones, release performance or every PDF glyph. [Single remaining-check list](testing-handoff.md). Local pruning manifests retain replacement hashes; private originals/configuration/unique receipts preserved.
