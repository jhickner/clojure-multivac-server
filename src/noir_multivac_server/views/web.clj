(ns noir-multivac-server.views.web
  (:require [clojure.string :as string]
            [noir.validation :as vali]
            [noir.response :as res]
            [noir.session :as session]
            [noir-multivac-server.models.user :as users]
            [noir-multivac-server.models.item :as items])
  (:use noir.core
        noir.fetch.remotes
        hiccup.core
        hiccup.page
        hiccup.form
        hiccup.element
        (clj-time [core :only [in-days interval now]])
        (clj-time [coerce :only [from-date]])
        ))

;***********************
; PARTIALS
;***********************

(defpartial layout [& content]
            (html5
              [:head
               [:meta {:content "width=device-width, initial-scale=1" 
                       :name "viewport"}]
               [:meta {:name "apple-mobile-web-app-capable"
                       :content "yes"}] 
               [:meta {:names "apple-mobile-web-app-status-bar-style"
                       :content "black-translucent"}]
               [:title "Multivac"]
               (include-js "http://use.typekit.com/vek3cab.js")
               (javascript-tag "try{Typekit.load();}catch(e){}")
               (include-css "/css/reset.css")
               (include-css "/css/multivac.css")
               (include-js 
                 "https://ajax.googleapis.com/ajax/libs/jquery/1.7.1/jquery.min.js") 
              (include-js "/js/main.js")] 
              [:body.droid
               [:div#wrapper
                content]]))

(defpartial tag-link [t]
  [:li.tag 
    [:a.tag {:href (str "/notes/" t)} t]])

(defpartial item-layout [i]
  [:li.item {:data-id (i :_id)}
    [:ul.tags (map tag-link (map string/upper-case (i :tags)))]
    [:div.date.info (in-days (interval (from-date (i :ts)) (now)))
                " days ago"]
    [:a.delete.info {:href "#" :data-id (i :_id) } "delete"]
    [:p.content (i :body)]
    (when-let [link (i :link)] 
      [:a {:href link :target "_new"} link]) 
   ])

(defpartial items-page [items]
  (layout
    (form-to [:post "/notes"]
             (text-field {:placeholder "Tags"} :tags))
    [:ul.items
      (map item-layout items)]))

(defpartial error-text [errors]
            [:p (string/join "<br/>" errors)])

(defpartial user-fields [{:keys [username] :as usr}]
            (vali/on-error :username error-text)
            (text-field {:placeholder "Username"} :username username)
            (password-field {:placeholder "Password"} :password))

;***********************
; REMOTES
;***********************

(defremote delete-item [id]
  (items/delete! id)
  "ok")


;***********************
; PAGES
;***********************

(pre-route "/notes*" {}
           (when-not (users/admin?)
             (res/redirect "/login")))

(defpage "/" []
  (res/redirect "/notes"))

(defpage [:post "/notes"] {:as data}
  (items-page (items/search (data :tags))))

(defpage "/notes" [] (items-page []))
(defpage "/notes/" [] (items-page [])) 

(defpage [:get ["/notes/:tags" :tags #"(%20|[-\w\s,])+"]] {tags :tags}
  (let [tags (items/parse-tags tags)]
    ; don't allow an empty search
    (items-page (if (seq tags) 
                  (items/search tags)
                  []))))

(defpage "/login" {:as user}
         (if (users/admin?)
           (res/redirect "/notes")
           (layout
             (form-to [:post "/login"]
                      [:ul.actions
                        [:li (link-to {:class "submit"} "/" "Login")]]
                      (user-fields user)
                      (submit-button {:class "submit"} "submit")))))

(defpage [:post "/login"] {:as user}
  (if (users/login! user)
    (res/redirect "/notes")
    (render "/login" user)))

(defpage "/logout" {}
         (session/clear!)
         (res/redirect "/notes"))
