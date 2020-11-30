import com.vermouthx.stocker.gradle.StockerCopyChangelogTask
import com.vermouthx.stocker.gradle.StockerCopyReadmeTask
import com.vermouthx.stocker.gradle.StockerPatchHtmlTask

tasks.register<StockerPatchHtmlTask>("patchHtml")
tasks.register<StockerCopyChangelogTask>("copyChangelog")
tasks.register<StockerCopyReadmeTask>("copyReadme")