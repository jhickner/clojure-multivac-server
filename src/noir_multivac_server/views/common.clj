(ns noir-multivac-server.views.common
  (:require [clojure.string :as string]
            [noir.validation :as vali]
            [noir.response :as res]
            [noir.session :as session]
            [noir-multivac-server.models.user :as users]
            [noir-multivac-server.models.item :as items])
  (:use noir.core
        hiccup.core
        hiccup.page-helpers
        hiccup.form-helpers))

(defpartial layout [& content]
            (html5
              [:head
               [:title "noir-multivac-server"]
               (include-css 
                 "http://fonts.googleapis.com/css?family=IM+Fell+English")
               (include-css "/css/reset.css")
               (include-css "/css/multivac.css")]
              [:body
               [:div#wrapper
                content]]))

(defpartial tag-link [t]
  [:li.tag 
    [:a.tag {:href (str "/notes/" t)} t]])

(defpartial item-layout [i]
  [:li.item
    [:ul.tags (map tag-link (i :tags))]
    [:span.date (i :ts)]
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

(defpage "/" []
  (res/redirect "/notes"))

(defpage "/notes" []
  (items-page (items/search "todo")))

(defpage [:post "/notes"] {:as data}
  (items-page (items/search (data :tags))))

(defpage [:get ["/notes/:tags" :tags #"(%20|[\w\s,])+"]] {tags :tags}
  (items-page (items/search tags)))

(pre-route "/notes*" {}
           (when-not (users/admin?)
             (res/redirect "/login")))


(defpartial user-fields [{:keys [username] :as usr}]
            (vali/on-error :username error-text)
            (text-field {:placeholder "Username"} :username username)
            (password-field {:placeholder "Password"} :password))

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



