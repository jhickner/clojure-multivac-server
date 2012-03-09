(ns noir-multivac-server.server
  (:require [noir.server :as server]
            [somnium.congomongo :as db]))


(server/load-views "src/noir_multivac_server/views/")

(defn -main [& m]
  (let [mode (keyword (or (first m) :dev))
        port (Integer. (get (System/getenv) "PORT" "4567"))]
    (db/mongo! :db "multivac")
    (server/start port {:mode mode
                        :ns 'noir-multivac-server})))

