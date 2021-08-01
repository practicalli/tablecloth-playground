;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; TableCloth examples
;;
;; https://scicloj.github.io/tablecloth/index.html
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(ns practicalli.tablecloth-basics
  (:require [tablecloth.api :as tables]
            [tech.v3.datatype.functional :as datatype]
            [tech.v3.datatype.datetime :as datetime]
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

(-> seattle-weather-dataset
    (tables/head 3))

(-> seattle-weather-dataset
    (tables/group-by ,,, ["date"]))
;; => _unnamed [1461 3]:
;;    | :group-id |                                                         :name |                                                                       :data |
;;    |----------:|---------------------------------------------------------------|-----------------------------------------------------------------------------|
;;    |         0 | {"date" #object[java.time.LocalDate 0x3d53b27e "2012-01-01"]} |  Group: {"date" #object[java.time.LocalDate 0x1951e51 "2012-01-01"]} [1 6]: |
;;    |         1 | {"date" #object[java.time.LocalDate 0x6d0417c8 "2012-01-02"]} |  Group: {"date" #object[java.time.LocalDate 0x729f6c3 "2012-01-02"]} [1 6]: |
;;    |         2 |  {"date" #object[java.time.LocalDate 0xe257cfc "2012-01-03"]} | Group: {"date" #object[java.time.LocalDate 0x1e354650 "2012-01-03"]} [1 6]: |
;;    |         3 | {"date" #object[java.time.LocalDate 0x41ce5689 "2012-01-04"]} | Group: {"date" #object[java.time.LocalDate 0x1936d9aa "2012-01-04"]} [1 6]: |
;;    |         4 | {"date" #object[java.time.LocalDate 0x5a2fe8c5 "2012-01-05"]} | Group: {"date" #object[java.time.LocalDate 0x30783b4b "2012-01-05"]} [1 6]: |
;;    |         5 | {"date" #object[java.time.LocalDate 0x19fd99e1 "2012-01-06"]} | Group: {"date" #object[java.time.LocalDate 0x78af5c69 "2012-01-06"]} [1 6]: |
;;    |         6 |  {"date" #object[java.time.LocalDate 0x5314021 "2012-01-07"]} | Group: {"date" #object[java.time.LocalDate 0x4c9c82ea "2012-01-07"]} [1 6]: |
;;    |         7 | {"date" #object[java.time.LocalDate 0x40ae1d8c "2012-01-08"]} | Group: {"date" #object[java.time.LocalDate 0x6348e95a "2012-01-08"]} [1 6]: |
;;    |         8 |  {"date" #object[java.time.LocalDate 0xfd54f4c "2012-01-09"]} | Group: {"date" #object[java.time.LocalDate 0x129b341b "2012-01-09"]} [1 6]: |
;;    |         9 | {"date" #object[java.time.LocalDate 0x352cca0e "2012-01-10"]} | Group: {"date" #object[java.time.LocalDate 0x10557e35 "2012-01-10"]} [1 6]: |
;;    |        10 | {"date" #object[java.time.LocalDate 0x5f772747 "2012-01-11"]} | Group: {"date" #object[java.time.LocalDate 0x38f62507 "2012-01-11"]} [1 6]: |
;;    |        11 | {"date" #object[java.time.LocalDate 0x227cb9ce "2012-01-12"]} | Group: {"date" #object[java.time.LocalDate 0x1c50197a "2012-01-12"]} [1 6]: |
;;    |        12 | {"date" #object[java.time.LocalDate 0x3a44212e "2012-01-13"]} | Group: {"date" #object[java.time.LocalDate 0x5720dd07 "2012-01-13"]} [1 6]: |
;;    |        13 | {"date" #object[java.time.LocalDate 0x60ff8013 "2012-01-14"]} | Group: {"date" #object[java.time.LocalDate 0x6c3fb500 "2012-01-14"]} [1 6]: |
;;    |        14 | {"date" #object[java.time.LocalDate 0x73593d8e "2012-01-15"]} | Group: {"date" #object[java.time.LocalDate 0x65687b9d "2012-01-15"]} [1 6]: |
;;    |        15 | {"date" #object[java.time.LocalDate 0x380ec9f0 "2012-01-16"]} | Group: {"date" #object[java.time.LocalDate 0x79ef3583 "2012-01-16"]} [1 6]: |
;;    |        16 | {"date" #object[java.time.LocalDate 0x78f08708 "2012-01-17"]} | Group: {"date" #object[java.time.LocalDate 0x28f6075c "2012-01-17"]} [1 6]: |
;;    |        17 | {"date" #object[java.time.LocalDate 0x71573ec9 "2012-01-18"]} | Group: {"date" #object[java.time.LocalDate 0x39f020f7 "2012-01-18"]} [1 6]: |
;;    |        18 | {"date" #object[java.time.LocalDate 0x6c28c1e6 "2012-01-19"]} | Group: {"date" #object[java.time.LocalDate 0x1fe210c7 "2012-01-19"]} [1 6]: |
;;    |        19 | {"date" #object[java.time.LocalDate 0x4a48cf21 "2012-01-20"]} | Group: {"date" #object[java.time.LocalDate 0x1f217d95 "2012-01-20"]} [1 6]: |
;;    |        20 | {"date" #object[java.time.LocalDate 0x72206464 "2012-01-21"]} |  Group: {"date" #object[java.time.LocalDate 0x77545ca "2012-01-21"]} [1 6]: |
;;    |        21 | {"date" #object[java.time.LocalDate 0x5fa9bb28 "2012-01-22"]} | Group: {"date" #object[java.time.LocalDate 0x3fba2a4b "2012-01-22"]} [1 6]: |
;;    |        22 | {"date" #object[java.time.LocalDate 0x65a77349 "2012-01-23"]} | Group: {"date" #object[java.time.LocalDate 0x30d16153 "2012-01-23"]} [1 6]: |
;;    |        23 |  {"date" #object[java.time.LocalDate 0xf3bfcf3 "2012-01-24"]} | Group: {"date" #object[java.time.LocalDate 0x24887f09 "2012-01-24"]} [1 6]: |
;;    |        24 | {"date" #object[java.time.LocalDate 0x458f6909 "2012-01-25"]} |  Group: {"date" #object[java.time.LocalDate 0x97e2547 "2012-01-25"]} [1 6]: |

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

(def seattle-weather-keywords
  (tables/dataset "https://vega.github.io/vega-lite/examples/data/seattle-weather.csv" {:key-fn keyword}))


(-> seattle-weather-dataset
    (tables/group-by ,,, [""]))

(-> seattle-weather-dataset
    (tables/order-by ["date"] :desc ))
;; => https://vega.github.io/vega-lite/examples/data/seattle-weather.csv [1461 6]:
;;    |       date | precipitation | temp_max | temp_min | wind | weather |
;;    |------------|--------------:|---------:|---------:|-----:|---------|
;;    | 2015-12-31 |           0.0 |      5.6 |     -2.1 |  3.5 |     sun |
;;    | 2015-12-30 |           0.0 |      5.6 |     -1.0 |  3.4 |     sun |
;;    | 2015-12-29 |           0.0 |      7.2 |      0.6 |  2.6 |     fog |
;;    | 2015-12-28 |           1.5 |      5.0 |      1.7 |  1.3 |    rain |
;;    | 2015-12-27 |           8.6 |      4.4 |      1.7 |  2.9 |    rain |
;;    | 2015-12-26 |           0.0 |      4.4 |      0.0 |  2.5 |     sun |
;;    | 2015-12-25 |           5.8 |      5.0 |      2.2 |  1.5 |    rain |
;;    | 2015-12-24 |           2.5 |      5.6 |      2.2 |  4.3 |    rain |
;;    | 2015-12-23 |           6.1 |      5.0 |      2.8 |  7.6 |    rain |
;;    | 2015-12-22 |           4.6 |      7.8 |      2.8 |  5.0 |    rain |
;;    | 2015-12-21 |          27.4 |      5.6 |      2.8 |  4.3 |    rain |
;;    | 2015-12-20 |           4.3 |      7.8 |      4.4 |  6.7 |    rain |
;;    | 2015-12-19 |           0.0 |      8.3 |      2.8 |  4.1 |     fog |
;;    | 2015-12-18 |          18.5 |      8.9 |      4.4 |  5.1 |    rain |
;;    | 2015-12-17 |          21.8 |      6.7 |      3.9 |  6.0 |    rain |
;;    | 2015-12-16 |           3.6 |      6.1 |      2.8 |  2.3 |    rain |
;;    | 2015-12-15 |           1.5 |      6.7 |      1.1 |  2.9 |    rain |
;;    | 2015-12-14 |           0.0 |      7.8 |      1.7 |  1.7 |     sun |
;;    | 2015-12-13 |           1.3 |      7.8 |      6.1 |  6.1 |    rain |
;;    | 2015-12-12 |          16.0 |      8.9 |      5.6 |  5.6 |    rain |
;;    | 2015-12-11 |           0.3 |      9.4 |      4.4 |  2.8 |    rain |
;;    | 2015-12-10 |           9.4 |     11.7 |      6.1 |  7.5 |    rain |
;;    | 2015-12-09 |          13.5 |     12.2 |      7.8 |  6.3 |    rain |
;;    | 2015-12-08 |          54.1 |     15.6 |     10.0 |  6.2 |    rain |
;;    | 2015-12-07 |          27.4 |     11.1 |      8.3 |  3.4 |    rain |



(-> seattle-weather-keywords
    (tables/order-by [:date] :desc))
;; => https://vega.github.io/vega-lite/examples/data/seattle-weather.csv [1461 6]:
;;    |      :date | :precipitation | :temp_max | :temp_min | :wind | :weather |
;;    |------------|---------------:|----------:|----------:|------:|----------|
;;    | 2015-12-31 |            0.0 |       5.6 |      -2.1 |   3.5 |      sun |
;;    | 2015-12-30 |            0.0 |       5.6 |      -1.0 |   3.4 |      sun |
;;    | 2015-12-29 |            0.0 |       7.2 |       0.6 |   2.6 |      fog |
;;    | 2015-12-28 |            1.5 |       5.0 |       1.7 |   1.3 |     rain |
;;    | 2015-12-27 |            8.6 |       4.4 |       1.7 |   2.9 |     rain |
;;    | 2015-12-26 |            0.0 |       4.4 |       0.0 |   2.5 |      sun |
;;    | 2015-12-25 |            5.8 |       5.0 |       2.2 |   1.5 |     rain |
;;    | 2015-12-24 |            2.5 |       5.6 |       2.2 |   4.3 |     rain |
;;    | 2015-12-23 |            6.1 |       5.0 |       2.8 |   7.6 |     rain |
;;    | 2015-12-22 |            4.6 |       7.8 |       2.8 |   5.0 |     rain |
;;    | 2015-12-21 |           27.4 |       5.6 |       2.8 |   4.3 |     rain |
;;    | 2015-12-20 |            4.3 |       7.8 |       4.4 |   6.7 |     rain |
;;    | 2015-12-19 |            0.0 |       8.3 |       2.8 |   4.1 |      fog |
;;    | 2015-12-18 |           18.5 |       8.9 |       4.4 |   5.1 |     rain |
;;    | 2015-12-17 |           21.8 |       6.7 |       3.9 |   6.0 |     rain |
;;    | 2015-12-16 |            3.6 |       6.1 |       2.8 |   2.3 |     rain |
;;    | 2015-12-15 |            1.5 |       6.7 |       1.1 |   2.9 |     rain |
;;    | 2015-12-14 |            0.0 |       7.8 |       1.7 |   1.7 |      sun |
;;    | 2015-12-13 |            1.3 |       7.8 |       6.1 |   6.1 |     rain |
;;    | 2015-12-12 |           16.0 |       8.9 |       5.6 |   5.6 |     rain |
;;    | 2015-12-11 |            0.3 |       9.4 |       4.4 |   2.8 |     rain |
;;    | 2015-12-10 |            9.4 |      11.7 |       6.1 |   7.5 |     rain |
;;    | 2015-12-09 |           13.5 |      12.2 |       7.8 |   6.3 |     rain |
;;    | 2015-12-08 |           54.1 |      15.6 |      10.0 |   6.2 |     rain |
;;    | 2015-12-07 |           27.4 |      11.1 |       8.3 |   3.4 |     rain |

(-> seattle-weather-keywords
    (tables/order-by [:date] :asc))
;; => https://vega.github.io/vega-lite/examples/data/seattle-weather.csv [1461 6]:
;;    |      :date | :precipitation | :temp_max | :temp_min | :wind | :weather |
;;    |------------|---------------:|----------:|----------:|------:|----------|
;;    | 2012-01-01 |            0.0 |      12.8 |       5.0 |   4.7 |  drizzle |
;;    | 2012-01-02 |           10.9 |      10.6 |       2.8 |   4.5 |     rain |
;;    | 2012-01-03 |            0.8 |      11.7 |       7.2 |   2.3 |     rain |
;;    | 2012-01-04 |           20.3 |      12.2 |       5.6 |   4.7 |     rain |
;;    | 2012-01-05 |            1.3 |       8.9 |       2.8 |   6.1 |     rain |
;;    | 2012-01-06 |            2.5 |       4.4 |       2.2 |   2.2 |     rain |
;;    | 2012-01-07 |            0.0 |       7.2 |       2.8 |   2.3 |     rain |
;;    | 2012-01-08 |            0.0 |      10.0 |       2.8 |   2.0 |      sun |
;;    | 2012-01-09 |            4.3 |       9.4 |       5.0 |   3.4 |     rain |
;;    | 2012-01-10 |            1.0 |       6.1 |       0.6 |   3.4 |     rain |
;;    | 2012-01-11 |            0.0 |       6.1 |      -1.1 |   5.1 |      sun |
;;    | 2012-01-12 |            0.0 |       6.1 |      -1.7 |   1.9 |      sun |
;;    | 2012-01-13 |            0.0 |       5.0 |      -2.8 |   1.3 |      sun |
;;    | 2012-01-14 |            4.1 |       4.4 |       0.6 |   5.3 |     snow |
;;    | 2012-01-15 |            5.3 |       1.1 |      -3.3 |   3.2 |     snow |
;;    | 2012-01-16 |            2.5 |       1.7 |      -2.8 |   5.0 |     snow |
;;    | 2012-01-17 |            8.1 |       3.3 |       0.0 |   5.6 |     snow |
;;    | 2012-01-18 |           19.8 |       0.0 |      -2.8 |   5.0 |     snow |
;;    | 2012-01-19 |           15.2 |      -1.1 |      -2.8 |   1.6 |     snow |
;;    | 2012-01-20 |           13.5 |       7.2 |      -1.1 |   2.3 |     snow |
;;    | 2012-01-21 |            3.0 |       8.3 |       3.3 |   8.2 |     rain |
;;    | 2012-01-22 |            6.1 |       6.7 |       2.2 |   4.8 |     rain |
;;    | 2012-01-23 |            0.0 |       8.3 |       1.1 |   3.6 |     rain |
;;    | 2012-01-24 |            8.6 |      10.0 |       2.2 |   5.1 |     rain |
;;    | 2012-01-25 |            8.1 |       8.9 |       4.4 |   5.4 |     rain |


;; Research question: How much warmer does it get during the summer
;; ??

(-> seattle-weather-keywords
    (tables/order-by [:date] :asc))

;;
(->> seattle-weather-keywords
   :date
    (datetime/long-temporal-field :years ))

(-> seattle-weather-keywords
     (tables/add-columns {:summer (->> seattle-weather-keywords
                                       :date
                                       (datetime/long-temporal-field :months)
                                       #{6 7 8}
                                       some?)
                          :year   (->> seattle-weather-keywords
                                       :date
                                       (datetime/long-temporal-field :years))}))
;; => https://vega.github.io/vega-lite/examples/data/seattle-weather.csv [1461 8]:
;;    |      :date | :precipitation | :temp_max | :temp_min | :wind | :weather | :summer | :year |
;;    |------------|---------------:|----------:|----------:|------:|----------|---------|------:|
;;    | 2012-01-01 |            0.0 |      12.8 |       5.0 |   4.7 |  drizzle |   false |  2012 |
;;    | 2012-01-02 |           10.9 |      10.6 |       2.8 |   4.5 |     rain |   false |  2012 |
;;    | 2012-01-03 |            0.8 |      11.7 |       7.2 |   2.3 |     rain |   false |  2012 |
;;    | 2012-01-04 |           20.3 |      12.2 |       5.6 |   4.7 |     rain |   false |  2012 |
;;    | 2012-01-05 |            1.3 |       8.9 |       2.8 |   6.1 |     rain |   false |  2012 |
;;    | 2012-01-06 |            2.5 |       4.4 |       2.2 |   2.2 |     rain |   false |  2012 |
;;    | 2012-01-07 |            0.0 |       7.2 |       2.8 |   2.3 |     rain |   false |  2012 |
;;    | 2012-01-08 |            0.0 |      10.0 |       2.8 |   2.0 |      sun |   false |  2012 |
;;    | 2012-01-09 |            4.3 |       9.4 |       5.0 |   3.4 |     rain |   false |  2012 |
;;    | 2012-01-10 |            1.0 |       6.1 |       0.6 |   3.4 |     rain |   false |  2012 |
;;    | 2012-01-11 |            0.0 |       6.1 |      -1.1 |   5.1 |      sun |   false |  2012 |
;;    | 2012-01-12 |            0.0 |       6.1 |      -1.7 |   1.9 |      sun |   false |  2012 |
;;    | 2012-01-13 |            0.0 |       5.0 |      -2.8 |   1.3 |      sun |   false |  2012 |
;;    | 2012-01-14 |            4.1 |       4.4 |       0.6 |   5.3 |     snow |   false |  2012 |
;;    | 2012-01-15 |            5.3 |       1.1 |      -3.3 |   3.2 |     snow |   false |  2012 |
;;    | 2012-01-16 |            2.5 |       1.7 |      -2.8 |   5.0 |     snow |   false |  2012 |
;;    | 2012-01-17 |            8.1 |       3.3 |       0.0 |   5.6 |     snow |   false |  2012 |
;;    | 2012-01-18 |           19.8 |       0.0 |      -2.8 |   5.0 |     snow |   false |  2012 |
;;    | 2012-01-19 |           15.2 |      -1.1 |      -2.8 |   1.6 |     snow |   false |  2012 |
;;    | 2012-01-20 |           13.5 |       7.2 |      -1.1 |   2.3 |     snow |   false |  2012 |
;;    | 2012-01-21 |            3.0 |       8.3 |       3.3 |   8.2 |     rain |   false |  2012 |
;;    | 2012-01-22 |            6.1 |       6.7 |       2.2 |   4.8 |     rain |   false |  2012 |
;;    | 2012-01-23 |            0.0 |       8.3 |       1.1 |   3.6 |     rain |   false |  2012 |
;;    | 2012-01-24 |            8.6 |      10.0 |       2.2 |   5.1 |     rain |   false |  2012 |
;;    | 2012-01-25 |            8.1 |       8.9 |       4.4 |   5.4 |     rain |   false |  2012 |
;; summer: june july august

(-> seattle-weather-keywords
    (tables/add-columns {:summer (->> seattle-weather-keywords
                                      :date
                                      (datetime/long-temporal-field :months)
                                      (map #{6 7 8})
                                      (map some?))
                         :year   (->> seattle-weather-keywords
                                      :date
                                      (datetime/long-temporal-field :years))})
    (tables/group-by :summer)
    (tables/aggregate {:total-precipitation
                       (fn [dataset]
                         (-> dataset
                             :precipitation
                             tech.v3.datatype.functional/sum))}))
;; => _unnamed [2 2]:
;;    | :total-precipitation | :$group-name |
;;    |---------------------:|--------------|
;;    |               4081.2 |        false |
;;    |                344.8 |         true |







;; => _unnamed [2 3]:
;;    | :group-id | :name |                  :data |
;;    |----------:|-------|------------------------|
;;    |         0 | false | Group: false [1093 8]: |
;;    |         1 |  true |   Group: true [368 8]: |
;; => _unnamed [4 3]:
;;    | :group-id | :name |             :data |
;;    |----------:|------:|-------------------|
;;    |         0 |       | Group:  [1093 8]: |
;;    |         1 |     6 | Group: 6 [120 8]: |
;;    |         2 |     7 | Group: 7 [124 8]: |
;;    |         3 |     8 | Group: 8 [124 8]: |














;; (import [org.threeten.extra YearQuarter])
;; (def ausdata
;;   (-> "./data/aus-production.csv"
;;       (tbl/dataset
;;         {:dataset-name "aus-production"
;;          :key-fn keyword
;;          :parser-fn {"Quarter" [:year-quarter (fn [date-str] (-> date-str (replace #" " "-") (YearQuarter/parse)))]}})

;; ;; new column :QuarterEnd which is the last date of the :Quarter in localdate
;; (tbl/add-or-replace-column :QuarterEnd #(dtype/emap (fn [x] (.atEndOfQuarter x)) :local-date (:Quarter %)))
;; ;; indexed
;; (idx/index-by :Quarter)))


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
