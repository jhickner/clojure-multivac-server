(defproject noir-multivac-server "0.1.0-SNAPSHOT"
            :description "FIXME: write this!"
            :dependencies [[org.clojure/clojure "1.3.0"]
                           [noir "1.3.0-alpha10"]
                           [fetch "0.1.0-alpha2"]
                           [jayq "0.1.0-SNAPSHOT"]
                           [congomongo "0.1.8"]
                           [clj-base64 "0.0.0-SNAPSHOT"]
                           [cheshire "3.0.0"]]
            :plugins [[lein-cljsbuild "0.1.2"]]
            :cljsbuild {
              :builds [{
                :source-path "src"
                :compiler {
                  :output-to "resources/public/js/main.js"
                  :externs ["externs/jquery.js"]
                  ; :optimizations :whitespace 
                  ; :pretty-print true
                  :optimizations :advanced
                           }}]} 
            :main noir-multivac-server.server)

