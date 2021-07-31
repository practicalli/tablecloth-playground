;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; TableCloth examples
;;
;; https://scicloj.github.io/tablecloth/index.html
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(ns practicalli.tablecloth-basics
  (:require [tablecloth.api :as tables]
            [tech.v3.datatype.functional :as datatype]
            [clojure.java.io :as io]))

(comment

  ;; Empty dataset
  (tables/dataset)
  ;; => _unnamed [0 0]

  ;; Dataset from single value.
  (tables/dataset 999)
;; => _unnamed [1 1]:

;; | :$value |
;; |--------:|
;; |     999 |

  (def dataset
    (tables/dataset
     {:V1 (take 9 (cycle [1 2]))
      :V2 (range 1 10)
      :V3 (take 9 (cycle [0.5 1.0 1.5]))
      :V4 (take 9 (cycle ["A" "B" "C"]))}))

  ;; evaluate the dataset name and pretty print the results
  dataset
;; => _unnamed [9 4]:
;;    | :V1 | :V2 | :V3 | :V4 |
;;    |----:|----:|----:|-----|
;;    |   1 |   1 | 0.5 |   A |
;;    |   2 |   2 | 1.0 |   B |
;;    |   1 |   3 | 1.5 |   C |
;;    |   2 |   4 | 0.5 |   A |
;;    |   1 |   5 | 1.0 |   B |
;;    |   2 |   6 | 1.5 |   C |
;;    |   1 |   7 | 0.5 |   A |
;;    |   2 |   8 | 1.0 |   B |
;;    |   1 |   9 | 1.5 |   C |

;; Set column name for single value.

  (tables/dataset 999 {:single-value-column-name "my-single-value"})
;; => _unnamed [1 1]:

;; | my-single-value |
;; |----------------:|
;; |             999 |

;; Also set the dataset name.

  (tables/dataset 999 {:single-value-column-name ""
                       :dataset-name "Single value"})
;; => Single value [1 1]:

;; |   0 |
;; |----:|
;; | 999 |

;; Sequence of pairs (first = column name, second = value(s))
  (tables/dataset [[:A 33] [:B 5] [:C :a]])

;; Not sequential values are repeated row-count number of times.

  (tables/dataset [[:A [1 2 3 4 5 6]] [:B "X"] [:C :a]])
;; => _unnamed [6 3]:

;; | :A | :B | :C |
;; |---:|----|----|
;; |  1 |  X | :a |
;; |  2 |  X | :a |
;; |  3 |  X | :a |
;; |  4 |  X | :a |
;; |  5 |  X | :a |
;; |  6 |  X | :a |

;; Dataset created from map (keys = column names, vals = value(s)). Works the same as sequence of pairs.

  (tables/dataset {:A 33})
;; => _unnamed [1 1]:
;;    | :A |
;;    |---:|
;;    | 33 |


  (tables/dataset {:A [1 2 3]})
;; => _unnamed [3 1]:

;; | :A |
;; |---:|
;; |  1 |
;; |  2 |
;; |  3 |


  (tables/dataset {:A [3 4 5] :B "X"})
  ;; => _unnamed [3 2]:

  ;; | :A | :B |
  ;; |---:|----|
  ;; |  3 |  X |
  ;; |  4 |  X |
  ;; |  5 |  X |

;; You can put any value inside a column
  (tables/dataset {:A [[3 4 5] [:a :b]] :B "X"})
;; => _unnamed [2 2]:

;; |      :A | :B |
;; |---------|----|
;; | [3 4 5] |  X |
;; | [:a :b] |  X |

;; Sequence of maps
  (tables/dataset [{:a 1 :b 3} {:b 2 :a 99}])
;; => _unnamed [2 2]:

;; | :b | :a |
;; |---:|---:|
;; |  3 |  1 |
;; |  2 | 99 |

  (tables/dataset [{:a 1 :b [1 2 3]} {:a 2 :b [3 4]}])
;; => _unnamed [2 2]:

;; |      :b | :a |
;; |---------|---:|
;; | [1 2 3] |  1 |
;; |   [3 4] |  2 |

;; Missing values are marked by nil
  (tables/dataset [{:a nil :b 1} {:a 3 :b 4} {:a 11}])
;; => _unnamed [3 2]:

;; | :b | :a |
;; |---:|---:|
;; |  1 |    |
;; |  4 |  3 |
;; |    | 11 |

;; Reading from arrays, by default :as-rows
  (-> (map int-array [[1 2] [3 4] [5 6]])
      (into-array)
      (tables/dataset))
;; => :_unnamed [3 2]:
;;    | 0 | 1 |
;;    |--:|--:|
;;    | 1 | 2 |
;;    | 3 | 4 |
;;    | 5 | 6 |

  ;; :as-columns
  (-> (map int-array [[1 2] [3 4] [5 6]])
      (into-array)
      (tables/dataset {:layout :as-columns}))
;; => :_unnamed [2 3]:
;;    | 0 | 1 | 2 |
;;    |--:|--:|--:|
;;    | 1 | 3 | 5 |
;;    | 2 | 4 | 6 |

;; :as-rows with names
  (-> (map int-array [[1 2] [3 4] [5 6]])
      (into-array)
      (tables/dataset {:layout :as-rows
                       :column-names [:a :b]}))
;; => :_unnamed [3 2]:
;;    | :a | :b |
;;    |---:|---:|
;;    |  1 |  2 |
;;    |  3 |  4 |
;;    |  5 |  6 |

;; Create dataset using macro let-dataset to simulate R tibble function. Each binding is converted into a column.
  (tables/let-dataset [x (range 1 6)
                       y 1
                       z (datatype/+ x y)])
;; => _unnamed [5 3]:
;;    | :x | :y | :z |
;;    |---:|---:|---:|
;;    |  1 |  1 |  2 |
;;    |  2 |  1 |  3 |
;;    |  3 |  1 |  4 |
;;    |  4 |  1 |  5 |
;;    |  5 |  1 |  6 |

  ;; Import CSV file
  (tables/dataset "data/family.csv")

;; Import from URL
  (defonce seattle-weather-dataset
    (tables/dataset "https://vega.github.io/vega-lite/examples/data/seattle-weather.csv"))
  ;; => #'practicalli.tablecloth-basics/seattle-weather-dataset

  ;; Export dataset to a file or output stream can be done by calling tables/write!. Function accepts:
  ;; dataset
  ;; file name with one of the extensions: .csv, .tsv, .csv.gz and .tsv.gz or output stream
  ;; options:
  ;; :separator - string or separator char.
  (tables/write! seattle-weather-dataset "output.tsv.gz")

  ;; Check the file exists
  (.exists (io/file "output.tsv.gz")))


;; clojums and tables

(def DS (tables/dataset {:V1 (take 9 (cycle [1 2]))
                      :V2 (range 1 10)
                      :V3 (take 9 (cycle [0.5 1.0 1.5]))
                      :V4 (take 9 (cycle ["A" "B" "C"]))}))


DS


(clojure.pprint/pprint (take 2 (tables/rows seattle-weather-dataset :as-maps)))
;; => nil

;; (fn arg (fn arg ()))


;; List of columns in grouped dataset
(-> DS
    (tables/group-by ,,, :V1)

    #_(tables/column-names ,,,))

;; => _unnamed [2 3]:
;;    | :group-id | :name |           :data |
;;    |----------:|------:|-----------------|
;;    |         0 |     1 | Group: 1 [5 4]: |
;;    |         1 |     2 | Group: 2 [4 4]: |


;; => (:V1 :V2 :V3 :V4)


;; List of columns in grouped dataset treated as regular dataset

(-> DS
    (tables/group-by :V1)
    (tables/as-regular-dataset)
    (tables/column-names))

;; => (:group-id :name :data)

;; Content of the grouped dataset
(tables/columns (tables/group-by DS :V1) :as-map)


;; limited to 25 rows
(-> seattle-weather-dataset
    println)

;; Group by one or more column names
(-> seattle-weather-dataset
    (tables/group-by ,,, ["weather"]))
;; => _unnamed [5 3]:
;;    | :group-id |                 :name |                                :data |
;;    |----------:|-----------------------|--------------------------------------|
;;    |         0 | {"weather" "drizzle"} | Group: {"weather" "drizzle"} [53 6]: |
;;    |         1 |    {"weather" "rain"} |   Group: {"weather" "rain"} [641 6]: |
;;    |         2 |     {"weather" "sun"} |    Group: {"weather" "sun"} [640 6]: |
;;    |         3 |    {"weather" "snow"} |    Group: {"weather" "snow"} [26 6]: |
;;    |         4 |     {"weather" "fog"} |    Group: {"weather" "fog"} [101 6]: |


;; Use head to have a view of each group limited to a number
(-> seattle-weather-dataset
    (tables/group-by ,,, ["weather"])
    (tables/head 3));; => _unnamed [5 3]:
;;    | :group-id |                 :name |                               :data |
;;    |----------:|-----------------------|-------------------------------------|
;;    |         0 | {"weather" "drizzle"} | Group: {"weather" "drizzle"} [3 6]: |
;;    |         1 |    {"weather" "rain"} |    Group: {"weather" "rain"} [3 6]: |
;;    |         2 |     {"weather" "sun"} |     Group: {"weather" "sun"} [3 6]: |
;;    |         3 |    {"weather" "snow"} |    Group: {"weather" "snow"} [3 6]: |
;;    |         4 |     {"weather" "fog"} |     Group: {"weather" "fog"} [3 6]: |

(-> seattle-weather-dataset
    (tables/group-by ,,, ["weather"])
    (tables/column-names))
;; => ("date" "precipitation" "temp_max" "temp_min" "wind" "weather")

(require '[tech.v3.dataset.print :as dataset-print])

(-> seattle-weather-dataset
    (tables/group-by ,,, ["weather"])
    (dataset-print/print-policy :repl)
    (tables/head 3))
;; => _unnamed [5 3]:
;;    | :group-id |                 :name |                                                                        :data |
;;    |----------:|-----------------------|------------------------------------------------------------------------------|
;;    |         0 | {"weather" "drizzle"} | Group: {"weather" "drizzle"} [3 6]:                                          |
;;    |           |                       |                                                                              |
;;    |           |                       | \|       date \| precipitation \| temp_max \| temp_min \| wind \| weather \| |
;;    |           |                       | \|------------\|--------------:\|---------:\|---------:\|-----:\|---------\| |
;;    |           |                       | \| 2012-01-01 \|           0.0 \|     12.8 \|      5.0 \|  4.7 \| drizzle \| |
;;    |           |                       | \| 2012-01-27 \|           0.0 \|      6.7 \|     -2.2 \|  1.4 \| drizzle \| |
;;    |           |                       | \| 2012-02-15 \|           0.0 \|      7.2 \|      0.6 \|  1.8 \| drizzle \| |
;;    |         1 |    {"weather" "rain"} | Group: {"weather" "rain"} [3 6]:                                             |
;;    |           |                       |                                                                              |
;;    |           |                       | \|       date \| precipitation \| temp_max \| temp_min \| wind \| weather \| |
;;    |           |                       | \|------------\|--------------:\|---------:\|---------:\|-----:\|---------\| |
;;    |           |                       | \| 2012-01-02 \|          10.9 \|     10.6 \|      2.8 \|  4.5 \|    rain \| |
;;    |           |                       | \| 2012-01-03 \|           0.8 \|     11.7 \|      7.2 \|  2.3 \|    rain \| |
;;    |           |                       | \| 2012-01-04 \|          20.3 \|     12.2 \|      5.6 \|  4.7 \|    rain \| |
;;    |         2 |     {"weather" "sun"} | Group: {"weather" "sun"} [3 6]:                                              |
;;    |           |                       |                                                                              |
;;    |           |                       | \|       date \| precipitation \| temp_max \| temp_min \| wind \| weather \| |
;;    |           |                       | \|------------\|--------------:\|---------:\|---------:\|-----:\|---------\| |
;;    |           |                       | \| 2012-01-08 \|           0.0 \|     10.0 \|      2.8 \|  2.0 \|     sun \| |
;;    |           |                       | \| 2012-01-11 \|           0.0 \|      6.1 \|     -1.1 \|  5.1 \|     sun \| |
;;    |           |                       | \| 2012-01-12 \|           0.0 \|      6.1 \|     -1.7 \|  1.9 \|     sun \| |
;;    |         3 |    {"weather" "snow"} | Group: {"weather" "snow"} [3 6]:                                             |
;;    |           |                       |                                                                              |
;;    |           |                       | \|       date \| precipitation \| temp_max \| temp_min \| wind \| weather \| |
;;    |           |                       | \|------------\|--------------:\|---------:\|---------:\|-----:\|---------\| |
;;    |           |                       | \| 2012-01-14 \|           4.1 \|      4.4 \|      0.6 \|  5.3 \|    snow \| |
;;    |           |                       | \| 2012-01-15 \|           5.3 \|      1.1 \|     -3.3 \|  3.2 \|    snow \| |
;;    |           |                       | \| 2012-01-16 \|           2.5 \|      1.7 \|     -2.8 \|  5.0 \|    snow \| |
;;    |         4 |     {"weather" "fog"} | Group: {"weather" "fog"} [3 6]:                                              |
;;    |           |                       |                                                                              |
;;    |           |                       | \|       date \| precipitation \| temp_max \| temp_min \| wind \| weather \| |
;;    |           |                       | \|------------\|--------------:\|---------:\|---------:\|-----:\|---------\| |
;;    |           |                       | \| 2012-07-11 \|           0.0 \|     27.8 \|     13.3 \|  2.9 \|     fog \| |
;;    |           |                       | \| 2012-09-17 \|           0.0 \|     27.8 \|     11.7 \|  2.2 \|     fog \| |
;;    |           |                       | \| 2012-09-23 \|           0.0 \|     19.4 \|     10.0 \|  1.4 \|     fog \| |


;; Aggregate

(-> seattle-weather-dataset
    (tables/group-by ["weather"])
    (dataset-print/print-policy :repl)
    (tables/head 3)
    (tables/aggregate {:count tables/row-count}))
;; => _unnamed [5 2]:
;;    | :count | weather |
;;    |-------:|---------|
;;    |      3 | drizzle |
;;    |      3 |    rain |
;;    |      3 |     sun |
;;    |      3 |    snow |
;;    |      3 |     fog |

;; without the head
(-> seattle-weather-dataset
    (tables/group-by ["weather"])
    (dataset-print/print-policy :repl)
    (tables/aggregate {:count tables/row-count}))
;; => _unnamed [5 2]:
;;    | :count | weather |
;;    |-------:|---------|
;;    |     53 | drizzle |
;;    |    641 |    rain |
;;    |    640 |     sun |
;;    |     26 |    snow |
;;    |    101 |     fog |
