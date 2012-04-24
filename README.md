# stocker
    
Clone of ystockquote (https://github.com/fengb/ystockquote/).

## Usage

Stocker uses clj-time to some extent

    => (use '[clj-time.core :exclude (extend)])
    nil
    => (use 'stocker.core)
    nil

To pull in individual stats

    => (curr-stats "GOOG" :price :exch)
    {:exch "NasdaqNM", :price "597.60"}

To pull in all stats just pass the stock symbol

    => (curr-stats "GOOG")
    {:market-cap "194.4B", :price-book-ratio "3.14", :50-day-moving-avg "625.676", :dividend-yield "N/A", :short-ratio "1.50", :52-wk-low "473.02", :earnings-per-share "32.998", :change "+1.54", :book-val "189.709", :avg-daily-vol "2588040", :exch "NasdaqNM", :price-earnings-ratio "18.06", :dividend-per-share "0.00", :price-sales-ratio "4.85", :vol "2197933", :200-day-moving-avg "610.334", :ebitda "14.796B", :52-wk-high "670.25", :price "597.60", :price-eanings-growth-ratio "0.78"}

You can pull historical data by passing a start and end date

    => (hist-stats "GOOG" (date-time 2012 01 01) (date-time 2012 01 04))
    [{:date "2012-02-03", :close "596.33", :low "588.05", :vol "3168500", :adj-close "596.33", :open "590.66", :high "597.07"} {:date "2012-02-02", :close "585.11", :low "582.08", :vol "2414700", :adj-close "585.11", :open "584.87", :high "586.41"} {:date "2012-02-01", :close "580.83", :low "579.14", :vol "2320700", :adj-close "580.83", :open "584.94", :high "585.50"}]

hist-stats will accept an interval as well, if that's more convenient

    => (hist-stats "GOOG" (interval (date-time 2012 01 01) (date-time 2012 01 04)))
    [{:date "2012-02-03", :close "596.33", :low "588.05", :vol "3168500", :adj-close "596.33", :open "590.66", :high "597.07"} {:date "2012-02-02", :close "585.11", :low "582.08", :vol "2414700", :adj-close "585.11", :open "584.87", :high "586.41"} {:date "2012-02-01", :close "580.83", :low "579.14", :vol "2320700", :adj-close "580.83", :open "584.94", :high "585.50"}]


Values are left as strings in order to achieve exact parity with the data from Yahoo.  Do what you want with it.

## Notices

Only tested on Clojure 1.3

If Yahoo changes their format or url, which they likely will, this will probably break.  If you need a solid solution please look at Clojuratica (http://clojuratica.weebly.com/).  Mathematica has excellent financial capabilities.  Understand you take a speed hit.

## License

Copyright © 2012

Distributed under the Eclipse Public License, the same as Clojure.
