SELECT Heading,	   
 IF(Id IS NULL, 0, SUM(IF(Persons = 'Children' AND Gender = 'M', 1, 0))) AS Children_Males, 
 IF(Id IS NULL, 0, SUM(IF(Persons = 'Children' AND Gender = 'F', 1, 0))) AS Children_Females,
 IF(Id IS NULL, 0, SUM(IF(Persons = 'Adults' AND Gender = 'M', 1, 0))) AS Adults_Males, 
 IF(Id IS NULL, 0, SUM(IF(Persons = 'Adults' AND Gender = 'F', 1, 0))) AS Adults_Females

FROM
    (    SELECT Id,Gender,Heading,Persons
        FROM(
        (SELECT  Id,Gender,'enrolled_This_Month' as Heading,'Children' as Persons
        FROM(
            select distinct o.person_id AS Id,
                                            patient_identifier.identifier AS patientIdentifier,
                                            floor(datediff(CAST('2020-07-31' AS DATE), person.birthdate)/365) AS Age,
                                            person.gender AS Gender,
                                            observed_age_group.name AS age_group
        from obs o 	
                INNER JOIN person ON person.person_id = o.person_id AND person.voided = 0
                INNER JOIN person_name ON person.person_id = person_name.person_id AND person_name.preferred = 1
                INNER JOIN patient_identifier ON patient_identifier.patient_id = person.person_id AND patient_identifier.identifier_type = 3 AND patient_identifier.preferred=1
                INNER JOIN reporting_age_group AS observed_age_group ON
                CAST('2020-07-31' AS DATE) BETWEEN (DATE_ADD(DATE_ADD(person.birthdate, INTERVAL observed_age_group.min_years YEAR), INTERVAL observed_age_group.min_days DAY))
                AND (DATE_ADD(DATE_ADD(person.birthdate, INTERVAL observed_age_group.max_years YEAR), INTERVAL observed_age_group.max_days DAY))
        WHERE observed_age_group.report_group_name = 'Modified_Ages'
        AND o.voided = 0
        AND MONTH(value_datetime) = MONTH(CAST('2020-07-31' AS DATE)) 
        AND YEAR(value_datetime) = YEAR(CAST('2020-07-31' AS DATE))
        and concept_id = 2223) enrolled_a
        WHERE age < 15)

        UNION
        (SELECT  Id,Gender,'enrolled_This_Month' as Heading,'Adults'
                FROM(
        select distinct o.person_id AS Id,
                                            patient_identifier.identifier AS patientIdentifier,
                                            floor(datediff(CAST('2020-07-31' AS DATE), person.birthdate)/365) AS Age,
                                            person.gender AS Gender,
                                            observed_age_group.name AS age_group
        from obs o 	
                INNER JOIN person ON person.person_id = o.person_id AND person.voided = 0
                INNER JOIN person_name ON person.person_id = person_name.person_id AND person_name.preferred = 1
                INNER JOIN patient_identifier ON patient_identifier.patient_id = person.person_id AND patient_identifier.identifier_type = 3 AND patient_identifier.preferred=1
                INNER JOIN reporting_age_group AS observed_age_group ON
                CAST('2020-07-31' AS DATE) BETWEEN (DATE_ADD(DATE_ADD(person.birthdate, INTERVAL observed_age_group.min_years YEAR), INTERVAL observed_age_group.min_days DAY))
                AND (DATE_ADD(DATE_ADD(person.birthdate, INTERVAL observed_age_group.max_years YEAR), INTERVAL observed_age_group.max_days DAY))
        WHERE observed_age_group.report_group_name = 'Modified_Ages'
        AND o.voided = 0
        AND MONTH(value_datetime) = MONTH(CAST('2020-07-31' AS DATE)) 
        AND YEAR(value_datetime) = YEAR(CAST('2020-07-31' AS DATE))
        and concept_id = 2223) enrolled_b
        WHERE age > 15) 

        UNION
        (SELECT  Id,Gender,'ever_enrolled_PreART' as Heading,'Children' as Persons
        FROM(select distinct o.person_id AS Id,
                patient_identifier.identifier AS patientIdentifier,
                floor(datediff(CAST('2020-07-31' AS DATE), person.birthdate)/365) AS Age,
                person.gender AS Gender,
                observed_age_group.name AS age_group
                from obs o 	
                        INNER JOIN person ON person.person_id = o.person_id AND person.voided = 0
                        INNER JOIN person_name ON person.person_id = person_name.person_id AND person_name.preferred = 1
                        INNER JOIN patient_identifier ON patient_identifier.patient_id = person.person_id AND patient_identifier.identifier_type = 3 AND patient_identifier.preferred=1
                        INNER JOIN reporting_age_group AS observed_age_group ON
                        CAST('2020-07-31' AS DATE) BETWEEN (DATE_ADD(DATE_ADD(person.birthdate, INTERVAL observed_age_group.min_years YEAR), INTERVAL observed_age_group.min_days DAY))
                        AND (DATE_ADD(DATE_ADD(person.birthdate, INTERVAL observed_age_group.max_years YEAR), INTERVAL observed_age_group.max_days DAY))
                WHERE observed_age_group.report_group_name = 'Modified_Ages'
                                AND o.voided = 0
                                and concept_id = 2223 and value_datetime < '2020-07-01'
        and o.person_id not in (
                                select person_id from obs
                                where concept_id in (2397,2403) 
                            )
                )preart_a
        WHERE age < 15)

        UNION

        (
        SELECT  Id,Gender,'ever_enrolled_PreART' as Heading,'Adults' as Persons
        FROM(select distinct o.person_id AS Id,
                patient_identifier.identifier AS patientIdentifier,
                floor(datediff(CAST('2020-07-31' AS DATE), person.birthdate)/365) AS Age,
                person.gender AS Gender,
                observed_age_group.name AS age_group
                from obs o 	
                        INNER JOIN person ON person.person_id = o.person_id AND person.voided = 0
                        INNER JOIN person_name ON person.person_id = person_name.person_id AND person_name.preferred = 1
                        INNER JOIN patient_identifier ON patient_identifier.patient_id = person.person_id AND patient_identifier.identifier_type = 3 AND patient_identifier.preferred=1
                        INNER JOIN reporting_age_group AS observed_age_group ON
                        CAST('2020-07-31' AS DATE) BETWEEN (DATE_ADD(DATE_ADD(person.birthdate, INTERVAL observed_age_group.min_years YEAR), INTERVAL observed_age_group.min_days DAY))
                        AND (DATE_ADD(DATE_ADD(person.birthdate, INTERVAL observed_age_group.max_years YEAR), INTERVAL observed_age_group.max_days DAY))
                WHERE observed_age_group.report_group_name = 'Modified_Ages'
                                AND o.voided = 0
                                and concept_id = 2223 and value_datetime < '2020-07-01'
        and o.person_id not in (
                                select person_id from obs
                                where concept_id in (2397,2403) 
                            )
                )preart_b
        WHERE age > 15   
        )

        )all_enrolled
) all_joined
GROUP BY Heading

