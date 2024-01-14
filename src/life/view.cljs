(ns life.view
  "Renders the game of life"
  (:require [babylonjs :as bb]
            [life.game :as game]))


(def ^:private show-labels? false)


(defn- create-label
  "Create a text label as a plane mesh with text on it and return it."
  [scene text]
  (let [plane (bb/MeshBuilder.CreatePlane scene)
        mat (bb/StandardMaterial. "label-material" scene)
        texture (bb/DynamicTexture. "label-texture" 512 scene true)]
    (set! plane.position (bb/Vector3. -0.1 0.1 0))
    (set! mat.diffuseTexture texture)
    (set! mat.specularColor (bb/Color3. 1 1 1))
    (set! mat.emissiveColor (bb/Color3. 1 1 1))
    (set! mat.backFaceCulling false)
    (set! texture.hasAlpha true)
    (set! plane.material mat)
    (.drawText texture text 0 60 "bold 64px monospace" "white" "transparent")
    plane))

(defn- create-fresnel-material [scene name color]
  (let [material (bb/StandardMaterial. name scene)
        white-color (bb/Color3.White)
        black-color (bb/Color3.Black)
        scaled-color (.scale color 1)
        reflection-params (bb/FresnelParameters.)
        emissive-params (bb/FresnelParameters.)
        opacity-params (bb/FresnelParameters.)]
    (set! (.-diffuseColor material) (bb/Color3. 0 0 0))
    (set! (.-emissiveColor material) scaled-color)
    (set! (.-alpha material) 0.1)
    (set! (.-specularPower material) 16)
    ;; reflection
    (set! (.-bias reflection-params) 0.1)
    (set! (.-reflectionFresnelParameters material) reflection-params)
    ;; emission
    (set! (.-bias emissive-params) 0.6)
    (set! (.-power emissive-params) 4)
    (set! (.-leftColor emissive-params) white-color)
    (set! (.-rightColor emissive-params) color)
    (set! (.-emissiveFresnelParameters material) emissive-params)
    ;; opacity
    (set! (.-leftColor opacity-params) white-color)
    (set! (.-rightColor opacity-params) black-color)
    (set! (.-bias opacity-params) 0)
    (set! (.-power opacity-params) 1)
    (set! (.-opacityFresnelParameters material) opacity-params)
    material))

(defn- get-or-create-fresnel-material
  "Get or create a material for a cell."
  [scene name color]
  (if-let [mat (.getMaterialByName scene name)]
    mat
    (create-fresnel-material scene name color)))

(defn- create-cell
  "Create a cell and return it."
  [scene pos]
  (let [mesh (bb/MeshBuilder.CreateSphere scene)
        mat (get-or-create-fresnel-material scene "cell-material" (bb/Color3. 0 1 0))
        [x y] pos]
    (set! mesh.position (bb/Vector3. x y 0))
    (set! mesh.scaling (bb/Vector3. 0.95 0.95 0.95))
    (set! mesh.material mat)
    mesh))

(defn- destroy-cell [cell-mesh]
  (when cell-mesh
    (.dispose cell-mesh)))

(defn- add-cell [board pos scene]
  (let [cell (create-cell scene pos)]
    (when show-labels? (let [label (create-label scene (str pos))]
                         (set! label.parent cell)))
    (assoc board pos cell)))

(defn- remove-cell
  "Delete a cell at a position."
  [board pos]
  (destroy-cell (get board pos))
  (dissoc board pos))

(defn render-board
  "Render the game board. 
   The commands is a list of [x y state] tuples, where state is either :alive or :dead.
   Returns a board updated by input commands."
  [scene board commands]
  (reduce (fn [board [x y state]]
            (case state
              nil    (add-cell board [x y] scene)
              :alive (add-cell board [x y] scene)
              :dead  (remove-cell board [x y])))
          board
          commands))


(defn update-board
  "Apply the rules of the game of life to the board. Returns updated board."
  [scene board]
  (->> (game/apply-rules board)
       (render-board scene board)))
