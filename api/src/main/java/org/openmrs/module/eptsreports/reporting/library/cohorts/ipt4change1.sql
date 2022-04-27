SELECT     p.patient_id
FROM       patient p
INNER JOIN encounter e
ON         e.patient_id = p.patient_id
INNER JOIN obs o
ON         o.encounter_id = e.encounter_id
INNER JOIN
           (
                      SELECT     p.patient_id,
                                 Min(e.encounter_datetime) inh_start
                      FROM       patient p
                      INNER JOIN encounter e
                      ON         e.patient_id = p.patient_id
                      INNER JOIN obs o
                      ON         o.encounter_id = e.encounter_id
                      INNER JOIN obs o2
                      ON         o2.encounter_id = e.encounter_id
                      WHERE      p.voided = 0
                      AND        e.voided = 0
                      AND        o.voided = 0
                      AND        e.location_id = :location
                      AND        e.encounter_type = ${60}
                      AND        (
                                            o.concept_id = ${23985}
                                 AND        o.value_coded IN (${656},
                                                              ${23982}))
                      AND        ((
                                                       o2.concept_id = ${23987}
                                            AND        (
                                                                  o2.value_coded = ${1257}
                                                       OR         o2.value_coded IS NULL))
                                 OR         o2.concept_id NOT IN
                                            (
                                                   SELECT oo.concept_id
                                                   FROM   obs oo
                                                   WHERE  oo.voided = 0
                                                   AND    oo.encounter_id = e.encounter_id
                                                   AND    oo.concept_id = ${23987} ))
                      AND        e.encounter_datetime BETWEEN date_sub(:endDate, interval 210 day) AND        :endDate
                      GROUP BY   p.patient_id) AS inh_start_date
ON         inh_start_date.patient_id = p.patient_id
WHERE      p.patient_id NOT IN
           (
                      SELECT     p.patient_id
                      FROM       patient p
                      INNER JOIN encounter e
                      ON         p.patient_id = e.patient_id
                      INNER JOIN obs o
                      ON         e.encounter_id = o.encounter_id
                      WHERE      p.voided = 0
                      AND        e.voided = 0
                      AND        o.voided = 0
                      AND        e.location_id = :location
                      AND        o.concept_id = ${23985}
                      AND        o.value_coded IN ( ${656},
                                                   ${23982} )
                      AND        e.encounter_datetime >= date_sub( inh_start_date.inh_start, interval 7 month)
                      AND        e.encounter_datetime < inh_start_date.inh_start)
UNION
SELECT     p.patient_id
FROM       patient p
INNER JOIN encounter e
ON         p.patient_id = e.patient_id
INNER JOIN obs o1
ON         e.encounter_id = o.encounter_id
INNER JOIN obs o2
ON         e.encounter_id = o2.encounter_id
INNER JOIN obs o3
ON         e.encounter_id = o3.encounter_id
WHERE      p.voided = 0
AND        e.voided = 0
AND        o1.voided = 0
AND        o2.voided = 0
AND        o3.voided = 0
AND        e.location_id = :location
AND        ( (
                                 o1.concept_id = ${6128}
                      AND        o1.value_datetime IS NOT NULL)
           OR         ((
                                            o2.concept_id = ${23985}
                                 AND        o2.value_coded = ${656})
                      AND        (
                                            o3.concept_id = ${6128}
                                 AND        o3.value_datetime IS NOT NULL))
           AND        e.encounter_type = ${53}
           AND        e.encounter_datetime >= date_sub( inh_start_date.inh_start, interval 7 month)
           AND        e.encounter_datetime < inh_start_date.inh_start
           UNION
           SELECT     p.patient_id
           FROM       patient p
           INNER JOIN encounter e
           ON         p.patient_id = e.patient_id
           INNER JOIN obs o4
           ON         e.encounter_id = o4.encounter_id
           INNER JOIN obs o5
           ON         e.encounter_id = o5.encounter_id
           INNER JOIN obs o6
           ON         e.encounter_id = o6.encounter_id
           WHERE      p.voided = 0
           AND        e.voided = 0
           AND        o4.voided = 0
           AND        o5.voided = 0
           AND        o6.voided = 0
           AND        e.location_id = :location
           AND        ((
                                            o4.concept_id = ${6122}
                                 AND        o4.value_coded = ${1256})
                      OR         ((
                                                       o5.concept_id = ${23985}
                                            AND        o5.value_coded = ${656} )
                                 AND        (
                                                       o6.concept_id = ${165308}
                                            AND        o6.value_coded = ${1256})) )
           AND        e.encounter_type = 6
           AND        e.encounter_datetime >= date_sub( inh_start_date.inh_start, interval 7 month)
           AND        e.encounter_datetime < inh_start_date.inh_start
           UNION
           SELECT     p.patient_id
           FROM       patient p
           INNER JOIN encounter e
           ON         p.patient_id = e.patient_id
           INNER JOIN obs o7
           ON         e.encounter_id = o7.encounter_id
           WHERE      p.voided = 0
           AND        e.voided = 0
           AND        o7.voided = 0
           AND        e.location_id = :location
           AND        e.encounter_type IN( ${6},
                                          ${9} )
           AND        o7.concept_id = 6128
           AND        o7.value_datetime IS NOT NULL
           AND        e.encounter_datetime <= date_sub( inh_start_date.inh_start, interval 7 month)
           AND        e.encounter_datetime < inh_start_date.inh_start )
GROUP BY   p.patient_id;




















String query = ""
+ "SELECT     p.patient_id "
+ "FROM       patient p "
+ "INNER JOIN encounter e "
+ "ON         e.patient_id = p.patient_id "
+ "INNER JOIN obs o "
+ "ON         o.encounter_id = e.encounter_id "
+ "INNER JOIN "
+ "           ( "
+ "                      SELECT     p.patient_id, "
+ "                                 Min(e.encounter_datetime) inh_start "
+ "                      FROM       patient p "
+ "                      INNER JOIN encounter e "
+ "                      ON         e.patient_id = p.patient_id "
+ "                      INNER JOIN obs o "
+ "                      ON         o.encounter_id = e.encounter_id "
+ "                      INNER JOIN obs o2 "
+ "                      ON         o2.encounter_id = e.encounter_id "
+ "                      WHERE      p.voided = 0 "
+ "                      AND        e.voided = 0 "
+ "                      AND        o.voided = 0 "
+ "                      AND        e.location_id = :location "
+ "                      AND        e.encounter_type = ${60} "
+ "                      AND        ( "
+ "                                            o.concept_id = ${23985} "
+ "                                 AND        o.value_coded IN (${656}, "
+ "                                                              ${23982})) "
+ "                      AND        (( "
+ "                                                       o2.concept_id = ${23987} "
+ "                                            AND        ( "
+ "                                                                  o2.value_coded = ${1257} "
+ "                                                       OR         o2.value_coded IS NULL)) "
+ "                                 OR         o2.concept_id NOT IN "
+ "                                            ( "
+ "                                                   SELECT oo.concept_id "
+ "                                                   FROM   obs oo "
+ "                                                   WHERE  oo.voided = 0 "
+ "                                                   AND    oo.encounter_id = e.encounter_id "
+ "                                                   AND    oo.concept_id = ${23987} )) "
+ "                      AND        e.encounter_datetime BETWEEN date_sub(:endDate, interval 210 day) AND        :endDate "
+ "                      GROUP BY   p.patient_id) AS inh_start_date "
+ "ON         inh_start_date.patient_id = p.patient_id "
+ "WHERE      p.patient_id NOT IN "
+ "           ( "
+ "                      SELECT     p.patient_id "
+ "                      FROM       patient p "
+ "                      INNER JOIN encounter e "
+ "                      ON         p.patient_id = e.patient_id "
+ "                      INNER JOIN obs o "
+ "                      ON         e.encounter_id = o.encounter_id "
+ "                      WHERE      p.voided = 0 "
+ "                      AND        e.voided = 0 "
+ "                      AND        o.voided = 0 "
+ "                      AND        e.location_id = :location "
+ "                      AND        o.concept_id = ${23985} "
+ "                      AND        o.value_coded IN ( ${656}, "
+ "                                                   ${23982} ) "
+ "                      AND        e.encounter_datetime >= date_sub( inh_start_date.inh_start, interval 7 month) "
+ "                      AND        e.encounter_datetime < inh_start_date.inh_start) "
+ "UNION "
+ "SELECT     p.patient_id "
+ "FROM       patient p "
+ "INNER JOIN encounter e "
+ "ON         p.patient_id = e.patient_id "
+ "INNER JOIN obs o1 "
+ "ON         e.encounter_id = o.encounter_id "
+ "INNER JOIN obs o2 "
+ "ON         e.encounter_id = o2.encounter_id "
+ "INNER JOIN obs o3 "
+ "ON         e.encounter_id = o3.encounter_id "
+ "WHERE      p.voided = 0 "
+ "AND        e.voided = 0 "
+ "AND        o1.voided = 0 "
+ "AND        o2.voided = 0 "
+ "AND        o3.voided = 0 "
+ "AND        e.location_id = :location "
+ "AND        ( ( "
+ "                                 o1.concept_id = ${6128} "
+ "                      AND        o1.value_datetime IS NOT NULL) "
+ "           OR         (( "
+ "                                            o2.concept_id = ${23985} "
+ "                                 AND        o2.value_coded = ${656}) "
+ "                      AND        ( "
+ "                                            o3.concept_id = ${6128} "
+ "                                 AND        o3.value_datetime IS NOT NULL)) "
+ "           AND        e.encounter_type = ${53} "
+ "           AND        e.encounter_datetime >= date_sub( inh_start_date.inh_start, interval 7 month) "
+ "           AND        e.encounter_datetime < inh_start_date.inh_start "
+ "           UNION "
+ "           SELECT     p.patient_id "
+ "           FROM       patient p "
+ "           INNER JOIN encounter e "
+ "           ON         p.patient_id = e.patient_id "
+ "           INNER JOIN obs o4 "
+ "           ON         e.encounter_id = o4.encounter_id "
+ "           INNER JOIN obs o5 "
+ "           ON         e.encounter_id = o5.encounter_id "
+ "           INNER JOIN obs o6 "
+ "           ON         e.encounter_id = o6.encounter_id "
+ "           WHERE      p.voided = 0 "
+ "           AND        e.voided = 0 "
+ "           AND        o4.voided = 0 "
+ "           AND        o5.voided = 0 "
+ "           AND        o6.voided = 0 "
+ "           AND        e.location_id = :location "
+ "           AND        (( "
+ "                                            o4.concept_id = ${6122} "
+ "                                 AND        o4.value_coded = ${1256}) "
+ "                      OR         (( "
+ "                                                       o5.concept_id = ${23985} "
+ "                                            AND        o5.value_coded = ${656} ) "
+ "                                 AND        ( "
+ "                                                       o6.concept_id = ${165308} "
+ "                                            AND        o6.value_coded = ${1256})) ) "
+ "           AND        e.encounter_type = 6 "
+ "           AND        e.encounter_datetime >= date_sub( inh_start_date.inh_start, interval 7 month) "
+ "           AND        e.encounter_datetime < inh_start_date.inh_start "
+ "           UNION "
+ "           SELECT     p.patient_id "
+ "           FROM       patient p "
+ "           INNER JOIN encounter e "
+ "           ON         p.patient_id = e.patient_id "
+ "           INNER JOIN obs o7 "
+ "           ON         e.encounter_id = o7.encounter_id "
+ "           WHERE      p.voided = 0 "
+ "           AND        e.voided = 0 "
+ "           AND        o7.voided = 0 "
+ "           AND        e.location_id = :location "
+ "           AND        e.encounter_type IN( ${6}, "
+ "                                          ${9} ) "
+ "           AND        o7.concept_id = 6128 "
+ "           AND        o7.value_datetime IS NOT NULL "
+ "           AND        e.encounter_datetime <= date_sub( inh_start_date.inh_start, interval 7 month) "
+ "           AND        e.encounter_datetime < inh_start_date.inh_start ) "
+ "GROUP BY   p.patient_id;";

use fgh;
##############################

SELECT     p.patient_id
FROM       patient p
INNER JOIN encounter e
ON         e.patient_id = p.patient_id
INNER JOIN obs o
ON         o.encounter_id = e.encounter_id
INNER JOIN
           (
                      SELECT     p.patient_id,
                                 Min(e.encounter_datetime) as inh_start
                      FROM       patient p
                      INNER JOIN encounter e
                      ON         e.patient_id = p.patient_id
                      INNER JOIN obs o
                      ON         o.encounter_id = e.encounter_id
                      INNER JOIN obs o2
                      ON         o2.encounter_id = e.encounter_id
                      WHERE      p.voided = 0
                      AND        e.voided = 0
                      AND        o.voided = 0
                      AND        e.location_id = 400
                      AND        e.encounter_type = 60
                      AND        (
                                            o.concept_id = 23985
                                 AND        o.value_coded IN (656,
                                                              23982))
                      AND        ((
                                                       o2.concept_id = 23987
                                            AND        (
                                                                  o2.value_coded = 1257
                                                       OR         o2.value_coded IS NULL))
                                 OR         o2.concept_id NOT IN
                                            (
                                                   SELECT oo.concept_id
                                                   FROM   obs oo
                                                   WHERE  oo.voided = 0
                                                   AND    oo.encounter_id = e.encounter_id
                                                   AND    oo.concept_id = 23987 ))
                      AND        e.encounter_datetime BETWEEN date_sub('2020-01-30', interval 210 day) AND        '2020-01-30'
                      GROUP BY   p.patient_id) inh_start_date
ON         inh_start_date.patient_id = p.patient_id
WHERE      p.patient_id NOT IN
           (
                      SELECT     p.patient_id
                      FROM       patient p
                      INNER JOIN encounter e
                      ON         p.patient_id = e.patient_id
                      INNER JOIN obs o
                      ON         e.encounter_id = o.encounter_id
                      WHERE      p.voided = 0
                      AND        e.voided = 0
                      AND        o.voided = 0
                      AND        e.location_id = 400
                      AND        o.concept_id = 23985
                      AND        o.value_coded IN ( 656,
                                                   23982 )
                      AND        e.encounter_datetime >= date_sub(inh_start_date.inh_start, interval 7 month)
                      AND        e.encounter_datetime < inh_start_date.inh_start)
UNION
SELECT     p.patient_id
FROM       patient p
INNER JOIN encounter e
ON         p.patient_id = e.patient_id
INNER JOIN obs o1
ON         e.encounter_id = o.encounter_id
INNER JOIN obs o2
ON         e.encounter_id = o2.encounter_id
INNER JOIN obs o3
ON         e.encounter_id = o3.encounter_id
WHERE      p.voided = 0
AND        e.voided = 0
AND        o1.voided = 0
AND        o2.voided = 0
AND        o3.voided = 0
AND        e.location_id = 400
AND        ( (
                                 o1.concept_id = 6128
                      AND        o1.value_datetime IS NOT NULL)
           OR         ((
                                            o2.concept_id = 23985
                                 AND        o2.value_coded = 656)
                      AND        (
                                            o3.concept_id = 6128
                                 AND        o3.value_datetime IS NOT NULL))
           AND        e.encounter_type = 53
           AND        e.encounter_datetime >= date_sub( inh_start_date.inh_start, interval 7 month)
           AND        e.encounter_datetime < inh_start_date.inh_start)
           UNION
           SELECT     p.patient_id
           FROM       patient p
           INNER JOIN encounter e
           ON         p.patient_id = e.patient_id
           INNER JOIN obs o4
           ON         e.encounter_id = o4.encounter_id
           INNER JOIN obs o5
           ON         e.encounter_id = o5.encounter_id
           INNER JOIN obs o6
           ON         e.encounter_id = o6.encounter_id
           WHERE      p.voided = 0
           AND        e.voided = 0
           AND        o4.voided = 0
           AND        o5.voided = 0
           AND        o6.voided = 0
           AND        e.location_id = 400
           AND        ((
                                            o4.concept_id = 6122
                                 AND        o4.value_coded = 1256)
                      OR         ((
                                                       o5.concept_id = 23985
                                            AND        o5.value_coded = 656 )
                                 AND        (
                                                       o6.concept_id = 165308
                                            AND        o6.value_coded = 1256)) )
           AND        e.encounter_type = 6
           AND        e.encounter_datetime >= date_sub( inh_start_date.inh_start, interval 7 month)
           AND        e.encounter_datetime < inh_start_date.inh_start
           UNION
           SELECT     p.patient_id
           FROM       patient p
           INNER JOIN encounter e
           ON         p.patient_id = e.patient_id
           INNER JOIN obs o7
           ON         e.encounter_id = o7.encounter_id
           WHERE      p.voided = 0
           AND        e.voided = 0
           AND        o7.voided = 0
           AND        e.location_id = 400
           AND        e.encounter_type IN( 6,
                                          9 )
           AND        o7.concept_id = 6128
           AND        o7.value_datetime IS NOT NULL
           AND        e.encounter_datetime <= date_sub( inh_start_date.inh_start, interval 7 month)
           AND        e.encounter_datetime < inh_start_date.inh_start 
GROUP BY   p.patient_id;

