(ns arkanoid-3d.logic
  (:require [play-clj.core :refer :all]
            [play-clj.g3d :refer :all]
            [play-clj.math :refer :all]
            [play-clj.ui :refer :all]
            [arkanoid-3d.entities :refer :all]))

(def mouse-sensetive 0.3)
(def velocity 0.5)
(def gravity 2)

(defn- initial-entities
  [screen]
  (vector
   (create-floor screen -3 0 11 7 0 11)
   (create-wall screen -3 0 -4 7 0 -4)
   (create-wall screen -4 0 -4 -4 0 10)
   (create-wall screen 8 0 -4 8 0 10)

   (create-player screen 2 0 10 3)
   (create-ball screen 0 0 4)

   (create-block screen 2 0 3)

   (create-block screen 1 0 2)
   (create-block screen 2 0 2)
   (create-block screen 3 0 2)

   (create-block screen 0 0 1)
   (create-block screen 1 0 1)
   (create-block screen 2 0 1)
   (create-block screen 3 0 1)
   (create-block screen 4 0 1)

   (create-block screen 1 0 0)
   (create-block screen 3 0 0)

   (create-block screen 1 1 2)
   (create-block screen 2 1 2)
   (create-block screen 3 1 2)

   (create-block screen 0 2 1)
   (create-block screen 1 2 1)
   (create-block screen 2 2 1)
   (create-block screen 3 2 1)
   (create-block screen 4 2 1)
   ))

(defn- handle-look-first-person!
  [screen]
  (when (button-pressed? :left)
    (let [delta-x (- (input! :get-delta-x))
          delta-y (- (input! :get-delta-y))]
      (perspective! screen :rotate (vector-3 0 1 0) delta-x)
      (perspective! screen :rotate
                    (-> (direction screen)
                        (vector-3! :cpy)
                        (vector-3! :crs (up screen)))
                    delta-y))))

(defn- handle-look-third-person!
  [screen]
  (when (button-pressed? :right)
    (let [delta-x (- (input! :get-delta-x))
          delta-y (- (input! :get-delta-y))]
      (perspective! screen :rotate-around (vector-3 0 0 0) (up screen) delta-x)
      (perspective! screen :rotate-around (vector-3 0 0 0)
                    (-> (direction screen)
                        (vector-3! :cpy)
                        (vector-3! :crs (up screen)))
                    delta-y))))

(defn- handle-camera-move!
  [screen]
  (let [delta-time (:delta-time screen)
        delta (* velocity delta-time)]
    (when (key-pressed? :w)
      (perspective! screen :translate (-> (direction screen)
                                          (vector-3! :cpy)
                                          (vector-3! :scl (float velocity)))))
    (when (key-pressed? :s)
      (perspective! screen :translate (-> (direction screen)
                                          (vector-3! :cpy)
                                          (vector-3! :scl (float (- velocity))))))
    (when (key-pressed? :a)
      (perspective! screen :translate (-> (direction screen)
                                          (vector-3! :cpy)
                                          (vector-3! :crs (up screen))
                                          (vector-3! :scl (float (- velocity))))))
    (when (key-pressed? :d)
      (perspective! screen :translate (-> (direction screen)
                                          (vector-3! :cpy)
                                          (vector-3! :crs (up screen))
                                          (vector-3! :scl (float velocity)))))
    (when (key-pressed? :e)
      (perspective! screen :translate (-> (up screen)
                                          (vector-3! :cpy)
                                          (vector-3! :scl (float velocity)))))
    (when (key-pressed? :q)
      (perspective! screen :translate (-> (up screen)
                                          (vector-3! :cpy)
                                          (vector-3! :scl (float (- velocity))))))))

(defn- handle-player-move
  [screen entities]
  (let [left -2
        right 6
        x (+ left (* (/ (game :x) (game :width)) (- right left)))]
    (map (fn [entity]
           (if (:player? entity)
             (assoc entity :x (* x tile-size))
             entity))
         entities)))

(defn- move-ball
  [screen entities]
  (map (fn [entity]
         (if (:ball? entity)
           (let [velocity (:velocity entity)]
             (-> entity
                 (update :x #(+ % (x velocity)))
                 (update :z #(+ % (z velocity)))))
           entity))
       entities))

;; collisions

(defn- get-collision
  [entity1 entity2]
  (when (= (:y entity1) (:y entity2))
    (let [collision-rect (rectangle 0 0 0 0)]
      (if (intersector!
           :intersect-rectangles
           (rectangle (- (:x entity1) (/ (:width entity1) 2))
                      (- (:z entity1) (/ (:depth entity1) 2))
                      (:width entity1)
                      (:depth entity1))
           (rectangle (- (:x entity2) (/ (:width entity2) 2))
                      (- (:z entity2) (/ (:depth entity2) 2))
                      (:width entity2)
                      (:depth entity2))
           collision-rect)
        collision-rect
        nil))))

(defn- handle-collision
  [screen entities collide-entity collision-rect]
  (if (:floor? collide-entity)
    (initial-entities screen)
    (filter
     some?
     (map (fn [entity]
            (cond
             (:ball? entity)
             (let [velocity (:velocity entity)
                   collision-width (rectangle! collision-rect :get-width)
                   collision-height (rectangle! collision-rect :get-height)
                   distinction (- collision-width collision-height)
                   error 0.1]
               (cond
                (or (< 0 distinction error)
                    (< 0 (- distinction) error))
                (-> entity
                    (assoc :velocity (vector-3 (- (x velocity)) (y velocity) (- (z velocity))))
                    (update :x #((if (< (x velocity) 0) + -) % (* collision-width 2)))
                    (update :z #((if (< (z velocity) 0) + -) % (* collision-height 2))))
                (> collision-width collision-height)
                (-> entity
                    (assoc :velocity (vector-3 (x velocity) (y velocity) (- (z velocity))))
                    (update :z #((if (< (z velocity) 0) + -) % (* collision-height 2))))
                (< collision-width collision-height)
                (-> entity
                    (assoc :velocity (vector-3 (- (x velocity)) (y velocity) (z velocity)))
                    (update :x #((if (< (x velocity) 0) + -) % (* collision-width 2))))))
             (and (= entity collide-entity) (:block? entity))
             nil
             :default entity))
          entities))))

(defn- check-collision
  [screen entities entity]
  (let [ball (find-first :ball? entities)]
    ;; (if (not= entity ball)
    (if (not (:ball? entity))
      (if-let [rect (get-collision ball entity)]
        (handle-collision screen entities entity rect)
        entities)
      entities)))

(defn- handle-collisions
  [screen entities]
  (reduce (partial check-collision screen)
          entities
          entities))

;; end collisions

(defn- get-blocks-under
  [entities entity]
  (filter (fn [other]
            (and (= (:x other) (:x entity))
                 (= (:z other) (:z entity))
                 (< (:y other) (:y entity))))
          entities))

(defn- apply-gravity-to
  [screen entities entity]
  (let [blocks-under (get-blocks-under entities entity)
        delta (* gravity (:delta-time screen))
        new-y (- (:y entity) delta)]
    (if (not (some (fn [other]
                     (< (:y entity)
                        (+ (:y other) (:height other))))
                   blocks-under))
      (let [corrected-y (if (< new-y 0) 0 new-y)]
        (assoc entity :y corrected-y))
      entity)))

(defn- apply-gravity
  [screen entities]
  (map (fn [entity]
         (if (< 0 (:y entity))
           (apply-gravity-to screen entities entity)
           entity))
       entities))

(defn update-game!
  [screen entities]
  (doto screen
    (handle-look-first-person!)
    (handle-look-third-person!)
    (handle-camera-move!)
    (perspective! :update))
  (->> entities
       (handle-player-move screen)
       (move-ball screen)
       (handle-collisions screen)
       (apply-gravity screen)))

(defn init
  [screen entities]
  (let [screen (update! screen
                        :renderer (model-batch)
                        :attributes (create-environment screen)
                        :camera (doto (perspective 75 (game :width) (game :height))
                                  (position! 5 20 30)
                                  (direction! 0 0 0)
                                  (near! 0.1)
                                  (far! 300)))]
    (initial-entities screen)))