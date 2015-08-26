(ns arkanoid-3d.core
  (:require [play-clj.core :refer :all]
            [play-clj.g3d :refer :all]
            [play-clj.math :refer :all]
            [play-clj.ui :refer :all]
            [arkanoid-3d.logic :refer :all]))

(defscreen main-screen
  :on-show
  (fn [screen entities]
    (init screen entities))

  :on-render
  (fn [screen entities]
    (clear! 1 1 1 1)
    (->> entities
         (update-game! screen)
         (render! screen))))


(defscreen text-screen
  :on-show
  (fn [screen entities]
    (update! screen :camera (orthographic) :renderer (stage))
    (assoc (label "0" (color :black))
      :id :fps
      :x 5))

  :on-render
  (fn [screen entities]
    (->> (for [entity entities]
           (case (:id entity)
             :fps (doto entity (label! :set-text (str (game :fps))))
             entity))
         (render! screen)))

  :on-resize
  (fn [screen entities]
    (height! screen 300)))

(defgame arkanoid-3d-game
  :on-create
  (fn [this]
    (set-screen! this main-screen text-screen)))
