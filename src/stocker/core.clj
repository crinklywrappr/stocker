(ns stocker.core
    (:require [clojure.data.csv :as csv])
    (:require [clojure.string :as cstr])
    (:require [clj-http.client :as client])
    (:require [clj-time.core :as ctime]))

(def curr-url "http://finance.yahoo.com/d/quotes.csv")
(def past-url "http://ichart.yahoo.com/table.csv")
(def stats-map {
     :price "l1" 
     :change "c1"
     :vol "v"
     :avg-daily-vol "a2"
     :exch "x"
     :market-cap "j1"
     :book-val "b4"
     :ebitda "j4"
     :dividend-per-share "d"
     :dividend-yield "y"
     :earnings-per-share "e"
     :52-wk-high "k"
     :52-wk-low "j"
     :50-day-moving-avg "m3"
     :200-day-moving-avg "m4"
     :price-earnings-ratio "r"
     :price-eanings-growth-ratio "r5"
     :price-sales-ratio "p5"
     :price-book-ratio "p6"
     :short-ratio "s7"})

(defn- cond-reverse
       "performs reverse on coll if (count coll) < n"
       [n coll]
       (if (< (count coll) n)
         (reverse coll)
         coll))

(defn safekey 
  "Given a keyword name, replaces all spaces with a dash, 
   transforms it to lowercase, and then makes it a keyword"
  [name] 
  (-> name 
      (cstr/replace #" " "-") 
      (cstr/lower-case) 
      (keyword)))

(defn build-map 
  "[ks vs] -> Given these two collections, constructs a hashmap of {ks1 vs1 ks2 vs2 ...}"
  [ks vs] (apply hash-map (interleave ks vs)))

(defn curr-stats-raw
  "[sym & stats] -> Returns the requested stats for the given stock (raw)
   [sym] -> Returns all available stats for the given stock (raw)"
  ([sym & stats]
   (let [ystats (apply str (vals (select-keys stats-map stats)))]
     (:body (client/get (str curr-url "?s=" sym "&f=" ystats)))))
   ([sym] (apply curr-stats-raw (cons sym (keys stats-map)))))


(defn curr-stats
  "[sym & stats] -> Returns the requested stats for the given stock
   [sym] -> Returns all available stats for the given stock"
  ([sym & stats]
   (->> (apply curr-stats-raw sym stats)
        (csv/read-csv)
        ;; For some reason, yahoo reorders stats after 9 
        ;; entries, so we need to reverse the stats if 
        ;; less than 9 were requested, and not touch it
        ;; if more than 9 were requested.  Need to cons
        ;; 9 onto the form, since read-csv returns a nested
        ;; vector ie ([data])
        (cons 9)
        (apply cond-reverse)
        (interleave stats)
        (apply hash-map)))
  ([sym] (apply curr-stats (cons sym (keys stats-map)))))

(defn hist-stats-raw
  "[sym start end] -> returns the historical data for this stock between start & end, inclusive (raw)
   [sym interval] -> returns the historical data from this stock within interval, inclusive (raw)"
  ([sym start end]
   (let [url (str past-url "?s=" sym 
                  "&a=" (ctime/month start) "&b=" (ctime/day start) "&c=" (ctime/year start) 
                  "&d=" (ctime/month end) "&e=" (ctime/day end) "&f=" (ctime/year end) 
                  "&g=d&ignore=.csv")]
     (:body (client/get url))))
  ([sym interval] (hist-stats-raw sym (ctime/start interval) (ctime/end interval))))

(defn hist-stats 
  "[sym start end] -> returns the historical data for this stock between start & end, inclusive
   [sym interval] -> returns the historical data from this stock within interval, inclusive"
  ([sym start end] 
   (let [stats (csv/read-csv (hist-stats-raw sym start end)) 
         ks (apply map safekey (take 1 stats)) 
         hm (partial build-map ks)] 
     (vec (map hm (drop 1 stats)))))
  ([sym interval] (hist-stats sym (ctime/start interval) (ctime/end interval))))
