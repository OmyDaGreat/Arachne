package xyz.malefic.arachne.scene

class SceneManager {
    private val scenes = mutableMapOf<String, Scene>()
    private var currentScene: Scene? = null
    private var currentSceneName: String? = null

    fun addScene(
        name: String,
        scene: Scene,
    ) {
        scenes[name] = scene
    }

    fun removeScene(name: String) {
        if (currentSceneName == name) {
            currentScene?.onExit()
            currentScene = null
            currentSceneName = null
        }
        scenes[name]?.onDestroy()
        scenes.remove(name)
    }

    fun loadScene(name: String) {
        val scene =
            scenes[name] ?: run {
                console.error("Scene '$name' not found")
                return
            }

        currentScene?.onExit()
        currentScene = scene
        currentSceneName = name

        if (!scene.active) {
            scene.onCreate()
        }
        scene.onEnter()
    }

    fun getCurrentScene() = currentScene

    fun getCurrentSceneName() = currentSceneName

    fun update(deltaTime: Double) {
        currentScene?.update(deltaTime)
    }

    fun hasScene(name: String) = scenes.containsKey(name)

    fun clear() {
        currentScene?.onExit()
        scenes.values.forEach { it.onDestroy() }
        scenes.clear()
        currentScene = null
        currentSceneName = null
    }
}
