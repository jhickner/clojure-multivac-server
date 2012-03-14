(ns noir-multivac-server.client.main
  (:require [fetch.remotes :as remotes]) 
  (:require-macros [fetch.macros :as fm])
  (:use [jayq.core :only [$ remove delegate data]])) 


(defn remote-delete [id]
  (fm/remote (delete-item id) [res]
             (remove 
               ($ (str "li.item[data-id=" id "]")))))

(.ready ($ :document) 
        (fn []
          (delegate ($ :body) :a.delete :click
                    (fn [e]
                      (.preventDefault e)
                      (if (js/confirm "really delete?")
                        (this-as me
                                 (let [id (data ($ me) :id)]
                                   (remote-delete id)))))))) 
