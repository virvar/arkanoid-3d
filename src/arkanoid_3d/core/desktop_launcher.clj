(ns arkanoid-3d.core.desktop-launcher
  (:require [arkanoid-3d.core :refer :all])
  (:import [com.badlogic.gdx.backends.lwjgl LwjglApplication]
           [org.lwjgl.input Keyboard])
  (:gen-class))

(defn -main
  []
  (LwjglApplication. arkanoid-3d-game "arkanoid-3d" 800 600)
  (Keyboard/enableRepeatEvents true))
