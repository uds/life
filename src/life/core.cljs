(ns life.core
    (:require [babylonjs :as bb]))

(defn create-cube
  "Create a cube and return it."
  [scene]
  (let [box-mesh (bb/MeshBuilder.CreateBox scene)
        mat (bb/StandardMaterial. "cube-material" scene)]
    (set! mat.diffuseColor (bb/Color3. 1 0 1))
    (set! box-mesh.position (bb/Vector3. 0 0 0))
    (set! box-mesh.scaling (bb/Vector3. 1 1 1))
    (set! box-mesh.material mat)
    (set! box-mesh.showBoundingBox true)
    box-mesh))

(defn create-camera 
  "Create a camera and return it."
  [scene]
  (bb/ArcRotateCamera. "arc-camera" (/ Math/PI 0.3) (/ Math/PI 3) 3 (bb/Vector3. 0 0 0) scene))


(def ^:private babylon-engine nil)

(defn ^:dev/after-load -main
  []
  (let [canvas (.getElementById js/document "renderCanvas")
        engine (bb/Engine. canvas true)
        scene  (bb/Scene. engine)
        camera (create-camera scene)]
    (set! babylon-engine engine)

    (.attachControl camera canvas true) 
    (create-cube scene)

    (let [domeLight (bb/HemisphericLight. "light" (bb/Vector3. 0 1 0))]
      (set! domeLight.intensity 0.9))

    (.runRenderLoop engine #(.render scene))
    (.addEventListener js/window "resize" #(.resize engine))))

;; called by the shadow-cljs hot-loader every time when application about to be reloaded
(defn ^:dev/before-load-async stop [done]
  (.dispose babylon-engine)
  (done))
