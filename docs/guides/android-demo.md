# Android demo workspace

Choose Demo on setup to use the app screens with isolated temporary data, without joining or creating a project.

## Shared screens

| Destination | Shared behavior |
|---|---|
| Patients/Dashboard | Production `WardWorkspace`: navigation, search/filter/cards/details/forms/tasks/sort/dashboard, including simulated transfer controls and canonical pending counts. |
| Doctors | Production `DoctorsWorkspace`: search/filter/cards/Add/Edit/delete/team management and registry review. Registry Publish/Fetch are memory-only; CSV import/export are disabled. |
| Report | Production `ReportWorkspace`: shift doctors, General/supervisor selection, PDF/text preview, readiness links, final confirmation and receipt display. Publish/send only update the in-memory snapshot/receipts. |
| Appearance | Production `SettingsAppearanceSection`, palette/font/reduced-motion controls and patient preview; preferences stay temporary. Connection/security/project administration is outside the demo. |
| Drawer | Production `WardDrawer` with a demo-only session slot for actor selection, download Undo, reset and exit. |

The ward, report and doctor screens display demo identification. Report screens explicitly state that transfer/delivery is simulated. Shared UI changes apply automatically in both modes; sample scenarios and demo adapters still need updates when action contracts change.

## Isolation and lifecycle

`DemoWardViewModel` injects `InMemoryDemoWardRepository`, pure text/PDF builders and an explicit PDF saver. It never constructs production clinical/settings/session/Telegram services. `WardController`, `ReportController` and `DoctorsController` select production or demo operations below the shared UI; production authorization and durable delivery rules stay in their existing ViewModels/services.

Beverage patients/tree doctors, actor/roster/teams, drafts, fake remote snapshot and delivery receipts exist only in memory. Entered doctor PIN/Telegram identity values are discarded. Fetch uses the fake published snapshot; pending replacement uses the production chooser/review dialogs, with local demo Undo. Reset/exit/process death discards the session. The day label refreshes at08:30/foreground; report review captures its own day.

## Explicit local files

PDF preview uses the production renderer with **DEMO on every page**, including Cards. Save/Share copies the opened general preview; supervisor Save requires each supervisor preview to have been opened. PDF generation/copy runs off-main; private previews are removed on invalidation/reset/exit/ViewModel disposal. User-saved copies remain. No CSV exports, real assignment/admin exports, real upload or Telegram delivery.

## Source ownership

- [Coordinator](../../android/app/src/main/java/com/hos/rushdpatients/ui/demo/DemoWardScreen.kt) and [drawer adapter](../../android/app/src/main/java/com/hos/rushdpatients/ui/demo/DemoNavigation.kt): session navigation, appearance and reset/exit.
- [Ward adapter](../../android/app/src/main/java/com/hos/rushdpatients/ui/demo/DemoWardViewModel.kt), [report adapter](../../android/app/src/main/java/com/hos/rushdpatients/ui/demo/DemoReportController.kt), [doctor adapter](../../android/app/src/main/java/com/hos/rushdpatients/ui/demo/DemoDoctorsController.kt).
- [Memory repository](../../android/app/src/main/java/com/hos/rushdpatients/ui/demo/InMemoryDemoWardRepository.kt) and [dummy data](../../android/app/src/main/java/com/hos/rushdpatients/ui/demo/DemoData.kt).

## Evidence

Source completion does not establish runtime isolation, device UI or every-page PDF acceptance. [Current evidence](../status/current-state.md#verification-and-limits) · [Remaining checks](../status/testing-handoff.md).
