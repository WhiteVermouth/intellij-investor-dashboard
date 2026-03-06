# IntelliJ Investor Dashboard Agent Guide

## Project Overview

- This repository contains the `Stocker` JetBrains plugin (`com.vermouthx.intellij-investor-dashboard`).
- It is a mixed Kotlin/Java codebase:
  - Kotlin holds most application logic, actions, settings, dialogs, notifications, and tool window wiring.
  - Java holds table rendering, table view behavior, message-bus listeners, and some utility classes.
- The plugin targets IntelliJ Platform `2024.1` via the `org.jetbrains.intellij.platform` Gradle plugin.

## Repository Layout

- `src/main/kotlin/com/vermouthx/stocker`
  - `actions`: toolbar and Tools menu actions
  - `activities`: startup hooks
  - `notifications`: welcome and release-note notifications
  - `settings`: persistent plugin settings
  - `views/dialogs`, `views/windows`: UI entry points
  - `utils`: HTTP, parser, and helper logic
- `src/main/java/com/vermouthx/stocker`
  - `views`: `StockerTableView`
  - `components`: table models and renderers
  - `listeners`: message-bus listeners for quote update/reload/delete
- `src/main/resources`
  - `META-INF/plugin.xml`: plugin registration and action declarations
  - `messages/*.properties`: localized strings
  - `icons/`: plugin assets

## Working Rules

- Preserve the existing mixed-language structure. Do not move Java table/view classes into Kotlin unless the task explicitly requires a broader refactor.
- Prefer small, surgical fixes. This plugin has a lot of event-driven UI behavior; broad rewrites are risky.
- When changing user-visible text, check whether it belongs in `messages/StockerBundle*.properties` instead of hardcoding it.
- When changing plugin wiring, actions, startup behavior, settings registration, or notification groups, verify `src/main/resources/META-INF/plugin.xml`.
- When changing tool window, table, or popup behavior, review both sides of the flow:
  - UI event handling in `views` / `components`
  - message-bus update/delete/reload listeners in `listeners`
- When changing settings-backed behavior, confirm both persistence and immediate UI refresh behavior.

## Verification

- Default verification for code changes:
  - `./gradlew compileKotlin compileJava`
- For broader plugin or packaging changes, consider:
  - `./gradlew build`
- If the change affects UI behavior, context menus, notifications, actions, or settings application, note whether the fix was only compile-verified or manually exercised in IntelliJ.

## Release And Versioning

- `pluginVersion` lives in `gradle.properties`.
- `build.gradle.kts` uses `CHANGELOG.md` as the source for plugin change notes shown on release.
- `StockerNotification.kt` contains the in-product release note content shown to users after upgrade.
- When bumping to the next plugin version, always update these files together:
  - `gradle.properties`
  - `CHANGELOG.md`
  - `src/main/kotlin/com/vermouthx/stocker/notifications/StockerNotification.kt`

## Common Pitfalls

- Right-click or popup-menu behavior can break if selection/focus changes are not accounted for.
- `DefaultTableModel` already fires table events for some operations; avoid duplicate manual notifications unless required.
- Localization regressions are easy to introduce when labels/descriptions exist in both action declarations and runtime UI code.
- Settings changes should not silently require restart unless the behavior is explicitly designed that way.
