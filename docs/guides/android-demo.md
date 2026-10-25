# Android demo workspace

Choose Demo on setup: real ward UI with isolated temporary data, no project join/create.

## Navigation

| Destination | Behavior |
|---|---|
| Patients/Dashboard | Real compact bar/rail, search/count/filter/cards/details/forms/metrics; options group filters/layout/sort, no demo grouping or sync/admin Activity. |
| Shift doctors | Roster plus tree-named actor; changes My patients, never real login. |
| Report review | Roster first, scope/destination/layout then Advanced; explicit local picker PDF export only, no Telegram publication. |
| Settings | Temporary shared appearance/font controls, dummy preview. |
| Recycle/reset/exit | Drawer actions; confirmed reset restores dummy data, confirmed exit returns to setup/discards session. |

Persistent compact demo strip identifies the workspace.

## Shared behavior

Real cards/fullscreen forms/task editor/details/rounds/roster/sort/dashboard/theme/PDF; one draft including labs. Compact/two-column cards, brief individual/immediate bulk expansion. Stored order drives PDF; filters never drop report patients. Navigation retains viewport/expanded/pinned/per-page state; saved patients reveal/highlight or explain hidden filters. Adaptive/fold-aware labelled controls and report-action clearance.

## Isolation and data

`DemoWardViewModel`: memory-only clinical/roster/actor; no real repository/settings/login/session/Telegram. Beverage patients/tree doctors, plain checklist tasks, valid-value/assignment checks and no new completion attribution.

Explicit PDF only: options survive recreation, cancellation clears pending request; snapshot/file work off-main, cancellation preserved, bytes verified, DEMO every page. Reset/exit clears selection and is blocked during export. Process death starts fresh, never loads clinical state.

## Source organization

- [Screen coordinator](../../android/app/src/main/java/com/hos/rushdpatients/ui/demo/DemoWardScreen.kt): navigation, view state, list and shared form/detail callbacks.
- [Drawer and options](../../android/app/src/main/java/com/hos/rushdpatients/ui/demo/DemoNavigation.kt).
- [Secondary screens](../../android/app/src/main/java/com/hos/rushdpatients/ui/demo/DemoSecondaryScreens.kt): report review, roster/actor selection and local preferences.
- [ViewModel](../../android/app/src/main/java/com/hos/rushdpatients/ui/demo/DemoWardViewModel.kt): memory-only mutations and explicit PDF export.
- [Dummy data](../../android/app/src/main/java/com/hos/rushdpatients/ui/demo/DemoData.kt).

## Verification

Earlier demo checks predate checklist/editor simplification; no current suite/device/PDF acceptance claim. [Results](../status/current-state.md#verification-and-limits) · [Remaining checks](../status/testing-handoff.md).
