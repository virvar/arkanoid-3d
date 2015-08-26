(ns arkanoid-3d.entities
  (:require [play-clj.core :refer :all]
            [play-clj.g3d :refer :all]
            [play-clj.math :refer :all]
            [play-clj.ui :refer :all])
  (:import [com.badlogic.gdx.graphics.g3d.environment DirectionalLight]))

(def tile-size 2)
(def initial-ball-velocity (vector-3 0.2 0 0.2))


(defn create-wall
  [screen x1 y1 z1 x2 y2 z2]
  (let [attr (attribute! :color :create-diffuse (color :yellow))
        model-mat (material :set attr)
        model-attrs (bit-or (usage :position) (usage :normal))
        builder (model-builder)
        width (* (inc (- x2 x1)) tile-size)
        height (* (inc (- y2 y1)) tile-size)
        depth (* (inc (- z2 z1)) tile-size)]
    (-> (model-builder! builder :create-box
                        width
                        height
                        depth
                        model-mat model-attrs)
        model
        (assoc
          :x (* (/ (+ x1 x2) 2) tile-size)
          :y (* (/ (+ y1 y2) 2) tile-size)
          :z (* (/ (+ z1 z2) 2) tile-size)
          :width width
          :height height
          :depth depth
          :wall? true))))

(defn create-floor
  [screen x1 y1 z1 x2 y2 z2]
  (let [attr (attribute! :color :create-diffuse (color :red))
        model-mat (material :set attr)
        model-attrs (bit-or (usage :position) (usage :normal))
        builder (model-builder)
        width (* (inc (- x2 x1)) tile-size)
        height (* (inc (- y2 y1)) tile-size)
        depth (* (inc (- z2 z1)) tile-size)]
    (-> (model-builder! builder :create-box
                        width
                        height
                        depth
                        model-mat model-attrs)
        model
        (assoc
          :x (* (/ (+ x1 x2) 2) tile-size)
          :y (* (/ (+ y1 y2) 2) tile-size)
          :z (* (/ (+ z1 z2) 2) tile-size)
          :width width
          :height height
          :depth depth
          :floor? true))))

(defn create-block
  [screen x y z]
  (let [attr (attribute! :color :create-diffuse (color :blue))
        model-mat (material :set attr)
        model-attrs (bit-or (usage :position) (usage :normal))
        builder (model-builder)
        half-size (/ tile-size 2)]
    (-> (model-builder! builder :create-box tile-size tile-size tile-size model-mat model-attrs)
        model
        (assoc
          :x (* x tile-size)
          :y (* y tile-size)
          :z (* z tile-size)
          :width tile-size
          :height tile-size
          :depth tile-size
          :block? true))))

(defn create-player
  [screen x y z length]
  (let [attr (attribute! :color :create-diffuse (color :green))
        model-mat (material :set attr)
        model-attrs (bit-or (usage :position) (usage :normal))
        builder (model-builder)
        width (* length tile-size)
        height tile-size
        depth tile-size]
    (-> (model-builder! builder :create-box
                        width height depth
                        model-mat model-attrs)
        model
        (assoc
          :x (* x tile-size)
          :y (* y tile-size)
          :z (* z tile-size)
          :width width
          :height height
          :depth depth
          :player? true))))

(defn create-ball
  [screen x y z]
  (let [attr (attribute! :color :create-diffuse (color :purple))
        model-mat (material :set attr)
        model-attrs (bit-or (usage :position) (usage :normal))
        builder (model-builder)
        half-size (/ tile-size 2)]
    (-> (model-builder! builder :create-box
                        tile-size tile-size tile-size
                        model-mat model-attrs)
        model
        (assoc
          :x (* x tile-size)
          :y (* y tile-size)
          :z (* z tile-size)
          :width tile-size
          :height tile-size
          :depth tile-size
          :velocity initial-ball-velocity
          :ball? true))))

(defn create-environment
  [screen]
  (let [attr-type (attribute-type :color :ambient-light)
        attr (attribute :color attr-type 0.8 0.8 0.8 1)
        env (environment :set attr)
        light (DirectionalLight.)]
    (.set light (float 0.8) (float 0.8) (float 0.8) (float -0.2) (float -0.4) (float -1))
    (.add env light)))
