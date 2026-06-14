# CLAUDE.md

Guidance for Claude Code when working in this repository.

The full contributor guide is the single source of truth — read it first:

@AGENTS.md

The sections below are a Claude-oriented quick reference; if anything here ever
conflicts with `AGENTS.md`, `AGENTS.md` wins, and the drifted section here
should be fixed.

## What this is

`Stocker` — a JetBrains IDE plugin (`com.vermouthx.intellij-investor-dashboard`)
that shows a real-time stock/crypto watchlist (A-shares, HK, US, crypto) in a
tool window. Mixed Kotlin (logic/UI wiring) + Java (table rendering/listeners),
built on the IntelliJ Platform Gradle plugin, targeting platform `2025.3`.

## Common commands

| Task | Command |
| --- | --- |
| Compile (fast default check) | `./gradlew compileKotlin compileJava` |
| Run unit tests | `./gradlew test` |
| Full build (packaging changes) | `./gradlew build` |
| Plugin compatibility verification | `./gradlew verifyPlugin` |

Toolchain: JDK 21, Gradle wrapper 9.5.0. Always use the wrapper (`./gradlew`).

## How the plugin works (data flow)

- `StockerApp` runs one consolidated `ScheduledExecutorService` task per project
  on the configured refresh interval; it fetches favorites + indices for all
  markets and publishes results over the IntelliJ **message bus**.
- `StockerQuoteHttpUtil` builds provider URLs (Sina / Tencent) →
  `StockerQuoteParser` parses the text response into `StockerQuote`.
- Per-market tool-window tabs subscribe to update/delete/reload topics; the Java
  listeners apply cell-level diffs to the Swing `StockerTableModel`.
- `StockerSetting` is an application-level `PersistentStateComponent`
  (`stocker-config.xml`) holding watchlists, custom names, cost prices,
  holdings, color pattern, visible columns, and refresh interval.

## Conventions that matter most when editing

- **Preserve the Kotlin/Java split.** Don't migrate Java table/view/listener
  classes to Kotlin unless explicitly asked.
- **User-visible text** belongs in `src/main/resources/messages/StockerBundle*.properties`,
  not hardcoded — and keep the `zh_CN` bundle in sync.
- **Version bumps touch three files together:** `gradle.properties`,
  `CHANGELOG.md`, and `notifications/StockerNotification.kt`.
- **Tool-window/table/popup changes** require reviewing both the UI event side
  (`views`/`components`) and the message-bus listeners (`listeners`).
- **Plugin wiring changes** (actions, startup, services, notification groups)
  must be reflected in `src/main/resources/META-INF/plugin.xml`.

## Creating a release

Releases are **tag-driven**: bump the version on `master`, push a `v*` tag, and
CI (`.github/workflows/build.yml`) builds the plugin, creates the GitHub Release,
and publishes to the JetBrains Marketplace. See `AGENTS.md` → "Release And
Versioning" for the canonical rules.

1. **Bump the version in three files together** (keep them consistent):
   - `gradle.properties` → `pluginVersion` (the source of truth).
   - `CHANGELOG.md` → add a new `## X.Y.Z` section. Match the existing format:
     emoji-prefixed category headings (`### ✨ New Features`, `### 🐛 Bug Fixes`,
     `### 🔧 Maintenance`, …) with **bilingual** `English / 中文` entries. The
     Marketplace change notes are auto-derived from this file's latest entry by
     the `org.jetbrains.changelog` plugin — no manual copy needed.
   - `notifications/StockerNotification.kt` → update `buildReleaseNote()` to
     describe the new version, editing **both** the `zh_CN` and English HTML
     blocks. (This is the in-IDE popup shown on upgrade; the version string
     itself comes from `StockerMeta`, so only the prose needs changing.)
2. **Verify** before tagging:
   - `./gradlew test` and `./gradlew build` (and `./gradlew verifyPlugin` for
     compatibility).
3. **Commit, tag, and push.** Use a `vX.Y.Z` tag — CI's release job only runs
   for refs matching `v1.*`:
   - `git commit -am "🔖 Release version X.Y.Z: …"`
   - `git tag vX.Y.Z && git push origin master --tags`
4. **CI does the rest** on the tag: `buildPlugin` → GitHub Release with the
   `.zip` artifact → `publishPlugin` to the Marketplace (uses the
   `JETBRAINS_TOKEN` repo secret). To publish manually if needed:
   `./gradlew publishPlugin -Djetbrains.token=<token>`.

## Testing notes

- Tests are plain JUnit 5 with no IntelliJ test fixtures, so only
  **platform-free** logic is testable today (nothing touching
  `ApplicationManager`/services, the message bus, or localized `StockerBundle`
  titles). See the "Testing" section of `AGENTS.md` for the full picture.
- `StockerQuoteParser` parses by hard-coded field indices against undocumented
  provider formats — treat its tests as the contract and update fixtures in
  lockstep with any parser change.
