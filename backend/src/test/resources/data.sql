INSERT INTO users (id, name, password, country, email, zip, is_confirmed)
VALUES (-1, 'Josef', 'testpwd', 'Austria', 'a@b.com', '1337', TRUE),
       (-2, 'Mustermann', 'testpwd', 'Bustria', 'b@a.com', '6942', TRUE),
       (-3, 'Mustertrainer', 'testpwd', 'Germany', 'train@best.er', '4201', TRUE),
       (-4, 'Real trainings user', 'testpwd', 'Austria', 'a@trainer.co', '1337', TRUE);

INSERT INTO trainer (id, code)
VALUES (-3, '203');

INSERT INTO athlete (id, dob, height, weight)
VALUES (-1, PARSEDATETIME('02-03-1999', 'dd-MM-yyyy'), '189', '1000'),
       (-4, PARSEDATETIME('02-03-1989', 'dd-MM-yyyy'), '175', '700');

INSERT INTO athlete (id, dob, height, weight, trainer_id)
VALUES (-2, PARSEDATETIME('03-02-2001', 'dd-MM-yyyy'), '130', '1001', -3);


INSERT INTO step (id, duration_distance, duration_distance_unit, duration_type, target_from, target_to, target_type, type, note)
VALUES (-1, '1024', '1', '0', '10', '20', '0', '0', 'sample step #1'),
       (-2, '2048', '0', '2', '10', '20', '0', '2', 'sample step #2'),
       (-3, '4096', '2', '1', '10', '20', '0', '1', 'sample step #3'),
       (-4, '7', '3', '2', '100', '650', '2', '3', 'sample step #4'),
       (-5, '700', '2', '1', '99', '100', '1', '1', 'sample step #5'),
       (-6, '700', '2', '1', '99', '100', '1', '1', 'sample step #6'),
       (-7, null, null, '2', null, null, null, '2', 'lap button warm up #1'),
       (-8, '1', '0', '1', '240', '260', '2', '0', '1km 4:00-4:20'),
       (-9, '2', '2', '1', null, null, null, '1', '2min recovery'),
       (-10, null, null, '2', null, null, null, '3', 'lap button cooldown #1');


INSERT INTO activity_interval (id, repeat, step_id)
VALUES (-1, 1, -1),
       (-2, 2, -2),
       (-3, 5, -3),
       (-4, 1, -4),
       (-5, 2, -5),
       (-6, 1, -6),
       (-7, 1, -7),
       (-8, 1, -8),
       (-9, 1, -9),
       (-10, 7, null),
       (-11, 1, -10),
       (-12, 1, null);

INSERT INTO activity_interval_intervals(interval_id, intervals_id)
VALUES (-10, -8),
       (-10, -9),
       (-12, -7),
       (-12, -10),
       (-12, -11);



INSERT INTO planned_activity(id, template, type, with_trainer, created_by_id, created_for_id, date, interval_id, note, name, load, estimated_duration,
                             activity_id)
VALUES (-1, FALSE, '0', FALSE, -3, -2, PARSEDATETIME('26-08-2023', 'dd-MM-yyyy'), -1, 'test #1', 'test #1', '1', 60, null),
       (-2, TRUE, '1', FALSE, -3, null, null, -6, 'test #2', 'test #2', '1', 60, null),
       (-3, FALSE, '2', FALSE, -3, -1, PARSEDATETIME('26-12-2023', 'dd-MM-yyyy'), -2, 'test #3', 'test #3', '1', 60, null),
       (-4, FALSE, '3', FALSE, -3, -2, PARSEDATETIME('26-10-2023', 'dd-MM-yyyy'), -5, 'test #4', 'test #4', '1', 60, null),
       (-5, FALSE, '4', FALSE, -3, -2, PARSEDATETIME('26-10-2023', 'dd-MM-yyyy'), -3, 'test #5', 'test #5', '1', 60, null),
       (-6, FALSE, '5', FALSE, -3, -2, PARSEDATETIME('12-12-2023', 'dd-MM-yyyy'), -4, 'test #6', 'test #6', '1', 60, null),
       (-7, FALSE, '1', FALSE, -4, -4, PARSEDATETIME('30-09-2023', 'dd-MM-yyyy'), -12, 'test #7', '7x(1km P:1min)', '1', 60, null);

INSERT INTO activity(id, user_id, accuracy, average_bpm, min_bpm, max_bpm, distance, spent_kcal, cadence, avg_power, max_power, start_time, end_time, activity_type)
VALUES (-1, -1, 0, 100, 70, 120, 10000, 800, 80, 100, 120, PARSEDATETIME('18-12-2023 12:10', 'dd-MM-yyyy HH:mm'), PARSEDATETIME('18-12-2023 14:15', 'dd-MM-yyyy HH:mm'), 1)