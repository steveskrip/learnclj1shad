(ns ^:figwheel-hooks learnclj1shad.core
  (:require
   [goog.dom :as gdom]
   [reagent.core :as reagent :refer [atom]]
   [reagent.dom :as rdom]
   [ajax.core :refer [GET]]
   [cuerdas.core :as str]))

(println "This text is printed from src/learnclj1/core.cljs. Go ahead and edit it and see reloading in action.")

(defn multiply [a b] (* a b))
;; define your app data so that it doesn't get over-written on reload
(enable-console-print!)



(defonce app-state (atom {:title      "WhichWeather"
                          :postal-code ""
                          :data-received? false
                          :temperatures {:today {:label "Today"
                                                  :value nil}
                                          :tomorrow {:label "Tomorrow"
                                                     :value nil}}}))
(def api-key "c73c6867a8fdeb28b916f24746bc9a58")

(defn handle-response [resp]                                             ;; <2>
  (let [today (get-in resp ["list" 0 "main" "temp"])
        tomorrow (get-in resp ["list" 24 "main" "temp"])]
    (swap! app-state
      update-in [:temperatures :today :value] (constantly today))
    (swap! app-state
      update-in [:temperatures :tomorrow :value] (constantly tomorrow))))

(defn get-forecast! []                                                   ;; <3>
  (let [postal-code (:postal-code @app-state)]
    (GET "http://api.openweathermap.org/data/2.5/forecast"
         {:params {"q" (str postal-code ",us")
                   "units" "imperial"
                   "appid" api-key}
          :handler handle-response})))

(defn title []
  [:h1 (:title @app-state)])

(defn temperature [temp]
  [:div {:class "temperature"}
   [:div {:class "value"} (:value temp)]
   [:h2 (:label temp)]])

(defn postal-code []
  [:div {:class-name "postal-code"}
   [:h3 "Enter your postal code"]
   [:input {:type "text"
            :placeholder "Postal Code"
            :value (:postal-code @app-state)
            :on-change #(swap! app-state assoc :postal-code (-> % .-target .-value))}]
   [:button {:on-click get-forecast!} "Go"]])

(defn app []
  [:div {:class "app"}
   [title]
   [:div {:class "temperatures"}
    (for [temp (vals (:temperatures @app-state))]
      [temperature temp])]
   [postal-code]])

(defn ^:dev/after-load render! [] 
  (rdom/render [app] (. js/document (getElementById "app"))))
