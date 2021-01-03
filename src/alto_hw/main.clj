(ns alto-hw.main
  (:require [alto-hw.server :as server]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.reload :refer [wrap-reload]]))

(defn dev-main
  [{:keys [port]}]
  (jetty/run-jetty (-> #'server/app
                       (wrap-reload))
                   {:port port}))

(defn main
  [{:keys [port]}]
  (jetty/run-jetty server/app {:port port}))
