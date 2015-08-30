(ns arkanoid-3d.entities
  (:require [play-clj.core :refer :all :as play]
            [play-clj.g3d :refer :all]
            [play-clj.math :refer :all]
            [play-clj.ui :refer :all])
  (:import [com.badlogic.gdx.graphics.g3d.environment DirectionalLight]))

(def tile-size 2)
(def initial-ball-velocity (vector-3 0.2 0 0.2))
(def max-ball-velocity 0.2)

(defn- create-box
  [x y z width height depth color]
  (let [attr (attribute! :color :create-diffuse color)
        model-mat (material :set attr)
        model-attrs (bit-or (usage :position) (usage :normal))
        builder (model-builder)]
    (-> (model-builder! builder :create-box
                        width
                        height
                        depth
                        model-mat model-attrs)
        model
        (assoc
          :x x
          :y y
          :z z
          :width width
          :height height
          :depth depth))))

(defn- create-unit-box
  [x y z color]
  (let [x (* x tile-size)
        y (* y tile-size)
        z (* z tile-size)
        width tile-size
        height tile-size
        depth tile-size]
    (create-box x y z width height depth color)))

(defn- create-box-by-ends
  [x1 y1 z1 x2 y2 z2 color]
  (let [x (* (/ (+ x1 x2) 2) tile-size)
        y (* (/ (+ y1 y2) 2) tile-size)
        z (* (/ (+ z1 z2) 2) tile-size)
        width (* (inc (- x2 x1)) tile-size)
        height (* (inc (- y2 y1)) tile-size)
        depth (* (inc (- z2 z1)) tile-size)]
    (create-box x y z width height depth color)))

(defn- create-sphere
  [x y z width height depth color]
  (let [attr (attribute! :color :create-diffuse color)
        model-mat (material :set attr)
        model-attrs (bit-or (usage :position) (usage :normal))
        builder (model-builder)
        divisions-u 20
        divisions-v 20]
    (-> (model-builder! builder :create-sphere
                        width
                        height
                        depth
                        divisions-u
                        divisions-v
                        model-mat model-attrs)
        model
        (assoc
          :x x
          :y y
          :z z
          :width width
          :height height
          :depth depth))))


(defn create-wall
  [x1 y1 z1 x2 y2 z2]
  (let [color (play/color :yellow)
        box (create-box-by-ends x1 y1 z1 x2 y2 z2 color)]
    (assoc box :wall? true)))

(defn create-floor
  [x1 y1 z1 x2 y2 z2]
  (let [color (play/color :red)
        box (create-box-by-ends x1 y1 z1 x2 y2 z2 color)]
    (assoc box :floor? true)))

(defn create-block
  [x y z]
  (let [color (play/color :blue)
        box (create-unit-box x y z color)]
    (assoc box :block? true)))

(defn create-player
  [x y z length]
  (let [x (* x tile-size)
        y (* y tile-size)
        z (* z tile-size)
        width (* length tile-size)
        height tile-size
        depth tile-size
        color (play/color :green)
        box (create-sphere x y z width height depth color)]
    (assoc box :player? true)))

(defn create-ball
  [x y z]
  (let [color (play/color :purple)
        box (create-unit-box x y z color)]
    (assoc box
      :ball? true
      :velocity initial-ball-velocity)))

(defn create-environment
  [screen]
  (let [attr-type (attribute-type :color :ambient-light)
        attr (attribute :color attr-type 0.8 0.8 0.8 1)
        env (environment :set attr)
        light (DirectionalLight.)]
    (.set light (float 0.8) (float 0.8) (float 0.8) (float -0.2) (float -0.4) (float -1))
    (.add env light)))
