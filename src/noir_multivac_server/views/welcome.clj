(ns noir-multivac-server.views.welcome
  (:require [noir-multivac-server.views.common :as common]
            [noir.response :as res])
  (:use noir.core
        hiccup.core
        hiccup.page-helpers))


(defpage "/welcome" []
         (common/layout
           [:p "Welcome to noir-multivac-server"]))


