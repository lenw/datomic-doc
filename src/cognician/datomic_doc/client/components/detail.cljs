(ns cognician.datomic-doc.client.components.detail
  (:require [clojure.string :as string]
            [rum.core :as rum]
            [cognician.datomic-doc.client.util :as util]))

(rum/defc metadata [lookup-type 
                    {:keys [db/valueType db/cardinality db/unique db/index
                            db/isComponent db/noHistory db/fulltext deprecated?]}
                    {:keys [created last-touched datom-count]}]
  [:.box.metadata
   [:strong "Created: "] (util/format-date created) ". "
   (when last-touched
     [:strong {:key "last-touched"} 
      "Last touched: " (util/format-date last-touched) ". "])
   (when datom-count
     [:strong {:key "appearances"} 
      "Appearances: " (util/format-number datom-count) "."])
   (when (and (= :enum lookup-type) deprecated?)
     [:div.tags-list
      [:span.tag.is-danger "Deprecated"]])
   (when (= :schema lookup-type)
     [:div.tags-list
      (when deprecated? 
        [:span.tag.is-danger "Deprecated"])
      [:span.tag.is-primary (util/kw->label valueType)]
      (when (= cardinality :db.cardinality/many)
        [:span.tag.is-warning "Many"])
      (when unique
        [:span.tag.is-info "Unique: " (util/kw->label unique)])
      (when (and index (not unique) (not= valueType :db.type/ref))
        [:span.tag "Indexed"])
      (when isComponent
        [:span.tag "Component"])
      (when noHistory
        [:span.tag "No History"])
      (when fulltext
        [:span.tag "Full-text Indexed"])])])

(rum/defc detail [state]
  (let [{:keys [routes route-params read-only? lookup-type lookup-ref 
                entity entity-stats]} @state]
    [:div.container
     [:section.section
      [:h1.title "Datomic Doc"]
      [:nav.nav
       [:.nav-left.nav-menu
        [:span.nav-item
         [:a.button {:href (util/path-for routes :search route-params)} "Search"]
         (when (contains? #{:schema :enum} lookup-type)
           (let [query (str (namespace lookup-ref) "/")]
             [:a.button {:href (util/path-for routes :search-with-query 
                                              (assoc route-params :query query))}
              "Search \"" query "\""]))]]
       (when-not read-only?
         [:.nav-right.nav-menu
          [:span.nav-item
           [:a.button {:href (util/path-for routes (if (:ns route-params) 
                                                     :ident-edit-with-ns 
                                                     :ident-edit) 
                                            route-params)}
            "Edit :db/doc"]]])]
      [:h1.title 
       [:strong (util/kw->label lookup-type)] " "
       (if (= :entity lookup-type)
         (string/join " " lookup-ref)
         (str lookup-ref))]
      [:hr]
      (metadata lookup-type entity entity-stats)
      [:hr]
      (if-let [doc (:db/doc entity)]
        [:div doc]
        [:p "No documentation yet."])]]))
