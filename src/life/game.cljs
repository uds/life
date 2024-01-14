(ns life.game
  "The game of life core mechanics")

(def ^:private board-half-size 50)

(defn- pos-in-board? [[x y]]
  (and (> x (- board-half-size)) (< x board-half-size)
       (> y (- board-half-size)) (< y board-half-size)))

(defn- cell-alive? [board pos]
  (and (pos-in-board? pos)
       (get board pos)))

(defn- neighbours
  "Return the neighbours of a cell."
  [[pos-x pos-y]]
  (for [x (range -1 2)
        y (range -1 2)
        :when (not (and (= x 0) (= y 0)))]
    [(+ x pos-x) (+ y pos-y)]))

(defn- dead-neighbours
  "Return the dead neighbours of a cell."
  [board pos]
  (->> (neighbours pos)
       (filter #(not (cell-alive? board %)))))

(defn- alive-neighbours
  "Return the alive neighbours of a cell."
  [board pos]
  (->> (neighbours pos)
       (filter #(cell-alive? board %))))

(defn- apply-death-rule
  "Apply the rules of the game of life to a live cell. Returns a command to update the board or nil."
  [pos alive-neighbours-count]
  ;; rules of game of life (from Wikipedia):
  ;; 1. Any live cell with fewer than two live neighbours dies, as if by underpopulation.
  ;; 2. Any live cell with two or three live neighbours lives on to the next generation.
  ;; 3. Any live cell with more than three live neighbours dies, as if by overpopulation.
  (when (or (< alive-neighbours-count 2)
            (> alive-neighbours-count 3))
    (conj pos :dead)))

(defn- apply-birth-rule
  "Apply the rules of the game of life to a dead cell. Returns a command to update the board or nil."
  [board pos]
  (let [alive-count (count (alive-neighbours board pos))]
    ;; rules of game of life (from Wikipedia):
    ;; 4. Any dead cell with exactly three live neighbours becomes a live cell, as if by reproduction.
    (if (= alive-count 3) (conj pos :alive) nil)))

(defn- apply-birth-rules [board positions]
  (->> (map #(apply-birth-rule board %) positions)
       (remove nil?)))

(defn- apply-rules*
  "Apply the rules of the game of life to a cell. The board is a map of positions to cell meshes.
   Returns a list of commands to update the board."
  [board pos]
  (let [dead-neighbours (dead-neighbours board pos)
        alive-count (- 8 (count dead-neighbours))
        commands (apply-birth-rules board dead-neighbours)]
    (if-let [death-command (apply-death-rule pos alive-count)]
      (conj commands death-command)
      commands)))

(defn apply-rules
  "Apply the rules of the game of life to the board. The board is a map of positions to cell meshes.
   Returns a set of commands to update the board."
  [board]
  (->> (keys board)
       (mapcat #(apply-rules* board %))
       (set)))


(comment
  (neighbours [0 0])

  (dead-neighbours {[0 -1] true, [0 0] true, [0 1] true} [0 0])

  (apply-rules* {[0 -1] true, [0 0] true, [0 1] true} [0 0])

  (apply-rules {[0 -1] true, [0 0] true, [0 1] true})
)
  