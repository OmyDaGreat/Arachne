package xyz.malefic.arachne.assets

import kotlinx.browser.document
import org.w3c.dom.HTMLImageElement

data class LoadingProgress(
    val loaded: Int,
    val total: Int,
    val currentAsset: String = "",
) {
    val progress: Float get() = if (total > 0) loaded.toFloat() / total else 0f
    val isComplete: Boolean get() = loaded >= total
}

object AssetManager {
    private val textures = mutableMapOf<String, HTMLImageElement>()
    private val loading = mutableSetOf<String>()
    private var totalAssets = 0
    private var loadedAssets = 0

    fun loadTexture(
        url: String,
        onComplete: ((HTMLImageElement) -> Unit)? = null,
    ) {
        if (textures.containsKey(url)) {
            onComplete?.invoke(textures[url]!!)
            return
        }

        if (loading.contains(url)) return

        loading.add(url)
        totalAssets++

        val img = document.createElement("img") as HTMLImageElement
        img.onload = {
            textures[url] = img
            loading.remove(url)
            loadedAssets++
            onComplete?.invoke(img)
            null
        }
        img.onerror = { _, _, _, _, _ ->
            console.error("Failed to load texture: $url")
            loading.remove(url)
            loadedAssets++
            null
        }
        img.src = url
    }

    fun loadTextures(
        urls: List<String>,
        onProgress: ((LoadingProgress) -> Unit)? = null,
        onComplete: (() -> Unit)? = null,
    ) {
        var completed = 0
        val total = urls.size

        urls.forEach { url ->
            loadTexture(url) {
                completed++
                onProgress?.invoke(LoadingProgress(completed, total, url))
                if (completed == total) {
                    onComplete?.invoke()
                }
            }
        }
    }

    fun getTexture(url: String) = textures[url]

    fun hasTexture(url: String) = textures.containsKey(url)

    fun isLoading(url: String) = loading.contains(url)

    fun getLoadingProgress() = LoadingProgress(loadedAssets, totalAssets)

    fun clear() {
        textures.clear()
        loading.clear()
        totalAssets = 0
        loadedAssets = 0
    }

    fun unloadTexture(url: String) {
        textures.remove(url)
    }
}
