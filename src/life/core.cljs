(ns life.core
  (:require [babylonjs :as bb]
            [life.view :as view]))


(def ^:private !babylon-engine (atom nil))
(def ^:private !board (atom nil))


(def ^:private blinker-pattern
  [[0 0] [0 1] [0 2]])

(def ^:private toad-pattern
  [[0 0] [0 1] [0 2] [1 -1] [1 0] [1 1]])

(def ^:private glider-pattern
  [[0 0] [0 1] [0 2] [1 0] [2 1]])

(def ^:private gosper-glider-gun-pattern
  '((5 1) (5 2) (6 1) (6 2) (5 11) (6 11) (7 11) (4 12) (3 13) (3 14) (8 12) (9 13) (9 14) (6 15) (4 16) (5 17) (6 17) (7 17) (6 18) (8 16) (3 21) (4 21) (5 21) (3 22) (4 22) (5 22) (2 23) (6 23) (1 25) (2 25) (6 25) (7 25) (3 35) (4 35) (3 36) (4 36)))


(defn- shift-pattern [pattern x y]
  (map (fn [[x1 y1]]
         [(+ x1 x) (+ y1 y)])
       pattern))

(defn create-camera
  "Create a camera and return it."
  [scene]
  (bb/ArcRotateCamera. "arc-camera" (* Math/PI 1.5) (* Math/PI 0.5) 50 (bb/Vector3. 0 0 0) scene))

(defn ^:dev/after-load -main
  []
  (let [canvas (.getElementById js/document "renderCanvas")
        engine (bb/Engine. canvas true)
        scene  (bb/Scene. engine)
        camera (create-camera scene)]
    (reset! !babylon-engine engine)

    (.attachControl camera canvas true)

    ;; renders initial board state
    (let [pattern (concat (shift-pattern blinker-pattern 0 -6)
                          (shift-pattern toad-pattern -10 -6)
                          (shift-pattern glider-pattern -10 6)
                          (shift-pattern gosper-glider-gun-pattern 5 -18))]
      (reset! !board (view/render-board scene {} pattern)))

    (let [domeLight (bb/HemisphericLight. "light" (bb/Vector3. 1 5 -1))]
      (set! domeLight.intensity 0.9))

    (let [skip-seconds 0.2
          skip-frames (* (.getFps engine) skip-seconds)]
      (.runRenderLoop engine (fn []
                               ;; update board state after every skip-seconds
                               (when (or (= 0 skip-frames) (zero? (mod (.getFrameId scene) skip-frames)))
                                 (reset! !board (view/update-board scene @!board)))
                               (.render scene))))

    (.addEventListener js/window "resize" #(.resize engine))))

;; called by the shadow-cljs hot-loader every time when application about to be reloaded
(defn ^:dev/before-load-async stop [done]
  (.dispose @!babylon-engine)
  (done))


(comment
  (keys (#'view/add-cell @!board [0 1] (.-scene @!babylon-engine)))

  (keys (#'view/remove-cell @!board [2 0]))

  (view/update-board (.-scene @!babylon-engine) @!board))