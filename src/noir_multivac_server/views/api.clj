(ns noir-multivac-server.views.api
  (:require [noir-multivac-server.models.item :as items]
            [noir.response :as res]
            [noir.request :as request]
            [remvee.base64 :as base64]
            [cheshire.core :as json])
  (:use noir.core
        hiccup.core
        hiccup.page-helpers))


(def id-regex #"[a-zA-Z0-9]{24}")

(defn- json-ctype [body]
  (res/content-type "application/json" body))

(defpage [:get ["/api/search/:tags" :tags #"(%20|[\w\s,])+"]] {tags :tags}
  (json-ctype (items/search tags :as :json)))

(defpage [:get ["/api/item/:id" :id id-regex]] {id :id} 
  (json-ctype (items/fetch id :as :json)))

(defpage [:post "/api/item"] {}
  (let [body (slurp (:body (request/ring-request)))
        id (str (:_id (items/add! 
                        (json/parse-string body))))]
    id))

(defpage [:put ["/api/item/:id" :id id-regex]] {id :id}
  ; parse json body as p
  ;(items/update! id p)
  ; return 200 ok
  )

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
             (if (= (:user auth) 
                    "653638dc733afce75130303fe6e6010f63768af0")
               nil
               (res/status 401 "Access denied"))))

