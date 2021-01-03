(ns alto-hw.server
  (:require [compojure.core :refer [defroutes routes GET POST PUT DELETE]]
            [compojure.route :as route]
            [ring.middleware.content-type :refer [wrap-content-type]]
            [ring.middleware.json :refer [wrap-json-response wrap-json-body]]
            [alto-hw.model :as m]))

(defn api-routes
  [state-a]
  (routes
    (PUT "/" []
      (fn [r]
        (m/add-coins! state-a (-> r :body (get "coin")))
        {:status  204
         :headers {"X-Coins" (str (m/coins-accepted state-a))}}))

    (DELETE "/" []
      (fn [r]
        {:status  204
         :headers {"X-Coins" (str (m/return-coins! state-a))}}))

    (GET "/inventory" []
      (fn [r]
        {:status 200
         :body   (mapv #(m/inventory state-a %) ["0" "1" "2"])}))

    (PUT "/inventory/:id{[0-2]}" [id]
      (fn [r]
        (cond
          (m/can-purchase? state-a id)
          (let [[c i] (m/make-selection! state-a id)]
            {:status  200
             :headers {"X-Coins"               (str c)
                       "X-Inventory-Remaining" (str i)}
             :body    {:quantity 1}})

          (not (m/in-stock? state-a id))
          {:status  404
           :headers {"X-Coins" (str (m/coins-accepted state-a))}}

          :else
          {:status  403
           :headers {"X-Coins" (str (m/coins-accepted state-a))}})))))

(defn mk-app
  [state-a]
  (routes
    (-> (api-routes state-a)
        (wrap-json-response)
        (wrap-json-body))
    (route/not-found "Route Not Handled")))

(def app (mk-app (m/mk-state-a)))
