(ns arkanoid-3d.core.run-repl
  (:require [play-clj.core :refer :all]
            [arkanoid-3d.core :refer :all]
            [arkanoid-3d.core.desktop-launcher :refer [-main]]))

(defscreen blank-screen
  :on-render
  (fn [screen entities]
    (clear!)))

(set-screen-wrapper! (fn [screen screen-fn]
                       (try (screen-fn)
                         (catch Exception e
                           (.printStackTrace e)
                           (set-screen! arkanoid-3d-game blank-screen)))))

(-main)

(on-gl (set-screen! arkanoid-3d-game main-screen text-screen))
