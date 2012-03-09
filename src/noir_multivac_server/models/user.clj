(ns noir-multivac-server.models.user
  (:require 
    [noir.util.crypt :as crypt]
    [noir.session :as session]
    [noir.validation :as vali]
    [somnium.congomongo :as db]))

(def coll :users)

(defn admin? []
  (session/get :admin))

(defn me []
  (session/get :username))

(defn get-username [username]
  (db/fetch-one coll :where {:username username}))

(defn login! [{:keys [username password] :as user}]
  (let [{stored-pass :password} (get-username username)]
    (if (and stored-pass 
             (crypt/compare password stored-pass))
      (do
        (session/put! :admin true)
        (session/put! :username username))
      (vali/set-error :username "Invalid username or password"))))




