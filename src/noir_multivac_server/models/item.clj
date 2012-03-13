(ns noir-multivac-server.models.item
  (:require 
    [clojure.string :as string]
    [somnium.congomongo :as db]))

(def coll :items)

;***********************
; HELPERS
;***********************

(defn- parse-tags [tags] 
  (if (string? tags) 
    (map string/lower-case 
         (string/split (string/replace tags #"[^\w\s,]" "") #"[\s,]+"))
    tags))

(defn- annotate-item [p]
  (merge p {:ts (java.util.Date.) :tags (parse-tags (:tags p))}))

; db/fetch :as :json doesn't actually return correct json
(defn- fix-json [s]
  (str "[" (apply str (interpose "," s)) "]"))


;***********************
; OPERATIONS
;***********************

(defn search [tags &{:keys [as]}]
  (let [tag-vec (parse-tags tags)
        is-json (= as :json)
        opts (if is-json [:as :json] [])
        opts (if (seq tag-vec) 
               (concat opts [:where {:tags {"$all" tag-vec}}
                             :sort {:ts 1}]))
        res (apply db/fetch coll opts)]
    (if is-json
      (fix-json res)
      res)))

(defn fetch [id &{:keys [as]}] 
  (let [opts [:where {:_id (db/object-id id)}]]
    (apply db/fetch-one coll (if (= as :json)
                               (concat opts '[:as :json])
                               opts))))

(defn add! [p] 
  (db/insert! coll (annotate-item p)))

(defn delete! [id] 
  (db/destroy! coll {:_id (db/object-id id)}))

(defn update! [id p]
  (if-let [current (fetch id)]
    (db/update! :items {:_id (db/object-id id)}
             (annotate-item (merge current p)))))

(defn tag-count [limit]
  (let [mapfn 
        "function() {
          this.tags.forEach(function(tag) {
            emit(tag,1)
          })
        }"
        reducefn 
        "function(key,values) {
          var res=0;
            values.forEach(function(value) {
              res+=value
            });
          return res;
        }"]
    (db/map-reduce coll mapfn reducefn :tag-count)
    (fix-json (db/fetch :tag-count 
                        :sort {:value -1} 
                        :limit limit
                        :as :json))))

