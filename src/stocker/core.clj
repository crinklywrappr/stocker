(ns stocker.core
    (:require [clojure.java.io :as io])
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

(defn- hist-map
       "Given a row of historical data, turns it into a map"
       [hist-coll]
       (apply hash-map 
              (interleave [:date :open :high :low :close :vol :adj-close] 
                          (cstr/split hist-coll #","))))

(defn curr-stats
  "[sym & stats] -> Returns the requested stats for the given stock
   [sym] -> Returns all available stats for the given stock"
  ([sym & stats]
   (let [ystats (apply str (vals (select-keys stats-map stats)))]
     (->>
       (-> (str curr-url "?s=" sym "&f=" ystats)
           (client/get)
           (:body)
           (cstr/trim-newline)
           (cstr/replace #"\"" "")
           (cstr/split #","))
       ;; For some reason, yahoo reorders stats after 9 
       ;; entries, so we need to reverse the stats if 
       ;; less than 9 were requested, and not touch it
       ;; if more than 9 were requested
       (cond-reverse 9) 
       (interleave stats)
       (apply hash-map))))
   ([sym] (apply curr-stats (cons sym (keys stats-map)))))

(defn hist-stats
  "[sym start end] -> returns the historical data for this stock between start & end, inclusive
   [sym interval] -> returns the historical data from this stock within interval, inclusive"
  ([sym start end]
   (let [url (str past-url "?s=" sym 
                  "&a=" (ctime/month start) "&b=" (ctime/day start) "&c=" (ctime/year start) 
                  "&d=" (ctime/month end) "&e=" (ctime/day end) "&f=" (ctime/year end) 
                  "&g=d&ignore=.csv")]
     (->>
       (-> (client/get url)
           (:body)
           (cstr/split #"\n"))
       (drop 1)
       (map hist-map)
       (vec))))
  ([sym interval] (hist-stats sym (ctime/start interval) (ctime/end interval))))
