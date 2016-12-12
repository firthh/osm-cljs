(ns osm-figwheel.core
  (:require [quil.core :as q :include-macros true]
            [quil.middleware :as m]
            [osm-figwheel.data :refer [data]]))

(enable-console-print!)

(defn setup []
  ; Set frame rate to 30 frames per second.
  (q/frame-rate 30)
  ; Set color mode to HSB (HSV) instead of default RGB.
  (q/color-mode :hsb)
  ; setup function returns initial state. It contains
  ; circle color and position.
  {:color 0
   :angle 0})

;; (defonce processed )

(let [a (first data)]
  (println (partition 2 (:nodes a)))
  )

(defn min-longitude [nodes]
  (apply min (map :lon nodes)))

(defn min-latitude [nodes]
  (apply min (map :lat nodes)))

(defn update-state [state]
  ; Update sketch state by changing circle color and position.
  {:color (mod (+ (:color state) 0.7) 255)
   :angle (+ (:angle state) 0.1)})

(defn draw-line [[{lon1 :lon lat1 :lat} {lon2 :lon lat2 :lat}]]
  (q/line lon1 lat1 lon2 lat2))

(def scale-factor 100000)

(def scale 1000)

(defn translate [min-lat min-lon n]
  ;(println min-lat)
  (-> n
      (update :lon - min-lon)
      (update :lon * scale-factor)
      (update :lat - min-lat)
      (update :lat * scale-factor)
      (update :lat #(- scale %))))

(defn draw-state [state]
  ; Clear the sketch by filling it with light-grey color.
  (q/background 240)
  ; Set circle color.
  (q/fill (:color state) 255 255)
  (let [a (first data)
                                        ;
        min-lon (apply min (mapcat (fn [w] (map :lon (:nodes w))) data))
        min-lat (apply min (mapcat (fn [w] (map :lat (:nodes w))) data))
        ]
    (doall (for [{:keys [nodes]} data]
             (->> nodes
                  (map (partial translate min-lat min-lon))
                  (partition 2 1)
                  (map draw-line)
                  doall)
             )))
  )

(q/defsketch osm-cljs
  :host "osm-cljs"
  :size [scale scale]
  ; setup function called only once, during sketch initialization.
  :setup setup
  ; update-state is called on each iteration before draw-state.
  :update update-state
  :draw draw-state
  ; This sketch uses functional-mode middleware.
  ; Check quil wiki for more info about middlewares and particularly
  ; fun-mode.
  :middleware [m/fun-mode])
