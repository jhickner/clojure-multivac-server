(ns noir-multivac-server.views.api
  (:require [noir-multivac-server.models.item :as items]
            [noir.response :as res])
  (:use noir.core
        hiccup.core
        hiccup.page-helpers))

(def id-regex #"[a-zA-Z0-9]{24}")

(defn- json-ctype [body]
  (res/content-type "application/json" body))

(defpage [:get ["/api/search/:tags" :tags #"(%20|[\w\s,])+"]] {tags :tags}
  (json-ctype (items/search tags :as :json)))

; get particular item
(defpage [:get ["/api/item/:id" :id id-regex]] {id :id} 
  (json-ctype (items/fetch id :as :json)))

; create an item
(defpage [:post "/api/item"] {} "create")

; update an item
(defpage [:put ["/api/item/:id" :id id-regex]] {} "update")

; delete an item
(defpage [:delete ["/api/item/:id" :id id-regex]] {id :id} "delete")
