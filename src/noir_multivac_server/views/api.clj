(ns noir-multivac-server.views.api
  (:require [noir-multivac-server.models.item :as items]
            [noir-multivac-server.models.user :as users]
            [noir.response :as res]
            [noir.request :as request]
            [remvee.base64 :as base64]
            [cheshire.core :as json])
  (:use noir.core
        hiccup.core
        hiccup.page))


(def id-regex #"[a-zA-Z0-9]{24}")

(defn- json-ctype [body]
  (res/content-type "application/json" body))

(defpage "/api/search/" []
  (json-ctype (items/search [] :as :json :sort-dir 1)))

(defpage [:get ["/api/search/:tags" :tags #"(%20|[-\w\s,])+"]] {tags :tags}
  (json-ctype (items/search tags :as :json :sort-dir 1)))

(defpage [:get ["/api/item/:id" :id id-regex]] {id :id} 
  (json-ctype (items/fetch id :as :json)))

(defpage [:post "/api/item"] {}
  (let [body (slurp (:body (request/ring-request)))
        id (str (:_id (items/add! 
                        (json/parse-string body))))]
    id))

(defpage [:delete ["/api/item/:id" :id id-regex]] {id :id}
  (items/delete! id)
  "ok")

(defpage "/api/tag-count" []
  (json-ctype (items/tag-count 20)))


;********************************
; API KEY
;********************************

(defn parse-basic-auth [req]
  (let [auth ((:headers req) "authorization")
        cred (and auth
                  (base64/decode-str
                    (last
                      (re-find #"^Basic (.*)$" auth))))
        user (and cred
                  (last
                    (re-find #"^(.*):" cred)))
        pass (and cred
                  (last
                    (re-find #":(.*)$" cred)))]
    (if (and user pass)
      {:user user :pass pass :cred cred})))

(pre-route "/api*" []
           (let [req (request/ring-request)
                 auth (parse-basic-auth req)]
             (if (users/get-key (:user auth))
               nil
               (res/status 401 "Access denied"))))

