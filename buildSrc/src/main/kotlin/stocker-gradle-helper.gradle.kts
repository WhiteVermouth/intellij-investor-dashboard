import com.vermouthx.stocker.gradle.StockerCopyChangelogTask
import com.vermouthx.stocker.gradle.StockerCopyReadmeTask
import com.vermouthx.stocker.gradle.StockerCreateDirectoryTask
import com.vermouthx.stocker.gradle.StockerPatchHtmlTask

tasks.register<StockerCreateDirectoryTask>("createDirectory")
tasks.register<StockerCopyChangelogTask>("copyChangelog")
tasks.register<StockerCopyReadmeTask>("copyReadme")
tasks.register<StockerPatchHtmlTask>("patchHtml")